package ftc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import messages.NodeFailed;
import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.TaskPastryGrid;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.Id;

public class Supervisor {

	public static ApplicationPastryGrid AppPG = null;
	public static Vector<NodeDetails> Nodes = new Vector<NodeDetails>();
	public static Vector<AppDetails> Apps = new Vector<AppDetails>();
	public static final int startAfterNbSec = 3;
	public static final int periodInSec = 3;

	Timer timer;

	public Supervisor(ApplicationPastryGrid AppPG) {
		Supervisor.AppPG = AppPG;
		timer = new Timer();
	}

	public Supervisor(Vector<AppDetails> Apps, Vector<NodeDetails> Nodes) {
		Supervisor.Apps = Apps;
		Supervisor.Nodes = Nodes;
		run();
	}

	public static boolean addApplication(String appName, String appTime) {
		if (getAppIndex(appName, appTime) == -1) {
			AppDetails appdetails = new AppDetails(appName, appTime);
			Apps.add(appdetails);
			System.out.println("Application added to FTC ");
			return true;
		} else {
			System.out.println("Error: App " + appName + " - "
					+ Id.build(appName + appTime).toString()
					+ " exist in this FTC");
			return false;
		}
	}

	public static boolean removeApplication(String appName, String appTime) {
		int index = getAppIndex(appName, appTime);
		if (index != -1) {
			for (int i = 0; i < Nodes.size(); i++)
				if (Nodes.get(i).indexApp == index) {
					Nodes.remove(i);
					i--;
				}

			Apps.remove(index);
			return true;
		} else {
			System.out.println("Error: App " + appName + " - "
					+ Id.build(appName + appTime).toString()
					+ " doesn't exist in this FTC");
			return false;
		}
	}

	public static boolean appExist(String appName, String appTime) {
		if (getAppIndex(appName, appTime) != -1)
			return true;
		return false;
	}

	public static int getAppIndex(String appName, String appTime) {
		for (int i = 0; i < Apps.size(); i++)
			if (Apps.get(i).appName.compareTo(appName) == 0
					&& Apps.get(i).appTime.compareTo(appTime) == 0)
				return i;
		return -1;
	}

	public static int getNodeIndex(NodeHandle nodeHandle) {
		for (int i = 0; i < Nodes.size(); i++)
			if (Nodes.get(i).nodeHandle.equals(nodeHandle))
				return i;
		return -1;
	}

	public static NodeHandle getNodeHandle(String id) {
		for (int i = 0; i < Nodes.size(); i++)
			if (Nodes.get(i).nodeHandle.getId().toStringFull().compareTo(id) == 0)
				return Nodes.get(i).nodeHandle;
		return null;
	}

	public void addNode(String appName, String appTime, TaskPastryGrid task,
			NodeHandle nodeHandle) {
		int indexApp = getAppIndex(appName, appTime);
		if (indexApp == -1)
			System.out.println("Error: App " + appName + " - "
					+ Id.build(appName + appTime).toString()
					+ " not found in this FTC ");
		else {
			if (getNodeIndex(nodeHandle) == -1) {
				NodeDetails nodedetails = new NodeDetails(indexApp, task,
						nodeHandle);
				Nodes.add(nodedetails);
				AppPG.NPG.updateHistoryFile(NodePastryGrid.ftcDirectory
						+ appName + appTime + "/history", "Supervise task: "
						+ task + " of App: " + appName + " on node: "
						+ nodeHandle.getId());
				System.out.println("Node of task " + task + " added to FTC ");
				// AppPG.past.insertFTC(Apps, Nodes,
				// AppPG.NPG.node.getEnvironment());
			} else
				System.out.println("Error: Node "
						+ nodeHandle.getId().toString() + "exist in this FTC");

		}
	}

	public void removeNode(NodeHandle nodeHandle, String appName, String appTime) {
		int index = getNodeIndex(nodeHandle);
		if (index == -1)
			//System.out.println("Error: Node " + nodeHandle.getId().toString()
			//		+ " not found in this FTC ");
			;
		else {
			Nodes.remove(index);
			AppPG.NPG.updateHistoryFile(NodePastryGrid.ftcDirectory + appName
					+ appTime + "/history", "Stop supervise node :"
					+ nodeHandle);
			System.out.println("Node of task removed from FTC ");
			// AppPG.past.insertFTC(Apps, Nodes,
			// AppPG.NPG.node.getEnvironment());
		}

	}

	public void run() {
		timer.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				NodeAlive();
			}
		}, startAfterNbSec * 1000, periodInSec * 1000);
	}

	public void stop() {
		timer.cancel();
	}

	public void NodeAlive() {
		final Vector<Integer> nodesToRemove = new Vector<Integer>();
		for (int i = 0; i < Nodes.size(); i++) {
			final NodeHandle nodeHandle = Nodes.get(i).nodeHandle;
			final TaskPastryGrid task = Nodes.get(i).task;
			final int index = i;
			final int indexApp = Nodes.get(i).indexApp;
			new Thread() {
				public void run() {

					if (!nodeHandle.checkLiveness()) {
						int k = 0;
						boolean alive = false;
						while (k < 3) {
							AppPG.NPG.sleep(5);
							if (nodeHandle.checkLiveness()) {
								alive = true;
								break;
							}
							k++;
						}
						if (!alive) {
							System.out.println("Node dead :" + nodeHandle);
							String appName = Apps.get(indexApp).appName;
							String appTime = Apps.get(indexApp).appTime;
							AppPG.NPG.updateHistoryFile(NodePastryGrid.ftcDirectory
									+ appName + appTime + "/history",
									"Node dead :" + nodeHandle);
							Id idRdv = Id.build(appName + appTime);
							NodeFailed nodefailed = new NodeFailed(nodeHandle,
									appName, appTime, nodeHandle, task);
							AppPG.routeMyMsg(nodefailed, idRdv);

							SetFailedInNodesExecutingTasksXML(
									NodePastryGrid.ftcDirectory + appName + appTime
											+ "/NodesExecutingTasks.xml", task
											.getName());
							AppPG.NPG.sleep(1);
							nodesToRemove.add(index);
						}
					}
				}
			}.start();
		}

		for (int i = 0; i < nodesToRemove.size(); i++)
			Nodes.remove(nodesToRemove.get(i));
		if (nodesToRemove.size() > 0)
			AppPG.past.insertFTC(Apps, Nodes, AppPG.NPG.node.getEnvironment());
	}

	@SuppressWarnings("unchecked")
	public void SetFailedInNodesExecutingTasksXML(String pathFile, String task) {

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
			if (tasks.get(i).getAttributeValue("taskName").compareTo(task) == 0
					&& tasks.get(i).getAttributeValue("state").compareTo(
							"in execution") == 0) {
				tasks.get(i).setAttribute("state", "failed");
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

}
