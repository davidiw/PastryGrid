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

public class ApplicationReceived extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 17; // 4

	public ApplicationReceived(NodeHandle from, String appName, String time) {
		super(from, appName, time);

	}

	public ApplicationReceived(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);
	}

	public void response(ApplicationPastryGrid App) {
		String line = "Searching nodes to execute first tasks of App: "
				+ appName;
		System.out.println(line);
		App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
				+ App.NPG.node.getId().hashCode() + "/history", line);
		Vector<TaskPastryGrid> W;
		W = App.getTasks(null, NodePastryGrid.submissionDirectory + time + "/"
				+ ApplicationPastryGrid.appXml);
		TaskPastryGrid predTask = new TaskPastryGrid();
		if (W.size() == 0) {
			System.out.println("There is no tasks to execute.");
			return;
		}
		Requirements R = W.get(0).getRequirements(
				NodePastryGrid.submissionDirectory + time + "/"
						+ ApplicationPastryGrid.appXml);
		LeafSet leafSet = ((PastryNode) App.NPG.node).getLeafSet();
		Vector<NodeHandle> H = new Vector<NodeHandle>();
		NodeHandle nh;

		int i = 1;
		if (App.NPG.submissionNodeCanWork)
			i = 0;
		for (; i <= leafSet.cwSize(); i++) {
			nh = leafSet.get(i);
			H.add(nh);
		}
		
		if(H.size()==0)
			System.err.println("There is no nodes to execute tasks");
		else{
			short TTL = 10;
			WorkRequest workrequest = new WorkRequest(App.NPG.node
					.getLocalNodeHandle(), appName, time, predTask, H, W, R, TTL);
			App.routeMyMsgDirect(workrequest, H.get(0));
		}
		

	}

	public short getType() {
		return TYPE;
	}
}