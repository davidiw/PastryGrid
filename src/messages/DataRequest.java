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

public class DataRequest extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 9;
	TaskPastryGrid task;
	Vector<String> inputs;

	short Ilength;

	public DataRequest(NodeHandle from, String appName, String time,
			TaskPastryGrid task, Vector<String> inputs) {
		super(from, appName, time);
		this.task = task;
		this.inputs = inputs;
	}

	public DataRequest(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		task = TaskPastryGrid.readTaskPastryGrid(buf);
		Ilength = buf.readShort();
		inputs = new Vector<String>();
		for (int i = 0; i < Ilength; i++) {
			String s = buf.readUTF();
			inputs.add(s);
		}
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		task.serialize(buf);
		Ilength = (short) inputs.size();
		buf.writeShort(Ilength);
		for (int i = 0; i < Ilength; i++)
			buf.writeUTF((String) inputs.get(i));
	}

	public void response(final ApplicationPastryGrid App) {
		final String xmlFilePath = NodePastryGrid.rdvDirectory + appName + time + "/"
				+ appName + "/" + ApplicationPastryGrid.appXml;
		new Thread() {
			public void run() {
				/*
				 * if(!new File(App.NPG.rdvDirectory+appName+time).exists()){
				 * App.past.lookupRDV(App.NPG.node,
				 * App.NPG.rdvDirectory+appName+time+".zip",
				 * App.NPG.node.getEnvironment()); App.NPG.sleep(3);
				 * Unzip.unzip(App.NPG.rdvDirectory+appName+time+".zip",
				 * App.NPG.rdvDirectory); }
				 */
				if (inputs.size() == 0)
					inputs = task.getInputFile(xmlFilePath);
				else {
					Vector<String> allinputs = task.getInputFile(xmlFilePath);
					for (int i = 0; i < inputs.size(); i++)
						for (int j = 0; j < allinputs.size(); j++)
							if (inputs.get(i).compareToIgnoreCase(
									allinputs.get(j)) == 0) {
								allinputs.remove(j);
								break;
							}
					inputs = allinputs;
				}

		/*		new Thread() {
					public void run() {
		*/				System.out.println("TriggerSupervision " + task.getName());
						TriggerSupervision triggersupervision = new TriggerSupervision(
								App.NPG.node.getLocalNodeHandle(), appName,
								time, from, task);
						Id idFtc = Id.build(appName + time + "FTC");
						App.routeMyMsg(triggersupervision, idFtc);
						App.NPG.sleep(1);
		/*			}
				}.start();
*/
				// zip files then send
				Vector<String> files = new Vector<String>();
				String taskFolderName = NodePastryGrid.rdvDirectory + appName + time
						+ "/" + appName + "/" + task.getName();
				File taskFolder = new File(taskFolderName);
				taskFolder.mkdirs();

				String dirResults = NodePastryGrid.rdvDirectory + appName + time + "/"
						+ appName + "/results/";
				for (int i = 0; i < inputs.size(); i++)
					//Zip.copyFile(new File(dirResults + inputs.get(i)), taskFolder); // +"/inputs"
					files.add(dirResults + inputs.get(i));

				String fileIn = NodePastryGrid.rdvDirectory + appName + time + "/"
						+ appName + task.getDirIn(xmlFilePath)
						+ task.getFileIn(xmlFilePath);
				files.add(fileIn);
				//Zip.copyFile(new File(fileIn), taskFolder);

				String binary = NodePastryGrid.rdvDirectory + appName + time + "/"
						+ appName + task.BinaryDir(xmlFilePath)
						+ task.BinaryName(xmlFilePath);
				files.add(binary);
				//Zip.copyFile(new File(binary), taskFolder);

				// create Task.xml
				task.generateTaskXml(xmlFilePath, taskFolderName);
				files.add(taskFolderName+"/Task.xml");

				String zipTaskName = NodePastryGrid.rdvDirectory + appName + time
						+ "/" + appName + "/" + task.getName() + ".zip";
			/*	String[] listFiles = taskFolder.list();
				for (int i = 0; i < listFiles.length; i++) {
					String filename = listFiles[i];
					listFiles[i] = taskFolderName + "/" + filename;
				}
*/
				String[] listFiles = new String[files.size()];
				files.copyInto(listFiles);
	//			for (int i = 0; i < listFiles.length; i++)
	//				System.out.println(listFiles[i]);
				Zip.zipFiles(zipTaskName, listFiles);
				// Zip.zipDir(zipTaskName, taskFolderName);
				
				String line = "Sending task's data: " + task + " to " + from;
				System.out.println(line);
				App.NPG.updateHistoryFile(NodePastryGrid.rdvDirectory + appName + time
						+ "/history", line);

				YourData yourdata = new YourData(App.NPG.node
						.getLocalNodeHandle(), appName, time, task, zipTaskName);
				//App.routeMyMsgDirect(yourdata, from);
				App.sendFile(yourdata, from);
				//System.out.println("Data sent.");
/*				while (taskFolder.exists()) {
					Zip.deleteDir(taskFolder);
					App.NPG.sleep(3);
				}
*/
		}
		}.start();

	}

	public short getType() {
		return TYPE;
	}
}