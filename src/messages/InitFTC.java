package messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ftc.Supervisor;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class InitFTC extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 4;

	public InitFTC(NodeHandle from, String appName, String time) {
		super(from, appName, time);

	}

	public InitFTC(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		// buf.writeShort(TYPE);
		super.serialize(buf);
	}

	public void makeNodesExecutingTasksXML(String pathFile) {

		Element root = new Element("Application");
		root.setAttribute("appName", appName);
		root.setAttribute("appTime", time);
		root.setAttribute("startTime", NodePastryGrid.getTime());
		Document document = new Document(root);

		try {
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(pathFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String line = "Finished creating " + pathFile;
		System.out.println(line);

	}

	public void response(ApplicationPastryGrid App) {
		String line = "I'm the FTC" + App.NPG.node.getLocalNodeHandle()
				+ " of App: " + appName + "-" + time;
		System.out.println(line);
		new File(NodePastryGrid.ftcDirectory + appName + time).mkdirs();
		File tmpFile = new File(NodePastryGrid.ftcDirectory + appName + time
				+ "/history");
		try {
			tmpFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		App.NPG.updateHistoryFile(NodePastryGrid.ftcDirectory + appName + time
				+ "/history", line);
		/*
		 * File ftcTaskFail = new File(App.NPG.ftcDirectory+"/FTC_TaskFail");
		 * if(ftcTaskFail.exists()) ftcTaskFail.delete();
		 */
		makeNodesExecutingTasksXML(NodePastryGrid.ftcDirectory + appName + time
				+ "/NodesExecutingTasks.xml");

		App.supervisor = new Supervisor(App);
		Supervisor.addApplication(appName, time);
		App.supervisor.run();
	}

	public short getType() {
		return TYPE;
	}
}
