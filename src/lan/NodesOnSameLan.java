package lan;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.Vector;

import principal.Main;

public class NodesOnSameLan {
	public static Vector<InetSocketAddress> nodes = new Vector<InetSocketAddress>();
	public static int bindPort = 5999;
	public static String ip;
	public static String broadcast = null;
	public static int NbSec = 5;
	public static Thread threadReceptionTCP = null;
	public static Thread threadReceptionUDP = null;
	public static Thread threadSendUDP = null;
	public NodesOnSameLan(int NbSec){
		if(NbSec < 0)
			NodesOnSameLan.NbSec = NbSec;
		getBroadcast();
		if(broadcast == null)
			return;
		threadReceptionTCP = new ReceptionTCP(bindPort);
		threadReceptionUDP = new ReceptionUDP(bindPort);
		threadSendUDP = new SendUDP(broadcast, bindPort);
		start();
		
        while(NodesOnSameLan.NbSec > 0 && nodes.size()<3)
        	try {
    			Thread.sleep(1000);
    			NodesOnSameLan.NbSec--;
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    		
    		new Thread() { 
    			public void run() {
    				while(NodesOnSameLan.NbSec > 0)
    		        	try {
    		    			Thread.sleep(1000);
    		    			NodesOnSameLan.NbSec--;
    		    		} catch (InterruptedException e) {
    		    			e.printStackTrace();
    		    		}
    			}
    		}.start();
     //   for(int i=0; i<nodes.size(); i++)
     //   	System.out.println(nodes.get(i).getAddress().getHostAddress()+":"+nodes.get(i).getPort());
		
	}
	
	public static void getBroadcast(){
		try {
			InetAddress localhost = Main.getInetAddress(); 
			
			if(localhost == null)
				return;
			ip = localhost.getHostAddress();
			//System.out.println(ip);
			if(ip.startsWith("127"))
				return;
			String OS = System.getProperty("os.name");
			System.out.println("your operating system is: "+OS);
			if (OS != null && OS.toLowerCase().indexOf("windows") != -1){
				NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost); 
				//System.out.println(networkInterface.toString());			
				
				//System.out.println(networkInterface.getInterfaceAddresses().size());
				List<InterfaceAddress> list = networkInterface.getInterfaceAddresses();
				InterfaceAddress nic = null;
				for(int i=0; i< list.size(); i++)
					if(list.get(i).getAddress().equals(localhost))
						nic = list.get(i);
				if(nic != null){
					//System.out.println(nic.toString());//+"\n"+nic.getAddress()+"\n"+nic.getBroadcast());
					broadcast = nic.getBroadcast().getHostAddress();
				}
				
			}
			else{
				broadcast = "255.255.255.255";
			}
			
		}catch (SocketException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		if(broadcast == null)
			broadcast = "255.255.255.255";
	}

	public static void start(){
		if(threadReceptionTCP == null)
			return;
        threadReceptionTCP.start();
        threadReceptionUDP.start();
        threadSendUDP.start();
	}
	

	@SuppressWarnings("deprecation")
	public static void stop(){
		if(threadReceptionTCP == null)
			return;
		
        threadReceptionTCP.stop();
        threadReceptionUDP.stop();
        threadSendUDP.stop();
        ReceptionTCP.close();
		ReceptionUDP.close();
	}
}
