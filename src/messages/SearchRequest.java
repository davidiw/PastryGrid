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

public class SearchRequest extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 13;
	TaskPastryGrid task;

	public SearchRequest(NodeHandle from, String appName, String time,
			TaskPastryGrid task) {
		super(from, appName, time);
		this.task = task;
	}

	public SearchRequest(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		task = TaskPastryGrid.readTaskPastryGrid(buf);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		task.serialize(buf);
	}

	public void response(final ApplicationPastryGrid App) {
		final String xmlFilePath = NodePastryGrid.rdvDirectory + appName + time + "/"
				+ appName + "/" + ApplicationPastryGrid.appXml;
	/*	new Thread() {
			public void run() {
	*/			/*
				 * if(!new File(App.NPG.rdvDirectory+appName+time).exists()){
				 * App.past.lookupRDV(App.NPG.node,
				 * App.NPG.rdvDirectory+appName+time+".zip",
				 * App.NPG.node.getEnvironment()); App.NPG.sleep(3);
				 * Unzip.unzip(App.NPG.rdvDirectory+appName+time+".zip",
				 * App.NPG.rdvDirectory); }
				 */

				Vector<TaskPastryGrid> sharedTasks = task
						.getSharedSucc(xmlFilePath);
				String dirIn = NodePastryGrid.rdvDirectory + appName + time + "/"
						+ appName + "/results/";
				for (int i = 0; i < sharedTasks.size(); i++) {
					Vector<String> inputFiles = sharedTasks.get(i)
							.getInputFile(xmlFilePath);
					for (int j = 0; j < inputFiles.size(); j++) {
						if (!new File(dirIn + inputFiles.get(j)).exists()) {
							sharedTasks.remove(i);
							i--;
							break;
						}
					}
				}

				if (sharedTasks.size() > 0) {
					String line = "SearchRequestAck to " + from;
					System.out.println(line);
					App.NPG.updateHistoryFile(NodePastryGrid.rdvDirectory + appName
							+ time + "/history", line);
					SearchRequestAck searchrequestack = new SearchRequestAck(
							App.NPG.node.getLocalNodeHandle(), appName, time,
							task, sharedTasks);
					App.routeMyMsgDirect(searchrequestack, from);
				} else {
					String line = "SearchRequestReject to " + from;
					System.out.println(line);
					App.NPG.updateHistoryFile(NodePastryGrid.rdvDirectory + appName
							+ time + "/history", line);
					SearchRequestReject searchrequestreject = new SearchRequestReject(
							App.NPG.node.getLocalNodeHandle(), appName, time);
					App.routeMyMsgDirect(searchrequestreject, from);
				}
	/*		}
		}.start();
*/
	}

	public short getType() {
		return TYPE;
	}
}