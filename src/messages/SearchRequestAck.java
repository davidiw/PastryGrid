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

public class SearchRequestAck extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 14;
	TaskPastryGrid predTask;
	Vector<TaskPastryGrid> W;

	short Wlength;

	public SearchRequestAck(NodeHandle from, String appName, String time,
			TaskPastryGrid predtask, Vector<TaskPastryGrid> w) {
		super(from, appName, time);
		predTask = predtask;
		W = w;
	}

	public SearchRequestAck(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);

		predTask = TaskPastryGrid.readTaskPastryGrid(buf);
		Wlength = buf.readShort();
		W = new Vector<TaskPastryGrid>();
		for (int i = 0; i < Wlength; i++) {
			TaskPastryGrid task = TaskPastryGrid.readTaskPastryGrid(buf);
			W.add(task);
		}
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		predTask.serialize(buf);
		Wlength = (short) W.size();
		buf.writeShort(Wlength);
		for (int i = 0; i < Wlength; i++)
			(W.get(i)).serialize(buf);
	}

	public void response(ApplicationPastryGrid App) {
		System.out.println("Search request ack: " + predTask);
		String xmlFilePath = NodePastryGrid.workDirectory + appName + time + "/"
				+ predTask + "/Task.xml";
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
				.getLocalNodeHandle(), appName, time, predTask, H, W, R, TTL);
		App.routeMyMsgDirect(workrequest, H.get(0));
	}

	public short getType() {
		return TYPE;
	}
}