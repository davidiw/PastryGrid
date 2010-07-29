package ftc;

import java.io.IOException;

import principal.TaskPastryGrid;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class NodeDetails {
	public int indexApp;
	public TaskPastryGrid task;
	public NodeHandle nodeHandle;

	public NodeDetails(int indexApp, TaskPastryGrid task, NodeHandle nodeHandle) {
		super();
		this.indexApp = indexApp;
		this.task = task;
		this.nodeHandle = nodeHandle;
	}

	public void serialize(OutputBuffer buf) throws IOException {
		buf.writeInt(indexApp);
		task.serialize(buf);
		nodeHandle.serialize(buf);
	}

	public static NodeDetails readNodeDetails(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		int indexapp = buf.readInt();
		TaskPastryGrid task = TaskPastryGrid.readTaskPastryGrid(buf);
		NodeHandle nodeHandle = endpoint.readNodeHandle(buf);

		return new NodeDetails(indexapp, task, nodeHandle);
	}
}
