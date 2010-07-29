package principal;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import principal.Main;

public class multicastDiscovery extends Thread {
	public static int bindPort = 5999;
  public static String multicast = "230.230.230.152";
  public InetAddress pastryAddress;
  public int pastryPort;
  public NodePastryGrid npg;
	public Thread threadReceiveMulticast = null;

	public multicastDiscovery(InetAddress pastryAddress, int pastryPort, NodePastryGrid npg) {
    super();

    this.pastryAddress = pastryAddress;
    this.pastryPort = pastryPort;
    this.npg = npg;
		start();
	}
	
  @Override
	public void run() {
		try {
			MulticastSocket socket = new MulticastSocket(multicastDiscovery.bindPort);
      InetAddress group = InetAddress.getByName(multicastDiscovery.multicast);
      socket.setInterface(pastryAddress);
      socket.joinGroup(group);
      socket.setSoTimeout(60000);
      long next = 0;

			byte[] buffer = new byte[10];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			while (true) {
        if(npg.node.getLeafSet().size() < 15) {
          long current = (new Date()).getTime();
          if(next < current) {
            next = current + 60000;
		        sendMulticast(multicast, bindPort);
          }
        }
        try {
  				socket.receive(packet);
        } catch(java.io.InterruptedIOException e) {
          continue;
        }

				String data = new String(packet.getData(), 0, packet.getLength());
				InetAddress clientAddress = packet.getAddress();
				System.out.println("UDP received from : " +clientAddress.getHostAddress() + " : "	+ data);
        int port = Integer.parseInt(data.substring(2));
        if(data.charAt(0) == 'q') {
				  System.out.println("Sending response to: " + clientAddress.getHostAddress());
          sendUnicast(clientAddress.getHostAddress(), bindPort);
        } else if(data.charAt(0) != 'r') {
          continue;
        }

        System.out.println("Adding to bootstrap: " + clientAddress + ":" + port);
				npg.node.boot(new InetSocketAddress(clientAddress, port));
			}
		} catch (Exception e) {
			System.out.println("\nReceptionUDP-Exception:\n");
			e.printStackTrace();
		}
  }

  public void sendUnicast(String ip, int port) {
		try {
			DatagramSocket socket = new DatagramSocket();
			InetAddress destinationAddress = InetAddress.getByName(ip);
			byte[] buffer = ("r:" + Integer.toString(pastryPort)).getBytes();
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destinationAddress, port);
			packet.setData(buffer);
      socket.send(packet);
			socket.close();
		} catch (Exception e) {
		  System.out.println( "\nEnvoiUDP-Exception:\n");
		  e.printStackTrace();
    }
  }

  public void sendMulticast(String ip, int port) {
		try {
			MulticastSocket socket = new MulticastSocket();
      socket.setLoopbackMode(false);
      socket.setInterface(pastryAddress);
			InetAddress destinationAddress = InetAddress.getByName(ip);
			//System.out.println("serverIP: "+serverAddress.toString());
			byte[] buffer = ("q:" + Integer.toString(pastryPort)).getBytes();
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destinationAddress, port);
			packet.setData(buffer);
      System.out.println("Sending multicast query");
      socket.send(packet);
			socket.close();
		} catch (Exception e) {
		  System.out.println( "\nEnvoiUDP-Exception:\n");
		  e.printStackTrace();
	  }
  }
}
