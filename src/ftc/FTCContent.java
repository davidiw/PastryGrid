package ftc;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastException;
import rice.p2p.past.rawserialization.RawPastContent;
import rice.pastry.Id;

public class FTCContent implements RawPastContent {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 40;

	public Vector<AppDetails> Apps;
	public Vector<NodeDetails> Nodes;

	public int AppsLength;
	public int NodesLength;

	public FTCContent(Vector<AppDetails> Apps, Vector<NodeDetails> Nodes)
			throws IOException {
		this.Apps = Apps;
		this.Nodes = Nodes;

		AppsLength = Apps.size();
		NodesLength = Nodes.size();
	}

	public FTCContent(InputBuffer buf, Endpoint endpoint) throws IOException {
		AppsLength = buf.readInt();
		Apps = new Vector<AppDetails>();
		for (int i = 0; i < AppsLength; i++)
			Apps.add(AppDetails.readAppDetails(buf));

		NodesLength = buf.readInt();
		Nodes = new Vector<NodeDetails>();
		for (int i = 0; i < AppsLength; i++)
			Nodes.add(NodeDetails.readNodeDetails(buf, endpoint));

	}

	public void serialize(OutputBuffer buf) throws IOException {

		buf.writeShort(TYPE);

		AppsLength = Apps.size();
		buf.writeInt(AppsLength);
		for (int i = 0; i < AppsLength; i++)
			Apps.get(i).serialize(buf);

		NodesLength = Nodes.size();
		buf.writeInt(NodesLength);
		for (int i = 0; i < NodesLength; i++)
			Nodes.get(i).serialize(buf);

	}

	public PastContent checkInsert(rice.p2p.commonapi.Id id,
			PastContent existingContent) throws PastException {
		if (existingContent == null) {
			return this;
		}
		if (existingContent instanceof FTCContent) {
			return this;
		} else {
			throw new PastException((new StringBuilder(
					"Content type collision. Inserting Content1 on topof "))
					.append(existingContent).toString());
		}
	}

	public short getType() {
		return TYPE;
	}

	public PastContentHandle getHandle(Past local) {
		return new ContentHashPastContentHandle(local.getLocalNodeHandle(),
				getId());
	}

	public rice.p2p.commonapi.Id getId() {

		return Id.build("ftcid");// Apps.toString()
	}

	public boolean isMutable() {
		return false;
	}

	public String toString() {
		return "FTC";
	}

	/*
	 * public void result(String app) { System.out.println((new
	 * StringBuilder("je suis le point FTC de l'app: "
	 * )).append(app).toString()); File f = new File((new
	 * StringBuilder(String.valueOf
	 * (Mythread.PGRepWork))).append("ftc").append(app
	 * ).append(".tmp").toString()); try { f.createNewFile(); }
	 * catch(IOException e) { e.printStackTrace(); } PG_Layer.sup.set_env(node,
	 * jobs); PG_Layer.sup.run(); }
	 */
	public void saveFile(String name, String path) {
		if (path.endsWith("/") || path.endsWith("\\"))
			path = path.substring(0, path.length() - 1);
		new File(path).mkdirs();
		File file = new File(path + "/" + name); // name = "ftc"+app+".tmp"
		System.out.println(" I'm the FTC of App: " + Apps.toString());

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		new Supervisor(Apps, Nodes);
	}
}
