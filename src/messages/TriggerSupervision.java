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

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.TaskPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class TriggerSupervision extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 10;
	public NodeHandle node; // the node handle of the node to supervise
	TaskPastryGrid task;

	public TriggerSupervision(NodeHandle from, String appName, String time,
			NodeHandle node, TaskPastryGrid task) {
		super(from, appName, time);
		this.node = node;
		this.task = task;
	}

	public TriggerSupervision(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);

		node = endpoint.readNodeHandle(buf);
		task = TaskPastryGrid.readTaskPastryGrid(buf);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		node.serialize(buf);
		task.serialize(buf);
	}

	@SuppressWarnings("unchecked")
	public void addInNodesExecutingTasksXML(String pathFile) {

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
		for (int i = tasks.size() - 1; i >= 0; i--)
			if (tasks.get(i).getAttributeValue("taskName").compareTo(
					task.getName()) == 0
					&& tasks.get(i).getAttributeValue("state").compareTo(
							"in execution") == 0) {
				tasks.get(i).setAttribute("state", "failed");
				tasks.get(i).setAttribute("finishTime",
						NodePastryGrid.getTime());
				break;
			}

		Element Task = new Element("Task");
		Task.setAttribute("taskName", task.getName());
		Task.setAttribute("nodeId", node.getId().toStringFull());
		Task.setAttribute("startTime", NodePastryGrid.getTime());
		Task.setAttribute("state", "in execution");
		root.addContent(Task);

		try {
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(pathFile));
			//System.out.println("added " + task.getName());
		} catch (IOException e) {
			//System.out.println("failed adding " + task.getName());
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
				App.supervisor.addNode(appName, time, task, node);
				addInNodesExecutingTasksXML(NodePastryGrid.ftcDirectory + appName + time
						+ "/NodesExecutingTasks.xml");
			}
		}.start();
	
		
	}

	public short getType() {
		return TYPE;
	}
}