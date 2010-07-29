package messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ftc.Supervisor;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.TaskPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class StopWorking extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 18;
	TaskPastryGrid task;
	boolean toFTC;

	public StopWorking(NodeHandle from, String appName, String time,
			TaskPastryGrid task, boolean toftc) {
		super(from, appName, time);
		this.task = task;
		toFTC = toftc;
	}

	public StopWorking(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		task = TaskPastryGrid.readTaskPastryGrid(buf);
		toFTC = buf.readBoolean();
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		task.serialize(buf);
		buf.writeBoolean(toFTC);
	}

	@SuppressWarnings("unchecked")
	public NodeHandle SetCancelledInNodesExecutingTasksXML(String pathFile) {

		if (!(new File(pathFile)).exists()) {
			System.out.println("XmlError : " + pathFile + " not found");
			return null;
		}
		NodeHandle node = null;
		Document document = null;
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(new File(pathFile));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Element root = document.getRootElement();

		List<Element> tasks = root.getChildren("Task");

		for (int i = tasks.size() - 1; i >= 0; i--)
			if (tasks.get(i).getAttributeValue("taskName").compareTo(
					task.getName()) == 0
					&& tasks.get(i).getAttributeValue("state").compareTo(
							"in execution") == 0) {
				tasks.get(i).setAttribute("state", "cancelled");
				tasks.get(i).setAttribute("finishTime",
						NodePastryGrid.getTime());
				node = Supervisor.getNodeHandle(tasks.get(i).getAttributeValue(
						"nodeId"));
				break;
			}

		try {
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(pathFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return node;
	}

	public void response(ApplicationPastryGrid App) {
		if (toFTC) {
			if (Supervisor.appExist(appName, time)) {
				NodeHandle node = SetCancelledInNodesExecutingTasksXML(NodePastryGrid.ftcDirectory
						+ appName + time + "/NodesExecutingTasks.xml");
				if (node != null) {
					// delete it from ftc
					StopWorking stopworking = new StopWorking(App.NPG.node
							.getLocalNodeHandle(), appName, time, task, false);
					App.routeMyMsgDirect(stopworking, node);
				}
				App.supervisor.removeNode(node, appName, time);
			}
		} else { // if this is a worker
			if (WorkRequest.taskToExecute != null
					&& task.getName().compareToIgnoreCase(
							WorkRequest.taskToExecute.getName()) == 0) {
				App.stopped = true;
				if (App.process != null)
					App.process.destroy();
			}
		}
	}

	public short getType() {
		return TYPE;
	}
}
