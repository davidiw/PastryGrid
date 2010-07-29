package lan;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class Send extends Thread {
	String msg;
	int port;
	String ip;
	public Send(String msg, String ip, int port) {
		super();
		this.msg = msg;
		this.port = port;
		this.ip = ip;
	}

	@Override
	public void run() {
		Socket s;
		try {
			s = new Socket(ip, port);
			s.setSoTimeout(5000);			
			System.out.println("\n Sending "+msg+" ... \n");
			OutputStream oStream = s.getOutputStream();			
			BufferedOutputStream bOStream = new BufferedOutputStream(oStream);
			
			bOStream.write(msg.getBytes());
			bOStream.flush();
			bOStream.close();
			s.close();
			
		} catch (SocketTimeoutException e) {
			System.out.println( "\nSend timeout\n");
			e.printStackTrace();
		}catch (UnknownHostException e) {
			System.out.println( "\nSend-UnknownHostException\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println( "\nSend-IOException\n");
			e.printStackTrace();
		} catch (Exception e) {
			 System.out.println( "\nSend-Exception\n");
			e.printStackTrace();
		}
	 
		
		
	}

}
