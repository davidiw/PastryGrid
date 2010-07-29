package messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.Requirements;
import principal.TaskPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.Id;
import rice.pastry.PastryNode;
import rice.pastry.leafset.LeafSet;
import zip.Zip;

public class YourData extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 11;
	public TaskPastryGrid task;
	public String filepath;
//	public byte[] file;
//	public int Flength;

	public YourData(NodeHandle from, String appName, String time,
			TaskPastryGrid task, String filepath) {
		super(from, appName, time);

		this.task = task;
		this.filepath = filepath;
/*		File f = new File(filepath);
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
*/	}

	public YourData(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);
		//System.out.println("Deserialize YourData");

		task = TaskPastryGrid.readTaskPastryGrid(buf);
		filepath = "";
	/*	Flength = buf.readInt();
		file = new byte[Flength];
		buf.read(file, 0, file.length);
	*/
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);
		//System.out.println("Serialize YourData");
		task.serialize(buf);
	/*	buf.writeInt(Flength);
		buf.write(file, 0, file.length);
	*/
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
	public void runCommand(ApplicationPastryGrid App, String dirPath, String cmd) {
		try {
			

			Runtime runtime = Runtime.getRuntime();
			File directory = new File(dirPath);
			if(directory.exists())
				App.process = runtime.exec(cmd, null, directory);
			else
				App.process = runtime.exec(cmd);
			
			InputStream in = App.process.getInputStream();
			// OutputStream out = App.process.getOutputStream();
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

			InputStream err = App.process.getErrorStream();
			isr = new InputStreamReader(err);
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			App.process.waitFor();
			App.process.destroy();
		} catch (Exception e) {
			System.out.println("Execution Error: " + cmd + " " + e.toString());
		}
	}

	public void executeTask(final ApplicationPastryGrid App, String taskXmlPath) {

		String cmd = task.getCmdLine(taskXmlPath);
		String binary = task.BinaryName(taskXmlPath);

		String OS = App.getMyOS().toLowerCase();
		String line = "Executing task... : " + task.getName() + " - App: "
				+ appName + " - AppHandle: " + appName + time;
		System.out.println(line);
		App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
				+ App.NPG.node.getId().hashCode() + "/history", line);

		if (OS != null
				&& (OS.indexOf("linux") != -1 || OS.indexOf("unix") != -1)){
			
			runCommand(App, "/bin", "/bin/chmod 777 " + NodePastryGrid.workDirectory + appName
					+ time + "/" + task + "/" + binary);
			runCommand(App, NodePastryGrid.workDirectory + appName + time + "/" + task,
					NodePastryGrid.workDirectory + appName + time + "/" + task + "/" + cmd);
		}
		else
			runCommand(App, NodePastryGrid.workDirectory + appName + time + "/" + task,
					NodePastryGrid.workDirectory + appName + time + "/" + task + "/" + cmd);
		final String xmlFilePath = NodePastryGrid.workDirectory + appName + time + "/"
				+ task + "/Task.xml";
		if (App.stopped) {
			line = "Task cancelled: " + task.getName() + " - App: " + appName
					+ " - AppHandle: " + appName + time;
			System.out.println(line);
			App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
					+ App.NPG.node.getId().hashCode() + "/history", line);

			Vector<TaskPastryGrid> W = new Vector<TaskPastryGrid>();
			W.add(task);
			Requirements R = W.get(1).getRequirements(xmlFilePath);
			LeafSet leafSet = ((PastryNode) App.NPG.node).getLeafSet();
			Vector<NodeHandle> H = new Vector<NodeHandle>();
			NodeHandle nh;
			for (int i = 0; i <= leafSet.cwSize(); i++) {
				if (i != 0) { // don't send to self
					nh = leafSet.get(i);
					H.add(nh);
				}
			}
			short TTL = 10;
			WorkRequest workrequest = new WorkRequest(App.NPG.node
					.getLocalNodeHandle(), appName, time, new TaskPastryGrid(),
					H, W, R, TTL);
			App.routeMyMsgDirect(workrequest, H.get(0));
			App.NPG.sleep(1);

			WorkRequest.taskToExecute = null;
			App.stopped = false;
			App.process = null;
			App.idle = true;

			return;
		}

		line = "Finish executing task: " + task.getName() + " - App: "
				+ appName + " - AppHandle: " + appName + time;
		System.out.println(line);
		App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
				+ App.NPG.node.getId().hashCode() + "/history", line);
		// send result to rdv
	/*	new Thread() {
			public void run() {
*/
				String output = task.getOutputFile(xmlFilePath);
				String outputFilePath = NodePastryGrid.workDirectory + appName + time
						+ "/" + task + "/" + output;
				if (new File(outputFilePath).exists()) {
					line = "Task: " + task.getName()
							+ " sending result to rdv: " + output;
					System.out.println(line);
					App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
							+ App.NPG.node.getId().hashCode() + "/history",
							line);
					String[] filesPath = { outputFilePath };
					outputFilePath = NodePastryGrid.workDirectory + appName + time
							+ "/" + task + "/" + "output.zip";
					Zip.zipFiles(outputFilePath, filesPath);
					MyResult myresult = new MyResult(App.NPG.node
							.getLocalNodeHandle(), appName, time, task,
							outputFilePath, true);
					//App.routeMyMsgDirect(myresult, from);
					App.sendFile(myresult, from);
					while(!App.transfertComplete){
						//System.out.println("not yet");
						App.NPG.sleep(1);
					}
						
					App.transfertComplete = false;
				} else {
					System.out.println("Error output: " + outputFilePath
							+ " not found");
				}
	/*		}
		}.start();
*/
		// stop supervision
	/*	new Thread() {
			public void run() {
			
	*/			System.out.println("Stop Supervision "+task);
				Id idFtc = Id.build(appName + time + "FTC");
				WorkDone workdone = new WorkDone(App.NPG.node
						.getLocalNodeHandle(), appName, time, task);
				App.routeMyMsg(workdone, idFtc);
	/*		}
		}.start();
*/
		// extract succtasks
		Vector<TaskPastryGrid> W = task.getIsolatedSucc(xmlFilePath);

	/*	if (task.getSharedSucc(xmlFilePath).size() > 0)
			new Thread() {
				public void run() {
					// search request for the shared tasks
					String */
		if (task.getSharedSucc(xmlFilePath).size() > 0){
			line = "Search request task: " + task.getName();
			System.out.println(line);
			App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
					+ App.NPG.node.getId().hashCode() + "/history",
					line);
			SearchRequest searchrequest = new SearchRequest(
					App.NPG.node.getLocalNodeHandle(), appName, time,
					task);
			App.routeMyMsgDirect(searchrequest, from);
		}
		
					
	/*			}
			}.start();
*/
		if (W.size() > 0) {
			// work request for the isolated tasks
			line = "task: " + task.getName() + " W.size= " + W.size();
			System.out.println(line);
			App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
					+ App.NPG.node.getId().hashCode() + "/history", line);
			Requirements R = W.get(0).getRequirements(xmlFilePath);
			LeafSet leafSet = ((PastryNode) App.NPG.node).getLeafSet();
			Vector<NodeHandle> H = new Vector<NodeHandle>();
			NodeHandle nh;
			for (int i = 0; i <= leafSet.cwSize(); i++) {
				// if (i != 0) { // don't send to self
				nh = leafSet.get(i);
				H.add(nh);
				// }
			}
			short TTL = 10;
			WorkRequest workrequest = new WorkRequest(App.NPG.node
					.getLocalNodeHandle(), appName, time, task, H, W, R, TTL);
			App.routeMyMsgDirect(workrequest, H.get(0));
			App.NPG.sleep(1);
		}

		WorkRequest.taskToExecute = null;
		App.idle = true;
		System.out.println("I'm free");
	}

	public void response(final ApplicationPastryGrid App) {
		new Thread() { 
			public void run() {
				System.out.println("Receiving data from RDV");
				String taskFolder = NodePastryGrid.workDirectory + appName + time + "/"
						+ task;
//				saveFile(task + ".zip", taskFolder);
//				Unzip.unzip(taskFolder + "/" + task + ".zip", taskFolder);
				new File(taskFolder + "/" + task + ".zip").delete();

				executeTask(App, taskFolder + "/" + "Task.xml");
			}
		}.start();
		

	}

	public short getType() {
		return TYPE;
	}
}