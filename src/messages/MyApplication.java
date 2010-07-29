package messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.Id;
import zip.Unzip;

public class MyApplication extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 3;
	public byte[] file;
	public int Flength;
	public static String AppPath;

	public MyApplication(NodeHandle from, String appName, String time,
			String filepath) {
		super(from, appName, time);
/*
		File f = new File(filepath);
		Flength = (int) f.length();
		file = new byte[Flength];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(file);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
	}

	public MyApplication(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);
/*
		Flength = buf.readInt();
		file = new byte[Flength];
		buf.read(file, 0, file.length);
*/	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);
/*
		buf.writeInt(Flength);
		buf.write(file, 0, file.length);
*/	}

	public void saveFile(String name, String path) {
		if (path.endsWith("/") || path.endsWith("\\"))
			path = path.substring(0, path.length() - 1);
		new File(path).mkdirs();
		File f = new File(path + "/" + name);
		FileOutputStream out;
		try {
			out = new FileOutputStream(f);
			out.write(file);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void response(final ApplicationPastryGrid App) {
		System.out.println("I'm the RDV "+App.NPG.node);
		new Thread() {
			public void run() {

				//saveFile(appName + time + ".zip", NodePastryGrid.rdvDirectory);
				// unzip file etc...
				Unzip.unzip(NodePastryGrid.rdvDirectory + appName + time + ".zip",
						NodePastryGrid.rdvDirectory + appName + time);
				File results = new File(NodePastryGrid.rdvDirectory + appName + time
						+ "/" + appName + "/results");
				results.mkdirs();
				try {
					if (!new File(NodePastryGrid.rdvDirectory + appName + time
							+ "/history").createNewFile())
						System.out.println("Error: creating rdv history file");
				} catch (IOException e) {
					e.printStackTrace();
				}

				String line = "Application received: " + appName + " ( "
						+ Id.build(appName + time) + " ) from node: "
						+ from.getId();
				System.out.println(line);
				App.NPG.updateHistoryFile(NodePastryGrid.rdvDirectory + appName + time
						+ "/history", line);
				// Zip.deleteDir(new
				// File(App.NPG.workDirectory+appName+time+".zip"));

				// replicate RDV
				/*
				 * Zip.zipDir(App.NPG.rdvDirectory+appName+time+".zip",
				 * App.NPG.rdvDirectory+appName+time);
				 * App.past.insertRDV(App.NPG.node,
				 * App.NPG.rdvDirectory+appName+time+".zip",
				 * App.NPG.node.getEnvironment());
				 */
				ApplicationReceived applicationreceived = new ApplicationReceived(
						App.NPG.node.getLocalNodeHandle(), appName, time);
				App.routeMyMsgDirect(applicationreceived, from);
			}
		}.start();

	}

	public short getType() {
		return TYPE;
	}
}