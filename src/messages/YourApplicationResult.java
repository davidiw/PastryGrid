package messages;

import java.io.IOException;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.pastry.Id;

public class YourApplicationResult extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 20;
/*	public byte[] file;
	public int Flength;
*/	public String filepath;

	public YourApplicationResult(NodeHandle from, String appName, String time,
			String filepath) {
		super(from, appName, time);
		this.filepath = filepath;
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

	public YourApplicationResult(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);
		filepath = "";
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
/*
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
*/
	public void response(final ApplicationPastryGrid App) {
		new Thread() {
			public void run() {

				String line = "Receiving Application's Result : " + appName
						+ " ( " + Id.build(appName + time) + " ) from node: "
						+ from.getId();
				System.out.println(line);
				App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
						+ App.NPG.node.getId().hashCode() + "/history", line);
//				saveFile("Result" + appName + time + ".zip",
//						NodePastryGrid.submissionDirectory + time);

//				Unzip.unzip(NodePastryGrid.submissionDirectory + time + "/Result"
//						+ appName + time + ".zip", NodePastryGrid.submissionDirectory
//						+ time);

				line = "Application's Result " + appName + " ( "
						+ Id.build(appName + time)
						+ " ) received and stored in: "
						+ NodePastryGrid.submissionDirectory + time;
				System.out.println(line);
				App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
						+ App.NPG.node.getId().hashCode() + "/history", line);
			}
		}.start();

	}

	public short getType() {
		return TYPE;
	}
}