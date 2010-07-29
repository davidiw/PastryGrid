package principal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class TaskPastryGrid {
	String name;

	public TaskPastryGrid() {
		name = "";
	}

	public TaskPastryGrid(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	public String getOutputName(String xmlFilePath) {

		Element root = getRoot(xmlFilePath);
		Element task = root;
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					task = tasks.get(i);
					break;
				}
		}
		Element output = task.getChild("Output");
		return output.getAttributeValue("OutputName");
	}

	@SuppressWarnings("unchecked")
	public String getOutputFile(String xmlFilePath) {

		Element root = getRoot(xmlFilePath);
		Element task = root;
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					task = tasks.get(i);
					break;
				}
		}
		Element output = task.getChild("Output");
		return output.getAttributeValue("File");
	}

	@SuppressWarnings("unchecked")
	public Vector<String> getAllOutputFile(String xmlFilePath) {

		Element root = getRoot(xmlFilePath);
		Vector<String> outputs = new Vector<String>();
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				outputs.add(tasks.get(i).getChild("Output").getAttributeValue(
						"File"));
		}
		return outputs;
	}

	@SuppressWarnings("unchecked")
	public Vector<String> getInputName(String xmlFilePath) {

		Element root = getRoot(xmlFilePath);
		Vector<String> inputs = new Vector<String>();
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			List<Element> inputsElement = root.getChildren("Input");
			for (int i = 0; i < inputsElement.size(); i++) {
				inputs.add(inputsElement.get(i).getAttributeValue("InputName"));
			}
		} else { // table.xml or deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					List<Element> inputsElement = tasks.get(i).getChildren(
							"Input");
					for (int j = 0; j < inputsElement.size(); j++) {
						inputs.add(inputsElement.get(j).getAttributeValue(
								"InputName"));
					}
					break;
				}
		}
		return inputs;
	}

	@SuppressWarnings("unchecked")
	public Vector<String> getInputFile(String path) {
		Vector<String> inputfiles = new Vector<String>();
		if (path.endsWith(".xml") || path.endsWith(".XML")) {
			Element root = getRoot(path);
			if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
				List<Element> predecessors = root.getChildren("Predecessor");
				for (int i = 0; i < predecessors.size(); i++) {
					inputfiles.add(predecessors.get(i).getAttributeValue(
							"Outputs"));
				}
			} else { // table.xml or deployment.xml
				Vector<TaskPastryGrid> pred = Pred(path);
				for (int i = 0; i < pred.size(); i++)
					inputfiles.add(pred.get(i).getOutputFile(path));
			}
		} else { // the path may be a directory
			File dir = new File(path);
			if (!dir.exists())
				return inputfiles;
			if (dir.isDirectory()) {
				String[] list = dir.list();
				if (list.length == 0)
					return inputfiles;
				Vector<String> listinputs = new Vector<String>(list.length);
				for (int i = 0; i < list.length; i++)
					listinputs.add(list[i]);
				return listinputs;
			}
		}
		return inputfiles;
	}

	@SuppressWarnings("unchecked")
	public Vector<TaskPastryGrid> Succ(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Vector<TaskPastryGrid> successors = new Vector<TaskPastryGrid>();
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			List<Element> tasks = root.getChildren("Successor");
			for (int i = 0; i < tasks.size(); i++) {
				successors.add(new TaskPastryGrid(tasks.get(i)
						.getAttributeValue("Description")));
			}
		} else { // table.xml
			String taskOutput = getOutputName(xmlFilePath);

			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++) {
				TaskPastryGrid task = new TaskPastryGrid(tasks.get(i)
						.getAttributeValue("Description"));
				Vector<String> inputs = task.getInputName(xmlFilePath);
				for (int j = 0; j < inputs.size(); j++)
					if (inputs.get(j).compareToIgnoreCase(taskOutput) == 0) {
						successors.add(task);
						break;
					}
			}
		}

		return successors;
	}

	@SuppressWarnings("unchecked")
	public Vector<TaskPastryGrid> getIsolatedSucc(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Vector<TaskPastryGrid> successors = new Vector<TaskPastryGrid>();
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			List<Element> tasks = root.getChildren("Successor");
			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i).getChildren("Shared").size() == 0)
					successors.add(new TaskPastryGrid(tasks.get(i)
							.getAttributeValue("Description")));
			}
		} else { // table.xml
			String taskOutput = getOutputName(xmlFilePath);

			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++) {
				TaskPastryGrid task = new TaskPastryGrid(tasks.get(i)
						.getAttributeValue("Description"));
				Vector<String> inputs = task.getInputName(xmlFilePath);
				for (int j = 0; j < inputs.size(); j++)
					if (inputs.get(j).compareToIgnoreCase(taskOutput) == 0) {
						successors.add(task);
						break;
					}
			}
		}

		return successors;
	}

	@SuppressWarnings("unchecked")
	public Vector<TaskPastryGrid> getSharedSucc(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Vector<TaskPastryGrid> successors = new Vector<TaskPastryGrid>();
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			List<Element> tasks = root.getChildren("Successor");
			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i).getChildren("Shared").size() > 0)
					successors.add(new TaskPastryGrid(tasks.get(i)
							.getAttributeValue("Description")));
			}
		} else { // table.xml
			String taskOutput = getOutputName(xmlFilePath);

			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++) {
				TaskPastryGrid task = new TaskPastryGrid(tasks.get(i)
						.getAttributeValue("Description"));
				Vector<String> inputs = task.getInputName(xmlFilePath);
				for (int j = 0; j < inputs.size(); j++)
					if (inputs.get(j).compareToIgnoreCase(taskOutput) == 0) {
						successors.add(task);
						break;
					}
			}
		}

		return successors;
	}

	@SuppressWarnings("unchecked")
	public Vector<TaskPastryGrid> Pred(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Vector<TaskPastryGrid> predecessors = new Vector<TaskPastryGrid>();
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			List<Element> tasks = root.getChildren("Predecessor");
			for (int i = 0; i < tasks.size(); i++) {
				predecessors.add(new TaskPastryGrid(tasks.get(i)
						.getAttributeValue("Description")));
			}
		} else { // table.xml
			Vector<String> inputs = getInputName(xmlFilePath);

			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int j = 0; j < inputs.size(); j++)
				for (int i = 0; i < tasks.size(); i++) {
					TaskPastryGrid task = new TaskPastryGrid(tasks.get(i)
							.getAttributeValue("Description"));
					String taskOutput = task.getOutputName(xmlFilePath);
					if (inputs.get(j).compareToIgnoreCase(taskOutput) == 0) {
						predecessors.add(task);
						break;
					}
				}
		}

		return predecessors;
	}

	@SuppressWarnings("unchecked")
	public Vector<TaskPastryGrid> Friend(String xmlFilePath,
			TaskPastryGrid SuccTask) {
		Element root = getRoot(xmlFilePath);
		Vector<TaskPastryGrid> friend = new Vector<TaskPastryGrid>();
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			List<Element> successors = root.getChildren("Successor");
			for (int i = 0; i < successors.size(); i++) {
				if (successors.get(i).getAttributeValue("Description")
						.compareToIgnoreCase(SuccTask.getName()) == 0) {
					List<Element> tasks = successors.get(i).getChildren(
							"Shared");
					for (int j = 0; j < tasks.size(); j++)
						friend.add(new TaskPastryGrid(tasks.get(j).getText()));
					break;
				}
			}
		} else { // table.xml
			Vector<TaskPastryGrid> predecessors = SuccTask.Pred(xmlFilePath);
			for (int i = 0; i < predecessors.size(); i++)
				if (this.getName().compareToIgnoreCase(
						predecessors.get(i).getName()) != 0)
					friend.add(predecessors.get(i));
		}

		return friend;
	}

	@SuppressWarnings("unchecked")
	public String getModule(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Element task = root;
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					task = tasks.get(i);
					break;
				}
		}
		return task.getAttributeValue("ApplicationModule");
	}

	@SuppressWarnings("unchecked")
	public String getFileIn(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Element task = root;
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					task = tasks.get(i);
					break;
				}
		}

		return task.getAttributeValue("FileIn");
	}

	@SuppressWarnings("unchecked")
	public String getDirIn(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Element task = root;
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					task = tasks.get(i);
					break;
				}
		}

		return task.getAttributeValue("DirIn");
	}

	@SuppressWarnings("unchecked")
	public String getApplication(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		Element task = root;
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					task = tasks.get(i);
					break;
				}
		}

		return task.getAttributeValue("Application");
	}

	@SuppressWarnings("unchecked")
	public String BinaryName(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			return root.getAttributeValue("BinaryExecutable");
		} else { // table.xml
			String module = getModule(xmlFilePath);
			List<Element> modules = root.getChild("Application").getChildren(
					"Module");
			for (int i = 0; i < modules.size(); i++)
				if (modules.get(i).getAttributeValue("ModuleDescription")
						.compareToIgnoreCase(module) == 0) {
					return modules.get(i).getChild("Binary").getAttributeValue(
							"BinaryExecutable");
				}
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public String BinaryDir(String xmlFilePath) {
		Element root = getRoot(xmlFilePath);
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml
			String module = getModule(xmlFilePath);
			List<Element> modules = root.getChild("Application").getChildren(
					"Module");
			for (int i = 0; i < modules.size(); i++)
				if (modules.get(i).getAttributeValue("ModuleDescription")
						.compareToIgnoreCase(module) == 0) {
					return modules.get(i).getAttributeValue("ModuleDirIn");
				}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Requirements getRequirements(String xmlFilePath) {

		Element root = getRoot(xmlFilePath);
		Short cpu = null, ram = null;
		String os = null;
		if (root.getName().compareToIgnoreCase("task") == 0) { // task.xml
			Element successor = root.getChild("Successor");
			Element requirements = successor.getChild("Requirements");
			cpu = Short.parseShort(requirements.getAttributeValue("CPU"));
			ram = Short.parseShort(requirements.getAttributeValue("RAM"));
			os = requirements.getAttributeValue("OS");
		} else { // table.xml
			String module = getModule(xmlFilePath);
			List<Element> modules = root.getChild("Application").getChildren(
					"Module");
			for (int i = 0; i < modules.size(); i++)
				if (modules.get(i).getAttributeValue("ModuleDescription")
						.compareToIgnoreCase(module) == 0) {
					Element requirements = modules.get(i).getChild(
							"caracteristique"); // "Requirements"
					cpu = Short.parseShort(requirements
							.getAttributeValue("Cpu")); // "CPU"
					ram = Short.parseShort(requirements
							.getAttributeValue("Ram")); // "RAM"
					os = requirements.getAttributeValue("Systeme"); // "OS"
				}
		}

		Requirements R = new Requirements(cpu, ram, os);
		return R;
	}

	@SuppressWarnings("unchecked")
	public String getCmdLine(String xmlFilePath) {

		Element root = getRoot(xmlFilePath);
		Element task = root;
		if (root.getName().compareToIgnoreCase("task") != 0) { // table.xml or
																// deployment.xml
			List<Element> tasks = root.getChild("Table").getChildren("Task");
			for (int i = 0; i < tasks.size(); i++)
				if (this.getName().compareToIgnoreCase(
						tasks.get(i).getAttributeValue("Description")) == 0) {
					task = tasks.get(i);
					break;
				}
		}
		Element cmd = task.getChild("cmdLine");
		return cmd.getText();
	}

	public static Element getRoot(String xmlFilePath) {

		if (!(new File(xmlFilePath)).exists()) {
			System.out.println("XmlError : " + xmlFilePath + " not found");
			return null;
		}

		Document document = null;
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(new File(xmlFilePath));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Element root = document.getRootElement();
		return root;
	}

	public Boolean isFriend(String xmlFilePath, TaskPastryGrid SuccTask) {
		if (this.Friend(xmlFilePath, SuccTask).size() == 0)
			return false;
		return true;
	}

	public void generateTaskXml(String xmlFilePath, String outFolder) {
		System.out.println("Creating Task.xml of " + this.getName());

		Element root = new Element("Task");
		Document document = new Document(root);

		root.setAttribute("Description", this.getName());
		root.setAttribute("Application", this.getApplication(xmlFilePath));
		root.setAttribute("ApplicationModule", this.getModule(xmlFilePath));
		root.setAttribute("FileIn", this.getFileIn(xmlFilePath));
		root.setAttribute("BinaryExecutable", this.BinaryName(xmlFilePath));

		Vector<String> inputs = this.getInputName(xmlFilePath);
		for (int i = 0; i < inputs.size(); i++) {
			Element input = new Element("Input");
			input.setAttribute("InputName", inputs.get(i));
			root.addContent(input);
		}

		inputs = this.getInputFile(xmlFilePath);
		Vector<TaskPastryGrid> preds = this.Pred(xmlFilePath);
		for (int i = 0; i < preds.size(); i++) {
			Element predecessor = new Element("Predecessor");
			predecessor.setAttribute("Description", preds.get(i).getName());
			predecessor.setAttribute("Outputs", inputs.get(i));
			root.addContent(predecessor);
		}

		Element output = new Element("Output");
		output.setAttribute("OutputName", this.getOutputName(xmlFilePath));
		output.setAttribute("File", this.getOutputFile(xmlFilePath));
		root.addContent(output);

		Vector<TaskPastryGrid> succ = this.Succ(xmlFilePath);
		for (int i = 0; i < succ.size(); i++) {
			Element successor = new Element("Successor");
			successor.setAttribute("Description", succ.get(i).getName());
			successor
					.setAttribute("FileIn", succ.get(i).getFileIn(xmlFilePath));
			successor.setAttribute("ApplicationModule", succ.get(i).getModule(
					xmlFilePath));
			Requirements R = succ.get(i).getRequirements(xmlFilePath);
			Element requirements = new Element("Requirements");
			requirements.setAttribute("CPU", R.CPU + "");
			requirements.setAttribute("RAM", R.RAM + "");
			requirements.setAttribute("OS", R.OS);
			successor.addContent(requirements);
			Vector<TaskPastryGrid> friend = this.Friend(xmlFilePath, succ
					.get(i));
			for (int j = 0; j < friend.size(); j++) {
				Element shared = new Element("Shared");
				shared.setText(friend.get(j).getName());
				successor.addContent(shared);
			}
			root.addContent(successor);
		}

		Element cmd = new Element("cmdLine");
		cmd.setText(this.getCmdLine(xmlFilePath));
		root.addContent(cmd);

		try {
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(outFolder
					+ "/Task.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String toString() {
		return name;
	}

	public void serialize(OutputBuffer buf) throws IOException {
		buf.writeUTF(name);
	}

	public static TaskPastryGrid readTaskPastryGrid(InputBuffer buf)
			throws IOException {
		String NAME = buf.readUTF();
		return new TaskPastryGrid(NAME);
	}

}
