package lan;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceptionTCP extends Thread {
	String msg;
	int port;
	static ServerSocket server;

	public ReceptionTCP(int port) {
		super();
		this.port = port;
		System.out.println("\n Listening TCP ... \n\tplease wait "+NodesOnSameLan.NbSec+" secondes\n");
	}
	public static void close(){
		if(server.isClosed())
			return;
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(port);
			
			Socket client = server.accept();
			InputStream iStream = client.getInputStream();
			BufferedInputStream bIStream = new BufferedInputStream(iStream);

			byte[] b = new byte[1024];

			int bitsRecus = 0;

			String data;

			if((bitsRecus = bIStream.read(b)) >= 0){
				data = new String(b, 0, bitsRecus);
				System.out.println(data);
				String ip = data.substring(0, data.indexOf(":"));
				if(ip.compareTo(NodesOnSameLan.ip) != 0){
					String port = data.substring(data.indexOf(":")+1);
					NodesOnSameLan.nodes.add(new InetSocketAddress(ip,Integer.parseInt(port)));
					if(NodesOnSameLan.NbSec < 3)
						NodesOnSameLan.NbSec += 3; //we add 3 secs
				}
				
			}

			bIStream.close();
			client.close();
			server.close();
			run();
		} catch (Exception e) {
			System.out.println("\nReceptionTCP-Exception\n");
			e.printStackTrace();
		}

	}

}
