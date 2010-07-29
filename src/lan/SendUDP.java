package lan;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;



public class SendUDP extends Thread {
	String msg;
	int port;
	String ip;
	int i=0;
	public SendUDP(String ip, int port) {
		super();
		this.msg = "pg";
		this.port = port;
		this.ip = ip;
	}

	@Override
	public void run() {

		try {
			DatagramSocket socket = new DatagramSocket();
			InetAddress serverAddress = InetAddress.getByName(ip);
			//System.out.println("serverIP: "+serverAddress.toString());
			byte[] buffer = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);
			packet.setData(msg.getBytes());
			socket.send(packet);			
			socket.close();
			
		} catch (Exception e) {
		 System.out.println( "\nEnvoiUDP-Exception:\n");
		e.printStackTrace();
	}

	}

}
