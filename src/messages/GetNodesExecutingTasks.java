package messages;

import java.io.File;
import java.io.IOException;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.Id;
import zip.Zip;

public class GetNodesExecutingTasks extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 22;

	public GetNodesExecutingTasks(NodeHandle from, String appName, String time) {
		super(from, appName, time);

	}

	public GetNodesExecutingTasks(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);
	}

	public void response(ApplicationPastryGrid App) {
		String nodesxml = NodePastryGrid.ftcDirectory + appName + time
				+ "/NodesExecutingTasks.xml";
		if (new File(nodesxml).exists()) {
			String zipName = NodePastryGrid.ftcDirectory + appName + time
					+ "/NodesExecutingTasks.zip";
			String[] fileName = new String[1];
			fileName[0] = nodesxml;
			Zip.zipFiles(zipName, fileName);
			String line = "Sending NodesExecutingTasks.xml of Application : "
					+ appName + " ( " + Id.build(appName + time) + " )";
			System.out.println(line);
			App.NPG.updateHistoryFile(NodePastryGrid.ftcDirectory + appName + time
					+ "/history", line);
			NodesExecutingTasks nodesExecutingTasks = new NodesExecutingTasks(
					App.NPG.node.getLocalNodeHandle(), appName, time, zipName);
			App.routeMyMsgDirect(nodesExecutingTasks, from);
		} else {
			String line = "Application : " + appName + " ( "
					+ Id.build(appName + time)
					+ " ) not found, sending NodesExecutingTasks.xml cancelled";
			System.out.println(line);
			App.NPG.updateHistoryFile(NodePastryGrid.ftcDirectory + appName + time
					+ "/history", line);
		}
	}

	public short getType() {
		return TYPE;
	}
}