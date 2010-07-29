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

public class WorkDone extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 16;
	TaskPastryGrid task;

	public WorkDone(NodeHandle from, String appName, String time,
			TaskPastryGrid task) {
		super(from, appName, time);
		this.task = task;
	}

	public WorkDone(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		task = TaskPastryGrid.readTaskPastryGrid(buf);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		task.serialize(buf);
	}

	@SuppressWarnings("unchecked")
	public void SetCompletedInNodesExecutingTasksXML(String pathFile) {

		//System.out.println("SetFinished "+ task);
		if (!(new File(pathFile)).exists()) {
			System.out.println("XmlError : " + pathFile + " not found");
			return;
		}

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
		//System.out.println("tasks.size() ="+tasks.size());
		for (int i = tasks.size() - 1; i >= 0; i--)
			if (tasks.get(i).getAttributeValue("taskName").compareTo(task.getName()) == 0
					&& tasks.get(i).getAttributeValue("nodeId").compareTo(from.getId().toStringFull()) == 0) {
				tasks.get(i).setAttribute("state", "completed");
				tasks.get(i).setAttribute("finishTime",
						NodePastryGrid.getTime());
				break;
			}

		try {
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(pathFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void SetAppFinishedInNodesExecutingTasksXML(String pathFile) {

		if (!(new File(pathFile)).exists()) {
			System.out.println("XmlError : " + pathFile + " not found");
			return;
		}

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
		root.setAttribute("finishTime", NodePastryGrid.getTime());

		try {
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(pathFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void response(final ApplicationPastryGrid App) {
		/*
		 * if(!new File(App.NPG.ftcDirectory+appName+time).exists()){
		 * 
		 * App.supervisor = new Supervisor(App); App.supervisor.run();
		 * 
		 * App.past.lookupFTC(Supervisor.Apps, Supervisor.Nodes,
		 * App.NPG.node.getEnvironment()); App.NPG.sleep(3);
		 * 
		 * }
		 */
		new Thread() { 
			public void run() {
				if (task.getName().compareTo("") != 0) {
					SetCompletedInNodesExecutingTasksXML(NodePastryGrid.ftcDirectory + appName
							+ time + "/NodesExecutingTasks.xml");
					App.supervisor.removeNode(from, appName, time);
				} else {
					SetAppFinishedInNodesExecutingTasksXML(NodePastryGrid.ftcDirectory
							+ appName + time + "/NodesExecutingTasks.xml");
					Supervisor.removeApplication(appName, time);
				}
			}
		}.start();
		

	}

	public short getType() {
		return TYPE;
	}
}
