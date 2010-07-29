package messages;

import java.io.IOException;

import principal.ApplicationPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class SearchRequestReject extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 15;

	public SearchRequestReject(NodeHandle from, String appName, String time) {
		super(from, appName, time);
	}

	public SearchRequestReject(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);
	}

	public void response(ApplicationPastryGrid App) {
	}

	public short getType() {
		return TYPE;
	}
}
