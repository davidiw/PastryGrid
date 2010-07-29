package messages;

import java.io.IOException;

import principal.ApplicationPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class InitRDV extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 1;

	public InitRDV(NodeHandle from, String appName, String time) {
		super(from, appName, time);

	}

	public InitRDV(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);
	}

	public void response(final ApplicationPastryGrid App) {
		new Thread() {
			public void run() {
				GetApplication getapplication = new GetApplication(App.NPG.node
						.getLocalNodeHandle(), appName, time);
				App.routeMyMsgDirect(getapplication, from);
			}
		}.start();

	}

	public short getType() {
		return TYPE;
	}
}
