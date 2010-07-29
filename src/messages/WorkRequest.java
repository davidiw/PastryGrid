package messages;

import java.io.IOException;
import java.util.Vector;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.Requirements;
import principal.TaskPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.Id;
import rice.pastry.PastryNode;
import rice.pastry.leafset.LeafSet;

public class WorkRequest extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 5;
	TaskPastryGrid predTask;
	Vector<NodeHandle> H;
	Vector<TaskPastryGrid> W;
	Requirements R;
	short TTL;

	public static TaskPastryGrid taskToExecute = null;
	short Hlength;
	short Wlength;

	public WorkRequest(NodeHandle nodeHandle, String appName, String time,
			TaskPastryGrid predtask, Vector<NodeHandle> h,
			Vector<TaskPastryGrid> w, Requirements r, short tTL) {
		super(nodeHandle, appName, time);
		predTask = predtask;
		H = h;
		W = w;
		R = r;
		TTL = tTL;
	}

	public WorkRequest(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		predTask = TaskPastryGrid.readTaskPastryGrid(buf);
		Hlength = buf.readShort();
		H = new Vector<NodeHandle>();
		for (int i = 0; i < Hlength; i++) {
			NodeHandle nh = (NodeHandle) endpoint.readNodeHandle(buf);
			H.add(nh);
		}

		Wlength = buf.readShort();
		W = new Vector<TaskPastryGrid>();
		for (int i = 0; i < Wlength; i++) {
			TaskPastryGrid task = TaskPastryGrid.readTaskPastryGrid(buf);
			W.add(task);
		}

		R = Requirements.readRequirements(buf);
		TTL = buf.readShort();
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		predTask.serialize(buf);
		Hlength = (short) H.size();
		buf.writeShort(Hlength);
		for (int i = 0; i < Hlength; i++)
			((NodeHandle) H.get(i)).serialize(buf);

		Wlength = (short) W.size();
		buf.writeShort(Wlength);
		for (int i = 0; i < Wlength; i++)
			(W.get(i)).serialize(buf);

		R.serialize(buf);
		buf.writeShort(TTL);
		
	}

	public void response(final ApplicationPastryGrid App) {
		new Thread() { 
			public void run() {
				
				if (W.size() == 0){
					System.out.println("W.size() == 0");
					return;
				}
				H.remove(0);
				if (App.isIdle()) {
					if (App.verifyRequirements(R)) {
						App.idle = false;
						System.out.println("I'm busy");
						final TaskPastryGrid task = W.remove(0);
						taskToExecute = task;
						String line = "Taking task: " + task + " of App: " + appName
								+ " in node: " + App.NPG.node.getId();
						System.out.println(line);
						App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
								+ App.NPG.node.getId().hashCode() + "/history", line);
					/*	new Thread() {
							public void run() {
					*/			if (predTask.getName().compareTo("") != 0
										&& from.checkLiveness()) {
									GetResult getresult = new GetResult(App.NPG.node
											.getLocalNodeHandle(), appName, time,
											predTask);
									App.routeMyMsgDirect(getresult, from);
								} else {
									// demand data from RDV
									 line = "Task: " + task
											+ " demand data from RDV";
									System.out.println(line);
									App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
											+ App.NPG.node.getId().hashCode()
											+ "/history", line);
									Vector<String> inputs = new Vector<String>();
									DataRequest datarequest = new DataRequest(
											App.NPG.node.getLocalNodeHandle(), appName,
											time, task, inputs);
									Id idRdv = Id.build(appName + time);
									App.routeMyMsg(datarequest, idRdv);
								}
						/*	}
						}.start();
		*/
					}
				}
				if (W.size() == 0){
					//System.out.println("W.size() == 0");
					return;
				}
				String line = "Searching nodes for tasks: " + W.toString();
				System.out.println(line);
				App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
						+ App.NPG.node.getId().hashCode() + "/history", line);

				if (TTL == 0) {
					//System.out.println("H.size() == 0 && W.size() > 0 && TTL == 0");
					WorkRequestReject workrequestreject = new WorkRequestReject(
							App.NPG.node.getLocalNodeHandle(), appName, time, predTask,
							W, R);
					App.routeMyMsgDirect(workrequestreject, from);
					App.NPG.sleep(1);
					return;
				}
				else{
					if (H.size() == 0) {
						 //System.out.println("H.size() == 0 && W.size() > 0 && TTL > 0");

							LeafSet leafSet = ((PastryNode) App.NPG.node).getLeafSet();
							NodeHandle nh;
							for (int i = 0; i <= leafSet.cwSize(); i++) {
								// if (i != 0) { // don't send to self
								nh = leafSet.get(i);
								H.add(nh);
								// }
							}
							WorkRequest workrequest = new WorkRequest(from, appName, time,
									predTask, H, W, R, --TTL);
					/*		int i=0;
							while(!H.get(i).checkLiveness() && i<= leafSet.cwSize())
								i++;
							
							if(i>leafSet.cwSize()){
								System.out.println("All nodes are dead");
								WorkRequestReject workrequestreject = new WorkRequestReject(
										App.NPG.node.getLocalNodeHandle(), appName, time, predTask,
										W, R);
								App.routeMyMsgDirect(workrequestreject, from);
								App.NPG.sleep(1);
								return;
							}
							App.routeMyMsgDirect(workrequest, H.get(0));
					*/		
							App.routeMyMsg(workrequest, (Id) H.get(0).getId());
							App.NPG.sleep(1);
					}
					else{
						//System.out.println("H.size() != 0 && W.size() > 0 && TTL > 0");
						WorkRequest workrequest = new WorkRequest(from, appName, time,
								predTask, H, W, R, TTL);
						//App.routeMyMsgDirect(workrequest, H.get(0));
						App.routeMyMsg(workrequest, (Id) H.get(0).getId());
						App.NPG.sleep(1);
					}
				}

			}
		}.start();
		 
	}

	public short getType() {
		return TYPE;
	}
}
