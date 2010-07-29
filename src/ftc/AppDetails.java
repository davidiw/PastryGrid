package ftc;

import java.io.IOException;

import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class AppDetails {

	public String appName;
	public String appTime;

	// public int startAfterNbSec;
	// public int period;

	public AppDetails(String appName, String appTime) {
		super();
		this.appName = appName;
		this.appTime = appTime;
	}

	public void serialize(OutputBuffer buf) throws IOException {
		buf.writeUTF(appName);
		buf.writeUTF(appTime);
	}

	public static AppDetails readAppDetails(InputBuffer buf) throws IOException {
		String appname = buf.readUTF();
		String apptime = buf.readUTF();

		return new AppDetails(appname, apptime);
	}

	public String toString() {
		return appName + "-" + appTime;
	}
}
