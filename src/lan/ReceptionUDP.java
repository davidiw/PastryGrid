package lan;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import principal.Main;

public class ReceptionUDP extends Thread {
	String msg;
	static int port;
	static DatagramSocket socket;
	public ReceptionUDP(int port) {
		super();
		this.msg = Main.getIp()+":"+Main.bindport;
		ReceptionUDP.port = port;
	}

	public static void close(){
		socket.close();
	}

	public static void open(){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		try {
			open();
			byte[] buffer = new byte[10];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (true) {
				socket.receive(packet);
				InetAddress clientAddress = packet.getAddress();
				//int portdest = packet.getPort();
				String data = new String(packet.getData(), 0, packet.getLength());
				if(data.length() > 0 && data.compareToIgnoreCase("pg") == 0){
					System.out.println("UDP received from : "
							+clientAddress.getHostAddress() + " : "	+ data);
					
					Thread threadSend = new Send(Main.getIp()+":"+Main.bindport, clientAddress.getHostAddress(), port);
			        threadSend.start();
				}

			}

		} catch (Exception e) {
			System.out.println("\nReceptionUDP-Exception:\n");
			e.printStackTrace();
		}

	}

}
