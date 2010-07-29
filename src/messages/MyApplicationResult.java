package messages;

import java.io.File;
import java.io.IOException;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class MyApplicationResult extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 19;

	public MyApplicationResult(NodeHandle from, String appName, String time) {
		super(from, appName, time);
	}

	public MyApplicationResult(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);

	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

	}

	public void response(ApplicationPastryGrid App) {
		/*
		 * if(!new File(App.NPG.rdvDirectory+appName+time).exists()){
		 * App.past.lookupRDV(App.NPG.node,
		 * App.NPG.rdvDirectory+appName+time+".zip",
		 * App.NPG.node.getEnvironment()); App.NPG.sleep(3);
		 * Unzip.unzip(App.NPG.rdvDirectory+appName+time+".zip",
		 * App.NPG.rdvDirectory); }
		 */

		String result = NodePastryGrid.rdvResultsDirectory + "/" + appName + time
				+ "result.zip";
		MessagePastryGrid yourapplicationresult;
		if (new File(result).exists()){
			yourapplicationresult = new YourApplicationResult(App.NPG.node
					.getLocalNodeHandle(), appName, time, result);
			App.sendFile(yourapplicationresult, from);
		}
		else{
			yourapplicationresult = new YourApplicationResultNotYet(
					App.NPG.node.getLocalNodeHandle(), appName, time);
			App.routeMyMsgDirect(yourapplicationresult, from);
		}

//		new File(result).delete();

	}

	public short getType() {
		return TYPE;
	}
}
