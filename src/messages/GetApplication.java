package messages;

import java.io.IOException;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.Id;

public class GetApplication extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 2;

	public GetApplication(NodeHandle from, String appName, String time) {
		super(from, appName, time);

	}

	public GetApplication(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);
	}

	public void response(ApplicationPastryGrid App) {

		String line = "Sending MyApplication : " + appName + " ( "
				+ Id.build(appName + time) + " ) to RDV: " + from.getId();
		System.out.println(line);
		App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
				+ App.NPG.node.getId().hashCode() + "/history", line);
		MyApplication myapplication = new MyApplication(App.NPG.node
				.getLocalNodeHandle(), appName, time, MyApplication.AppPath);
		App.sendFile(myapplication, from);
		try {
		//	App.routeMyMsgDirect(myapplication, from);
		//	System.out.println(appName + time + ".zip");
			
		} catch (Exception e) { // may be rdv was crached so re-send init ftc to
								// the newer rdv
			Id idRdv = Id.build(appName + time);
			InitRDV initrdv = new InitRDV(App.NPG.node.getLocalNodeHandle(),
					appName, time);
			App.routeMyMsg(initrdv, idRdv);
			App.NPG.sleep(1);
		}

	}

	public short getType() {
		return TYPE;
	}
}