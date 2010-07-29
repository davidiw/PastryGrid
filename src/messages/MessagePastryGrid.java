package messages;

import java.io.IOException;

import principal.ApplicationPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.commonapi.rawserialization.RawMessage;

public abstract class MessagePastryGrid implements RawMessage {

	private static final long serialVersionUID = 1L;
	public NodeHandle from;
	public String appName;
	public String time;

	public MessagePastryGrid(NodeHandle from, String appName, String time) {
		super();
		this.from = from;
		this.appName = appName;
		this.time = time;
	}

	public void serialize(OutputBuffer buf) throws IOException {
		// buf.writeShort(TYPE);
		from.serialize(buf);
		buf.writeUTF(appName);
		buf.writeUTF(time);
	}

	public MessagePastryGrid(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		from = endpoint.readNodeHandle(buf);
		appName = buf.readUTF();
		time = buf.readUTF();
	}

	public int getPriority() {
		return Message.MEDIUM_HIGH_PRIORITY;
	}

	public void response(ApplicationPastryGrid App) {
	}

}
