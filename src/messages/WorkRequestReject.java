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

public class WorkRequestReject extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 8;
	TaskPastryGrid predTask;
	Vector<TaskPastryGrid> W;
	Requirements R;
	short Wlength;

	public WorkRequestReject(NodeHandle nodeHandle, String appName,
			String time, TaskPastryGrid predtask, Vector<TaskPastryGrid> w,
			Requirements r) {
		super(nodeHandle, appName, time);
		predTask = predtask;
		W = w;
		R = r;
	}

	public WorkRequestReject(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);

		predTask = TaskPastryGrid.readTaskPastryGrid(buf);
		Wlength = buf.readShort();
		W = new Vector<TaskPastryGrid>();
		for (int i = 0; i < Wlength; i++) {
			TaskPastryGrid task = TaskPastryGrid.readTaskPastryGrid(buf);
			W.add(task);
		}
		R = Requirements.readRequirements(buf);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		predTask.serialize(buf);
		Wlength = (short) W.size();
		buf.writeShort(Wlength);
		for (int i = 0; i < Wlength; i++)
			(W.get(i)).serialize(buf);
		R.serialize(buf);
	}

	public void response(final ApplicationPastryGrid App) {
		new Thread() {
			public void run() {
				String line = "WorkRequestReject, waiting 20 sec before resending message workRequest...";
				System.out.println(line);
				App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
						+ App.NPG.node.getId().hashCode() + "/history", line);
				App.NPG.sleep(20);
				if (W.size() == 0) {
					System.out.println("There is no tasks...");
					return;
				}

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
						.getLocalNodeHandle(), appName, time, predTask, H, W,
						R, TTL);
				App.routeMyMsgDirect(workrequest, H.get(0));
			}
		}.start();

	}

	public short getType() {
		return TYPE;
	}
}
