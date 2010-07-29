package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import rice.pastry.Id;
import rice.pastry.PastryNode;
import rice.pastry.leafset.LeafSet;

import messages.GetNodesExecutingTasks;
import messages.MyApplicationResult;
import messages.StopWorking;

public class Main {
	public static ApplicationPastryGrid App = null;
	public static InetAddress ext = null;
	public static int bindPort = 0;
	
	public static InetAddress bindAddress;

	public static void join() {		
    java.util.Random r = new java.util.Random();
		bindPort = r.nextInt(55535) + 10000;
		bindAddress = getIp();
		//System.out.println("bindPort = "+bindPort);
		NodePastryGrid NPG = new NodePastryGrid(bindAddress, bindPort);
		NPG.createEnvironment();
		NPG.createNode();
		NPG.createDirectories();
		NPG.saveDesktopCaracteristics();
		new multicastDiscovery(bindAddress, bindPort, NPG);

		App = new ApplicationPastryGrid(NPG);
		int sec = 10;
		System.out.println("Please wait "+sec+" seconds to fully starts up this node.");
		NPG.sleep(sec);
		System.out.println("Node joined successfully.");

	}

	public static int max(Vector<Integer> T){
		int max = T.get(0);
		for(int i=1; i<T.size();i++)
			if(max<T.get(i))
				max = T.get(i);
		return max;
	}


	public static boolean exist(int port, Vector<Integer> T){
		for(int i=0; i<T.size();i++)
			if(port == T.get(i))
				return true;
		return false;
	}


	public static InetAddress getIp(){
		try {
      InetAddress ip = InetAddress.getLocalHost();
      for(Enumeration nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
        NetworkInterface ni = (NetworkInterface) nis.nextElement();
        
        for(Enumeration ips = ni.getInetAddresses(); ips.hasMoreElements();) {
          ip = (InetAddress) ips.nextElement();
          String ipstr = ip.getHostAddress();
          System.out.println(ipstr);
          // Replace the 5. with whatever base network you want to bind with...
          if(ipstr.startsWith("5.")) {
            return ip;
          }
        }
      }
      return ip;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

	public static void runApplication(String appPath) {
		if (App == null) {
			System.out.println("you have to join ring first, then you can run your application");
		} else {
			if (new File(appPath).exists())
				App.runApplication(appPath);
			else
				System.out.println(appPath + " not found");
		}
	}

	public static void getNodesExecutingTasks(String appName, String appTime) {
		if (App == null) {
			System.out
					.println("you have to join ring first, then you can run your application");
		} else {
			Id idFtc = Id.build(appName + appTime + "FTC");
			GetNodesExecutingTasks getNodesExecutingTasks = new GetNodesExecutingTasks(
					App.NPG.node.getLocalNodeHandle(), appName, appTime);
			App.routeMyMsg(getNodesExecutingTasks, idFtc);
		}
	}

	public static void StopWorkingNode(String appName, String appTime,
			TaskPastryGrid task, String nodeId) {
		if (App == null) {
			System.out
					.println("you have to join ring first, then you can run your application");
		} else {
			Id idFtc = Id.build(appName + appTime + "FTC");
			StopWorking stopworking = new StopWorking(App.NPG.node
					.getLocalNodeHandle(), appName, appTime, task, true);
			App.routeMyMsg(stopworking, idFtc);
		}
	}

	public static void getApplicationResult(String appName, String appTime) {
		if (App == null) {
			System.out
					.println("you have to join ring first, then you can run your application");
		} else {
			Id idRdv = Id.build(appName + appTime);
			MyApplicationResult myApplicationResult = new MyApplicationResult(
					App.NPG.node.getLocalNodeHandle(), appName, appTime);
			App.routeMyMsg(myApplicationResult, idRdv);
		}
	}

	public static void displayMenu() {
		System.out.println("***** PastryGrid *****");
		System.out.println("   0) display menu");
		System.out.println("   1) join ring");
		System.out.println("   2) run application");
		System.out.println("   3) get list of nodes executing tasks");
		System.out.println("   4) stop working node");
		System.out.println("   5) get application result");
		System.out.println("   6) display neighbors");
		System.out.println("   7) disconnect node");
		System.out.println("   8) restart node");
		System.out.println("   9) usage");
		System.out.println("   q) exit");
		System.out.println("Type 1 or 2 or 3 etc.. or q to exit : ");
	}

	public static String read() {
		String line = "0";
		try {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			line = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	public static void getleafset(){
		LeafSet leafSet = ((PastryNode) App.NPG.node).getLeafSet();
		
			for (int i = -leafSet.ccwSize(); i <= leafSet.cwSize(); i++) {
				System.out.println(leafSet.get(i));
			}
	}
	
	public static void disconnect(){
		if(App == null || App.NPG == null)
			return;
		if(App.NPG.environment != null)
			App.NPG.environment.destroy();
		if(App.NPG.node != null)
			App.NPG.node.destroy();
		
		System.out.println("Node disconnected");		
	}
	
	public static void usage(){
		System.out.println("Usage: PastryGrid.jar -action");
		System.out.println("");
		System.out.println("where action include:");
		System.out.println("");
		System.out.println("\t -menu \t\t\t: to display menu");
		System.out.println("\t -join [appPath]\t: to join ring and (optional) run the specified application located in AppPath");
		System.out.println("\t -result AppName AppTime: to get the result of an application with handle (AppName+AppTime) ");
	}
	
	public static void actionMenu(String[] args){
		String choice = "";
		while (true) {
			choice = read();
			if (choice.compareToIgnoreCase("0") == 0) {
				displayMenu();
				continue;
			}
			if (choice.compareToIgnoreCase("1") == 0) {
				System.out.println("Joining ring...");
				join();
				continue;
			}
			if (choice.compareToIgnoreCase("2") == 0) {
				System.out.println("Running application.. ");
				System.out.println("get application path: ");
				String appPath = read();
				runApplication(appPath);
				continue;
			}
			if (choice.compareToIgnoreCase("3") == 0) {
				System.out.println("List of nodes executing tasks.. ");
				System.out.println("get application name: ");
				String appName = read();
				System.out.println("get application time: ");
				String appTime = read();
				getNodesExecutingTasks(appName, appTime);
				continue;
			}
			if (choice.compareToIgnoreCase("4") == 0) {
				System.out.println("Stop working node.. ");
				System.out.println("get application name: ");
				String appName = read();
				System.out.println("get application time: ");
				String appTime = read();
				System.out.println("get task name: ");
				TaskPastryGrid task = new TaskPastryGrid(read());
				System.out.println("get node id: ");
				String nodeId = read();
				Main.StopWorkingNode(appName, appTime, task, nodeId);
				continue;
			}
			if (choice.compareToIgnoreCase("5") == 0) {
				System.out.println("Application result.. ");
				System.out.println("get application name: ");
				String appName = read();
				System.out.println("get application time: ");
				String appTime = read();
				Main.getApplicationResult(appName, appTime);
				continue;
			}
			if (choice.compareToIgnoreCase("6") == 0) {
				System.out.println("Display neighbors.. ");				
				Main.getleafset();
				continue;
			}
			if (choice.compareToIgnoreCase("7") == 0) {
				System.out.println("Disconnecting from ring.. ");				
				Main.disconnect();
				continue;
			}
			if (choice.compareToIgnoreCase("8") == 0) {
				System.out.println("Restarting node.. ");				
				Main.disconnect();
				join();
				continue;
			}
			if (choice.compareToIgnoreCase("9") == 0) {			
				Main.usage();
				continue;
			}
			if (choice.compareToIgnoreCase("q") == 0 || choice.compareToIgnoreCase("exit") == 0) {
				disconnect();
				System.exit(0);
			}
			
			displayMenu();
		}
	}

	public static void actionResult(String[] args){
		System.out.println("Getting application's result.. ");
		String appName,appTime;
		if(args.length < 3){
			System.out.println("get application name: ");
			appName = read();
			System.out.println("get application time: ");
			appTime = read();
		}
		else{
			appName = args[1];
			appTime = args[2];
		}
		Main.getApplicationResult(appName, appTime);
		Main.actionMenu(args);
	}

	public static void actionJoin(String[] args){
		System.out.println("Joining ring...");
		join();
		if(args.length == 2){
			String appPath = args[1];
			System.out.println("Running application.. ");
			runApplication(appPath);
		}
		Main.actionMenu(args);
	}
	
	public static void main(String[] args) {
		
		if(args.length != 0){			
			if(args[0].compareToIgnoreCase("-join") == 0){
				Main.actionJoin(args);
			}			
			if(args[0].compareToIgnoreCase("-result") == 0){
				Main.actionResult(args);
			}
		}		

		if(args.length == 0 || args[0].compareToIgnoreCase("-menu") == 0){
			displayMenu();
			Main.actionMenu(args);
		}
	}

}
