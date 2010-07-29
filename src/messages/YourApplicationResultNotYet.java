package messages;

import java.io.IOException;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.Id;

public class YourApplicationResultNotYet extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 21;

	public YourApplicationResultNotYet(NodeHandle from, String appName,
			String time) {
		super(from, appName, time);
	}

	public YourApplicationResultNotYet(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);

	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

	}

	public void response(ApplicationPastryGrid App) {
		String line = "Application " + appName + " ( "
				+ Id.build(appName + time)
				+ " ) is running and is not completed.";
		System.out.println(line);
		App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
				+ App.NPG.node.getId().hashCode() + "/history", line);
	}

	public short getType() {
		return TYPE;
	}
}
