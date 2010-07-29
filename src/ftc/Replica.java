package ftc;

import java.io.IOException;
import java.util.Vector;

import principal.ApplicationPastryGrid;
import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.past.*;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.persistence.*;

public class Replica {
	public ApplicationPastryGrid AppPG = null;
	public static Past AppPast;
	public static StorageManagerImpl storm;

	public static int nbReplicas = 3;

	public Replica(ApplicationPastryGrid AppPG) {
		this.AppPG = AppPG;
		PastryIdFactory idf = new PastryIdFactory(AppPG.NPG.node
				.getEnvironment());
		// String storageDirectory =
		// "./storage"+AppPG.NPG.node.getId().hashCode();
		rice.persistence.Storage stor = new MemoryStorage(idf);
		storm = new StorageManagerImpl(idf, stor, new LRUCache(
				new MemoryStorage(idf), 0x20000000, AppPG.NPG.node
						.getEnvironment()));
		AppPast = new PastImpl(AppPG.NPG.node, storm, nbReplicas, "past");
		ContentDeserializer deserializer = new ContentDeserializer();
		AppPast.setContentDeserializer(deserializer);
	}

	public void insertRDV(PastryNode node, String filePath, Environment env) {
		try {
			// PastryIdFactory localFactory = new PastryIdFactory(env);
			final PastContent myContent = new RDVContent(filePath);
			Id key = myContent.getId();
			System.out.println("Inserting key : " + key + "  " + myContent
					+ " at node " + AppPast.getLocalNodeHandle());
			AppPast.insert(myContent, new Continuation() {

				// final Replica this$0;
				// private final PastContent val$myContent;

				public void receiveResult(Object result) {
					// System.out.println("Inserting result "+result.toString());
					Boolean results[] = (Boolean[]) result;
					int numSuccessfulStores = 0;
					for (int ctr = 0; ctr < results.length; ctr++) {
						if (results[ctr].booleanValue()) {
							numSuccessfulStores++;
						}
					}

					System.out.print(myContent + " successfully stored  at "
							+ numSuccessfulStores + " locations.");
				}

				public void receiveException(Exception result) {
					System.out.println("Error storing " + myContent);
				}

				/*
				 * { this$0 = Replica.this; myContent = pastcontent; super(); }
				 */
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void lookupRDV(PastryNode node, String filePath, Environment env) {
		// PastryIdFactory idf = new PastryIdFactory(env);
		// boolean t = true;
		try {
			Catalog catalog = storm;
			PastContent myContent = new RDVContent(filePath);
			final Id key = myContent.getId();

			int pos = filePath.lastIndexOf("/") + 1;
			if (pos == 0)
				pos = filePath.lastIndexOf("\\") + 1;

			final String fileName = filePath.substring(pos);
			System.out.println(catalog.getSize());
			System.out.println("Looking up key : " + key.toStringFull());
			AppPast.lookup(key, new Continuation() {

				// final Replica this$0;
				// private final Id val$key;

				public void receiveResult(Object result) {
					System.out.println("Successfully looked up "
							+ result.toString() + " for key " + key);
					if (result != null) {
						RDVContent rdvcontent = (RDVContent) result;
						System.out.println(rdvcontent.toString());
						rdvcontent.saveFile(fileName, AppPG.NPG.rdvDirectory);// .result("initlookup.zip");
					}
				}

				public void receiveException(Exception result) {
					System.out.println((new StringBuilder("Error looking up "))
							.append(key).toString());
					result.printStackTrace();
				}

				/*
				 * { this$0 = Replica.this; key = id; super(); }
				 */
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insertFTC(Vector<AppDetails> Apps, Vector<NodeDetails> Nodes,
			Environment env) {
		try {
			System.out.println("Inserting FTC... ");
			// PastryIdFactory localFactory = new PastryIdFactory(env);
			final PastContent myContent = new FTCContent(Apps, Nodes);
			Id key = myContent.getId();
			System.out.println((new StringBuilder("Inserting key :")).append(
					key).append("  ").append(myContent).append(" at node ")
					.append(AppPast.getLocalNodeHandle()).toString());
			AppPast.insert(myContent, new Continuation() {

				// final Replica this$0;
				// private final PastContent val$myContent;

				public void receiveResult(Object result) {
					System.out.println((new StringBuilder("insert result "))
							.append(result).toString());
					Boolean results[] = (Boolean[]) result;
					int numSuccessfulStores = 0;
					for (int ctr = 0; ctr < results.length; ctr++) {
						if (results[ctr].booleanValue()) {
							numSuccessfulStores++;
						}
					}

					System.out.print((new StringBuilder()).append(myContent)
							.append(" successfully stored  at ").append(
									numSuccessfulStores).append(
									" locations.\n>>>").toString());
				}

				public void receiveException(Exception result) {
					System.out.println((new StringBuilder("Error storing "))
							.append(myContent).toString());
				}

				/*
				 * { this$0 = Replica.this; myContent = pastcontent; super(); }
				 */
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void lookupFTC(Vector<AppDetails> Apps, Vector<NodeDetails> Nodes,
			Environment env) {
		try {
			System.out.println("Looking up ftc...");
			// PastryIdFactory idf = new PastryIdFactory(env);
			// boolean t = true;
			PastContent myContent = new FTCContent(Apps, Nodes);
			final Id key = myContent.getId();
			System.out.println((new StringBuilder("Looking up key :")).append(
					key.toStringFull()).toString());
			AppPast.lookup(key, new Continuation() {

				// final Replica this$0;
				// private final Id val$key;
				// private final String val$app;

				public void receiveResult(Object result) {
					System.out.println((new StringBuilder(
							"Successfully looked up ")).append(result).append(
							" for key ").append(key).append("==")
							.append(result).toString());
					if (result != null) {
						FTCContent ftccontent = (FTCContent) result;
						System.out.println(ftccontent.toString());
						ftccontent.saveFile("ftc"
								+ AppPG.NPG.node.getId().hashCode() + ".tmp",
								AppPG.NPG.ftcDirectory); // .result(app);
					}
				}

				public void receiveException(Exception result) {
					System.out.println((new StringBuilder("Error looking up "))
							.append(key).toString());
					result.printStackTrace();
				}

				/*
				 * { this$0 = Replica.this; key = id; app = s; super(); }
				 */
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}