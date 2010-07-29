package messages;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.TaskPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.Id;
import zip.Zip;

public class MyResult extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 7;
	public TaskPastryGrid task;
	//public byte[] file;
	//public int Flength;
	public boolean toRDV;
	public String filepath;
	public MyResult(NodeHandle from, String appName, String time,
			TaskPastryGrid task, String filepath, boolean tordv) {
		super(from, appName, time);

		this.task = task;
		this.filepath = filepath;
/*		if (filepath != null) {
			File f = new File(filepath);
			Flength = (int) f.length();
			file = new byte[Flength];
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				fis.read(file);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			file = new byte[1];
			file[0] = 0;
		}
*/
		toRDV = tordv;
	}

	public MyResult(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		task = TaskPastryGrid.readTaskPastryGrid(buf);
		filepath = "";
/*		Flength = buf.readInt();
		file = new byte[Flength];
		buf.read(file, 0, file.length);
*/		toRDV = buf.readBoolean();
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		task.serialize(buf);
/*		buf.writeInt(Flength);
		buf.write(file, 0, file.length);
*/		buf.writeBoolean(toRDV);
	}
/*
	public void saveFile(String name, String path) {
		if (path.endsWith("/") || path.endsWith("\\"))
			path = path.substring(0, path.length() - 1);
		new File(path).mkdirs();
		File f = new File(path + "/" + name);
		FileOutputStream out;
		try {
			out = new FileOutputStream(f);
			out.write(file);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
*/
	public void response(final ApplicationPastryGrid App) {
	/*	new Thread() {
			public void run() {
		*/		String taskFolder = NodePastryGrid.workDirectory + appName + time
						+ "/" + WorkRequest.taskToExecute;
				String appRdvFolder = NodePastryGrid.rdvDirectory + appName + time
						+ "/" + appName; // +"/results"
				if (!toRDV) { // if this node is a worker
					String line = "Worker Receiving result from "
							+ from.getId();
					System.out.println(line);
					App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
							+ App.NPG.node.getId().hashCode() + "/history",
							line);
					Vector<String> inputs;
					if (task.getName().compareTo("") != 0) {
						//long currentTime = App.NPG.environment.getTimeSource()
						//		.currentTimeMillis();
						//String cuTime = Long.toString(currentTime);

//						saveFile("output" + task + ".zip", taskFolder);
						String zipName = taskFolder + "/" + "output" + task
								+ ".zip";
//						Unzip.unzip(zipName, taskFolder);
						new File(zipName).delete();
						inputs = WorkRequest.taskToExecute
								.getInputFile(taskFolder);
					} else {
						inputs = new Vector<String>();
					}

					System.out.println("Demand Data from RDV");
					DataRequest datarequest = new DataRequest(App.NPG.node
							.getLocalNodeHandle(), appName, time,
							WorkRequest.taskToExecute, inputs);
					Id idRdv = Id.build(appName + time);
					App.routeMyMsg(datarequest, idRdv);
				} else {
					/*
					 * if(!new
					 * File(App.NPG.rdvDirectory+appName+time).exists()){
					 * App.past.lookupRDV(App.NPG.node,
					 * App.NPG.rdvDirectory+appName+time+".zip",
					 * App.NPG.node.getEnvironment()); App.NPG.sleep(3);
					 * Unzip.unzip(App.NPG.rdvDirectory+appName+time+".zip",
					 * App.NPG.rdvDirectory+appName+time); }
					 */
					// if this node is an RDV
					String line = "Receiving result from " + from.getId();
					App.NPG.updateHistoryFile(NodePastryGrid.rdvDirectory + appName
							+ time + "/history", line);
					//long currentTime = App.NPG.environment.getTimeSource()
					//		.currentTimeMillis();
					//String cuTime = Long.toString(currentTime);

//					saveFile("output" + cuTime + ".zip", appRdvFolder
//							+ "/results");
					String zipName = appRdvFolder + "/results/" + "output"
							+ task + ".zip";
//					Unzip.unzip(zipName, appRdvFolder + "/results");
					new File(zipName).delete();

					String xmlFilePath = NodePastryGrid.rdvDirectory + appName + time
							+ "/" + appName + "/"
							+ ApplicationPastryGrid.appXml;
					Vector<String> results = task.getAllOutputFile(xmlFilePath);
					String dirIn = NodePastryGrid.rdvDirectory + appName + time + "/"
							+ appName + "/results/";
					boolean appfinished = true;
					for (int i = 0; i < results.size(); i++) {
						if (!new File(dirIn + results.get(i)).exists())
							appfinished = false;
					}

					if (appfinished) {
						
						// stop supervision && remove app from ftc
				/*		new Thread() {
							public void run() {
					*/			Id idFtc = Id.build(appName + time + "FTC");
								WorkDone workdone = new WorkDone(App.NPG.node
										.getLocalNodeHandle(), appName, time,
										new TaskPastryGrid());
								App.routeMyMsg(workdone, idFtc);
				/*			}
						}.start();
*/
						// zip results folder
						Zip.copyFile(new File(NodePastryGrid.rdvDirectory + appName
								+ time + "/history"), new File(dirIn));
						String zipFileName = NodePastryGrid.rdvDirectory + appName
								+ time + "result.zip";
						Zip.zipDir(zipFileName, dirIn);
						Zip.copyFile(new File(zipFileName), new File(
								NodePastryGrid.rdvResultsDirectory));
						new File(zipFileName).delete();
				/*		// delete app directory
						File appDir = new File(NodePastryGrid.rdvDirectory + appName
								+ time);
						int i = 0;
						while (appDir.exists() && i < 3) {
							Zip.deleteDir(appDir);
							App.NPG.sleep(5);
							i++;
						}
						appDir = new File(NodePastryGrid.rdvDirectory + appName + time
								+ ".zip");
						i = 0;
						while (appDir.exists()) {
							Zip.deleteDir(appDir);
							App.NPG.sleep(5);
							i++;
						}
				*/	
						line = "********** Application " + appName + time
						+ " completed ***************";
				System.out.println(line);
				App.NPG.updateHistoryFile(NodePastryGrid.rdvDirectory
						+ appName + time + "/history", line);
						
					}
					// replicate RDV
					/*
					 * Zip.zipDir(App.NPG.rdvDirectory+appName+time+".zip",
					 * App.NPG.rdvDirectory+appName+time);
					 * App.past.insertRDV(App.NPG.node,
					 * App.NPG.rdvDirectory+appName+time+".zip",
					 * App.NPG.node.getEnvironment());
					 */
					
				}
/*			}
		}.start();
*/
	}

	public short getType() {
		return TYPE;
	}
}