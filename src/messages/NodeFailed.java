package messages;

import java.io.IOException;
import java.util.Vector;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.Requirements;
import principal.TaskPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.PastryNode;
import rice.pastry.leafset.LeafSet;

public class NodeFailed extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 12;
	NodeHandle node;
	TaskPastryGrid task;

	public NodeFailed(NodeHandle from, String appName, String time,
			NodeHandle node, TaskPastryGrid task) {
		super(from, appName, time);
		this.node = node;
		this.task = task;
	}

	public NodeFailed(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		node = endpoint.readNodeHandle(buf);
		task = TaskPastryGrid.readTaskPastryGrid(buf);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		node.serialize(buf);
		task.serialize(buf);
	}

	public void response(ApplicationPastryGrid App) {
		String xmlFilePath = NodePastryGrid.rdvDirectory + appName + time + "/"
				+ appName + "/" + ApplicationPastryGrid.appXml;
		String line = "Node " + node.getId() + " Failed. Reexecuting task "
				+ task;
		System.out.println(line);
		App.NPG.updateHistoryFile(NodePastryGrid.rdvDirectory + appName + time
				+ "/history", line);
		Vector<TaskPastryGrid> W = new Vector<TaskPastryGrid>();
		TaskPastryGrid predTask = new TaskPastryGrid();
		W.add(task);

		Requirements R = W.get(0).getRequirements(xmlFilePath);
		LeafSet leafSet = ((PastryNode) App.NPG.node).getLeafSet();
		Vector<NodeHandle> H = new Vector<NodeHandle>();
		NodeHandle nh;
		for (int i = -leafSet.ccwSize(); i <= leafSet.cwSize(); i++) {
			nh = leafSet.get(i);
			H.add(nh);
		}

		short TTL = 10;
		WorkRequest workrequest = new WorkRequest(App.NPG.node
				.getLocalNodeHandle(), appName, time, predTask, H, W, R, TTL);
		App.routeMyMsgDirect(workrequest, H.get(0));
	}

	public short getType() {
		return TYPE;
	}
}