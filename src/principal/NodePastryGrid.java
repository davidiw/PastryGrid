package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.management.OperatingSystemMXBean;

import rice.environment.Environment;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import zip.Unzip;
import zip.Zip;

public class NodePastryGrid {
	public PastryNode node = null;
	public Environment environment = null;
	public static String pastryDirectory = System.getProperty("user.home")
			+ "/PastryGrid/";
	public static String nodeDirectory = pastryDirectory + "node/";
	public static String workDirectory = pastryDirectory + "worker/";
	public static String submissionDirectory = pastryDirectory + "submission/";
	public static String rdvDirectory = pastryDirectory + "rdv/";
	public static String rdvResultsDirectory = pastryDirectory + "rdvResults/";
	public static String ftcDirectory = pastryDirectory + "ftc/";
	public String caracteristicsFile = "characteristics.xml";
	public Boolean submissionNodeCanWork = true;
	public int bindport;
	public InetSocketAddress bootaddress;
	public static List<InetSocketAddress> bootstrapAddresses = new ArrayList<InetSocketAddress>();

	public NodePastryGrid(int bindport, InetSocketAddress bootaddress) {
		super();

		this.bindport = bindport;
		this.bootaddress = bootaddress;
	}

	public void setBootaddress(InetSocketAddress bootaddress) {
		this.bootaddress = bootaddress;
	}
	
	public void createEnvironment() {
		environment = new Environment();
		environment.getParameters().setString("nat_network_prefixes", "10.;192.168.");
		environment.getParameters().setString("nat_search_policy", "never");
		environment.getParameters().setString("nat_app_name", "PastryGrid");

		System.out.println("Finished creating new environment ");
	}

	public boolean createNode() {
		
		try{		    
			NodeIdFactory nidFactory = new RandomNodeIdFactory(environment);
			SocketPastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, environment);
			
			int i = 0;
			int maxAttempts = 30;
			int attempts = 0;
			NodeHandle bootHandle = null;
			boolean success = false;
			while(!success && i<=20){ //test twice for every bootstrap				
				try{					
					bootHandle = factory.getNodeHandle(Main.getBootaddress((int)Math.floor(i/2)));
					i++;
					System.out.println(i+") "+bootHandle);
					node = factory.newNode(bootHandle);
					synchronized (node) {
						while (!node.isReady() && !node.joinFailed() && attempts < maxAttempts) {
							attempts++;
							if(attempts == maxAttempts)
								throw new Exception("timeout");
							node.wait(500L);
							if (node.joinFailed()) {
								throw new IOException(
										"Could not join the PastryGrid ring.  Reason: "
												+ node.joinFailedReason());
							}
						}						
					}
					success = true;
				}catch(Exception e){
						System.err.println("Joining ring failed from node "+node+" to bootstrap "+bootHandle);
						node.destroy();
						factory = new SocketPastryNodeFactory(nidFactory, bindport, environment);
				}
			}
			if(i > 20){
				System.err.println("Joining ring failed. Node couldn't connect");
				System.exit(1);
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
			//System.exit(1);
		}
		System.out.println("Finished creating new node " + node.getId().toStringFull()+"\n"+node);
		return true;
	}

	public void createDirectories() {
		File work = new File(workDirectory);
		File rdv = new File(rdvDirectory);
		File ftc = new File(ftcDirectory);
		File nd = new File(nodeDirectory);
		System.out.println("Deleting old folders :\n\t"+work.getName()+"\n\t"+rdv.getName()
				+"\n\t"+ftc.getName()+"\n\t"+nd.getName());
		Zip.deleteDir(work);
		Zip.deleteDir(rdv);
		Zip.deleteDir(ftc);
		Zip.deleteDir(nd);

		boolean success;
		if (!(new File(nodeDirectory)).exists())
			success = (new File(nodeDirectory)).mkdirs();
		else
			success = true;

		if (success) {
			File dir = new File(nodeDirectory + node.getId().hashCode());
			if (!dir.exists())
				success = dir.mkdirs();
			else
				success = true;
			if (success) {
				File history = new File(nodeDirectory + node.getId().hashCode()
						+ "/history");
				try {
					history.createNewFile();
					RandomAccessFile donnee = new RandomAccessFile(history,
							"rw");
					donnee.writeUTF("Node: " + node.toString() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Error: could not create \"" + nodeDirectory
						+ node.getId().hashCode()
						+ "\", due to access restriction.");
				System.exit(2);
			}

		} else {
			System.out.println("Error: could not create \"" + nodeDirectory
					+ "\", due to access restriction.");
			System.exit(2);
		}

		if (!(new File(workDirectory)).exists())
			success = (new File(workDirectory)).mkdirs();
		else
			success = true;

		if (!success) {
			System.out.println("Error: could not create \"" + workDirectory
					+ "\", due to access restriction.");
			System.exit(2);
		}

		if (!(new File(submissionDirectory)).exists())
			new File(submissionDirectory).mkdirs();

		if (!(new File(rdvDirectory)).exists())
			new File(rdvDirectory).mkdirs();

		if (!(new File(rdvResultsDirectory)).exists())
			new File(rdvResultsDirectory).mkdirs();

		if (!(new File(ftcDirectory)).exists())
			new File(ftcDirectory).mkdirs();

		String line = "Creation of files and directories completed successfully: \""
				+ nodeDirectory + node.getId().hashCode() + "\".";
		System.out.println(line);
		updateHistoryFile(nodeDirectory + node.getId().hashCode() + "/history",
				line);

	}

	public boolean runCpuz() {		
		if(System.getProperty("os.name").toLowerCase().indexOf("windows") == -1)
			return false;
		try {
			File directory = new File("");
			String path = System.getProperty("java.class.path");
		    if(path.indexOf(";")!=-1)
		    	path = path.substring(0, path.indexOf(";"));
		    String dirPath = directory.getAbsolutePath();		    
		    
		    if(!dirPath.endsWith("/") && !dirPath.endsWith("\\"))
		    	dirPath = dirPath+"/";

		    //System.out.println(path);
			if(path.endsWith(".jar")){
				path = dirPath+path;
				Unzip.extractFromJar(path, NodePastryGrid.nodeDirectory, "cpuz.exe");
				path = NodePastryGrid.nodeDirectory;
			}
			else
				path = path + "/cpu/";
			//System.out.println(path);

			String cmd = path + "cpuz.exe -txt=pastrygrid";
			File cpuDir = new File(path);
			// System.out.println(directory.getAbsolutePath());
			Process process;
			Runtime runtime = Runtime.getRuntime();
			process = runtime.exec(cmd, null, cpuDir);
			InputStream in = process.getInputStream();
			// OutputStream out = App.process.getOutputStream();
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

			InputStream err = process.getErrorStream();
			isr = new InputStreamReader(err);
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			process.waitFor();
			process.destroy();

			// extract informations
			File file = new File(path + "/pastrygrid.txt");
			String ip = "";
			int nbprocessors = 0;
			int nbcores = 0;
			int cpuspeed = 0;
			String cpuname = "";
			int memsize = 0;
			String manufacturer = "";
			String product = "";
			String graphics = "";
			int memgraphics = 0;
			String os = "";
			int pos1 = -1, pos2 = -1;
			if (file.exists()) {
				in = new FileInputStream(file);
				isr = new InputStreamReader(in);
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					if (line.indexOf("Number of processors") != -1) {
						System.out.println(line);
						pos1 = line.lastIndexOf("\t");
						if (pos1 != -1)
							nbprocessors = Integer.parseInt(line
									.substring(pos1 + 1));
					}
					if (line.indexOf("Number of cores") != -1) {
						System.out.println(line);
						pos1 = line.lastIndexOf("\t");
						pos2 = line.lastIndexOf(" (");
						if (pos1 != -1 && pos2 != -1)
							nbcores = Integer.parseInt(line.substring(pos1 + 1,
									pos2));
					}
					if (line.indexOf("Specification") != -1) {
						System.out.println(line);
						pos1 = line.lastIndexOf("\t");
						if (pos1 != -1)
							cpuname = line.substring(pos1 + 1);
					}
					if (line.indexOf("Stock frequency") != -1) {
						System.out.println(line);
						pos1 = line.lastIndexOf("\t");
						pos2 = line.lastIndexOf(" MHz");
						if (pos1 != -1 && pos2 != -1)
							cpuspeed = Integer.parseInt(line.substring(
									pos1 + 1, pos2));
					}
					if (line.indexOf("Memory Size") != -1) {
						System.out.println(line);
						pos1 = line.lastIndexOf("\t");
						pos2 = line.lastIndexOf(" MBytes");
						if (pos1 != -1 && pos2 != -1)
							memsize = Integer.parseInt(line.substring(pos1 + 1,
									pos2));
					}
					if (line.indexOf("DMI System Information") != -1) {
						System.out.println(line);
						line = br.readLine();
						if (line != null && line.indexOf("manufacturer") != -1) {
							System.out.println(line);
							pos1 = line.lastIndexOf("\t");
							if (pos1 != -1)
								manufacturer = line.substring(pos1 + 1);
						}
						pos1 = -1;
						line = br.readLine();
						if (line != null && line.indexOf("product") != -1) {
							System.out.println(line);
							pos1 = line.lastIndexOf("\t");
							if (pos1 != -1)
								product = line.substring(pos1 + 1);
						}
					}
					if (line.indexOf("Display adapter ") != -1) {
						line = br.readLine();
						line = br.readLine();
						line = br.readLine();
						if (line != null && line.indexOf("Name") != -1) {
							System.out.println(line);
							pos1 = line.lastIndexOf("\t");
							if (pos1 != -1)
								graphics = line.substring(pos1 + 1);
						}
						pos1 = -1;
						line = br.readLine();
						line = br.readLine();
						if (line != null && line.indexOf("Memory size") != -1) {
							System.out.println(line);
							pos1 = line.lastIndexOf("\t");
							pos2 = line.lastIndexOf(" MB");
							if (pos1 != -1 && pos2 != -1)
								memgraphics = Integer.parseInt(line.substring(
										pos1 + 1, pos2));
						}
					}
					if (line.indexOf("Windows Version") != -1) {
						System.out.println(line);
						pos1 = line.lastIndexOf("\t");
						if (pos1 != -1)
							os = line.substring(pos1 + 1);

						break;
					}
					pos1 = -1;
					pos2 = -1;
				}
				br.close();

				ip = InetAddress.getLocalHost().getHostAddress();

				Element root = new Element("Desktop");
				root.setAttribute("manufacturer", manufacturer);
				root.setAttribute("product", product);
				Document document = new Document(root);

				Element myIP = new Element("IP");
				root.addContent(myIP);
				myIP.setText(ip);

				Element myCPU = new Element("CPU");
				myCPU.setAttribute("specification", cpuname);
				myCPU.setAttribute("processors", nbprocessors + "");
				myCPU.setAttribute("cores", nbcores + "");
				root.addContent(myCPU);
				myCPU.setText(cpuspeed + "");

				Element myVGA = new Element("VGA");
				myVGA.setAttribute("memory", memgraphics + "");
				root.addContent(myVGA);
				myVGA.setText(graphics + "");

				Element myRAM = new Element("RAM");
				root.addContent(myRAM);
				myRAM.setText(memsize + "");

				Element myOS = new Element("OS");
				root.addContent(myOS);
				myOS.setText(os);
				XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
				sortie.output(document, new FileOutputStream(nodeDirectory
						+ node.getId().hashCode() + "/" + caracteristicsFile));
				return true;
			}

		} catch (Exception e) {
			System.out.println("Execution Error: \n" + e.toString());
			return false;
		}
		return false;
	}

	public boolean javaCharacteristics() {
		try {
			String ip = "";
			String cpu = "3000";
			String ram = "500";
			String os = "";

			int nbprocessors = 1;
			int nbcores = 1;
			String cpumodel = "";
			
			ip = Main.getIp();

			try{
				OperatingSystemMXBean composantSystem = (OperatingSystemMXBean) ManagementFactory
							.getOperatingSystemMXBean();
				Long memoryRAM = new Long(composantSystem.getTotalPhysicalMemorySize() / 1024L);
				ram = memoryRAM.toString();
				os = new String(composantSystem.getName());
			}catch(Exception e){}
			
			if(os == null || os.compareTo("")==0)
				os = System.getProperty("os.name");

			if(new File("/proc/cpuinfo").exists()){
				String cmd = "/bin/cat /proc/cpuinfo";
				Process process;
				Runtime runtime = Runtime.getRuntime();
				process = runtime.exec(cmd, null, new File("/bin"));
				InputStream in = process.getInputStream();
				// OutputStream out = App.process.getOutputStream();
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);
				String line;

				int pos = -1;
				int cpuspeed = 0;
				while ((line = br.readLine()) != null) {
					//System.out.println(line);
					if (line.indexOf("model name") != -1) {
						System.out.println(line);
						pos = line.indexOf(":");
						if (pos != -1)
							cpumodel = line.substring(pos + 1);
					}
					if (line.indexOf("cpu MHz") != -1) {
						System.out.println(line);
						pos = line.indexOf(":");
						if (pos != -1){
							cpuspeed = Math.round(Float.parseFloat(line.substring(pos + 1)));
							cpu = cpuspeed+"";
						}
					}
					if (line.indexOf("cpu cores") != -1) {
						System.out.println(line);
						pos = line.indexOf(":");
						if (pos != -1)
							nbcores = Integer.parseInt(line.substring(pos + 2));
						break;
					}
				}

				process.waitFor();
				process.destroy();
			}

			Element root = new Element("Desktop");
			Document document = new Document(root);

			Element myIP = new Element("IP");
			root.addContent(myIP);
			myIP.setText(ip);

			Element myCPU = new Element("CPU");
			myCPU.setAttribute("specification", cpumodel);
			myCPU.setAttribute("processors", nbprocessors + "");
			myCPU.setAttribute("cores", nbcores + "");
			root.addContent(myCPU);
			myCPU.setText(cpu);

			Element myRAM = new Element("RAM");
			root.addContent(myRAM);
			myRAM.setText(ram);

			Element myOS = new Element("OS");
			root.addContent(myOS);
			myOS.setText(os);
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(nodeDirectory
					+ node.getId().hashCode() + "/" + caracteristicsFile));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void saveDesktopCaracteristics() {
		new File(NodePastryGrid.nodeDirectory).mkdirs();
		System.out.println("Retrieving informations about your PC ..");
		if (!runCpuz())
			if (!javaCharacteristics()) {
				System.out.println("Error retrieving characteristics");
				System.exit(1);
			}
		String line = "Finished creating " + caracteristicsFile;
		System.out.println(line);
		updateHistoryFile(nodeDirectory + node.getId().hashCode() + "/history",
				line);

	}

	public void sleep(int nbSeconds) {
		try {
			environment.getTimeSource().sleep(nbSeconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String getTime() {
		GregorianCalendar calendar = new GregorianCalendar();
		Date time = new Date();
		calendar.setTime(time);
		return DateFormat.getTimeInstance().format(time);
	}

	public void updateHistoryFile(String historyFilePath, String line) {
		String time = getTime();
		File history = new File(historyFilePath);
		try {
			RandomAccessFile donnee = new RandomAccessFile(history, "rw");
			donnee.seek(donnee.length());
			donnee.writeUTF("[" + time + "] :" + line + "\n\r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
