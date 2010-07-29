package principal;

import zip.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.mpisws.p2p.filetransfer.FileReceipt;
import org.mpisws.p2p.filetransfer.FileTransfer;
import org.mpisws.p2p.filetransfer.FileTransferCallback;
import org.mpisws.p2p.filetransfer.FileTransferImpl;

import ftc.Replica;
import ftc.Supervisor;

import messages.*;

import rice.Continuation;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.commonapi.appsocket.AppSocket;
import rice.p2p.commonapi.appsocket.AppSocketReceiver;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.MessageDeserializer;
import rice.p2p.util.rawserialization.SimpleInputBuffer;
import rice.p2p.util.rawserialization.SimpleOutputBuffer;
import rice.pastry.Id;

public class ApplicationPastryGrid implements Application {

	public ApplicationPastryGrid App;
	public Endpoint endpoint;
	public NodePastryGrid NPG;
	public Boolean idle = true;
	public Replica past;
	public Supervisor supervisor;
	public Process process = null;
	public boolean stopped = false;
	// public NodeHandle nhRDV = null;
	public static String appXml = "table.xml";
	public FileTransfer fileTransfer;
	public boolean transfertComplete = false; 
	
	public ApplicationPastryGrid(NodePastryGrid NPG) {		
		this(NPG.node);
		this.App = this;
		this.NPG = NPG;
		this.idle = true;

		past = new Replica(this);
		supervisor = new Supervisor(this);

		String line = "Finished creating new application \"ApplicationPastryGrid\": "
				+ endpoint.getId();
		System.out.println(line);
		NPG.updateHistoryFile(NodePastryGrid.nodeDirectory + NPG.node.getId().hashCode()
				+ "/history", line);
	}

	public ApplicationPastryGrid(Node node) {
		endpoint = node.buildEndpoint(this, "ApplicationPastryGrid");
		endpoint.setDeserializer(new MessageDeserializer() {

			public Message deserialize(InputBuffer buf, short type,
					int priority, NodeHandle sender) throws IOException {
				switch (type) {
				case 1:
					return new InitRDV(buf, endpoint);
				case 2:
					return new GetApplication(buf, endpoint);
				case 3:
					return new MyApplication(buf, endpoint);
				case 4:
					return new InitFTC(buf, endpoint);
				case 5:
					return new WorkRequest(buf, endpoint);
				case 6:
					return new GetResult(buf, endpoint);
				case 7:
					return new MyResult(buf, endpoint);
				case 8:
					return new WorkRequestReject(buf, endpoint);
				case 9:
					return new DataRequest(buf, endpoint);
				case 10:
					return new TriggerSupervision(buf, endpoint);
				case 11:
					return new YourData(buf, endpoint);
				case 12:
					return new NodeFailed(buf, endpoint);
				case 13:
					return new SearchRequest(buf, endpoint);
				case 14:
					return new SearchRequestAck(buf, endpoint);
				case 15:
					return new SearchRequestReject(buf, endpoint);
				case 16:
					return new WorkDone(buf, endpoint);
				case 17:
					return new ApplicationReceived(buf, endpoint);
				case 18:
					return new StopWorking(buf, endpoint);
				case 19:
					return new MyApplicationResult(buf, endpoint);
				case 20:
					return new YourApplicationResult(buf, endpoint);
				case 21:
					return new YourApplicationResultNotYet(buf, endpoint);
				case 22:
					return new GetNodesExecutingTasks(buf, endpoint);
				case 23:
					return new NodesExecutingTasks(buf, endpoint);

				default:
					return null;
				}

			}

		});

		// example receiver interface
	    endpoint.accept(new AppSocketReceiver() {
	      /**
	       * When we accept a new socket.
	       */
	      public void receiveSocket(AppSocket socket) {
	        fileTransfer = new FileTransferImpl(socket,new FileTransferCallback() {
	        
	          public void messageReceived(ByteBuffer bb) {
	            System.out.println("Message received: "+bb);
	          }
	        
	          public void fileReceived(File f, ByteBuffer metadata) {
	            try {
	            	SimpleInputBuffer sib = new SimpleInputBuffer(metadata);
	              String originalFileName = sib.readUTF();
	              String destinatinationPath = "";//new SimpleInputBuffer(metadata).readUTF();
	              String folder = "";
	              short type = sib.readShort();
	              MessagePastryGrid message = null;
	              //System.out.println("*/* "+type);
	              switch (type) {
	              case 3:
	            	  message = new MyApplication(sib, endpoint);
	            	  folder = NodePastryGrid.rdvDirectory + message.appName + message.time;
	            	  destinatinationPath = NodePastryGrid.rdvDirectory+message.appName
	            	  							+ message.time + ".zip";
	            	  
	            	  break;				
					case 7:
						message = new MyResult(sib, endpoint);
						if (!((MyResult)message).toRDV){
							folder = NodePastryGrid.workDirectory + message.appName
        	 						+ message.time	+ "/" + WorkRequest.taskToExecute;
							destinatinationPath = folder + "/" + "output" + ((MyResult)message).task
													+ ".zip";
						}
						else{
							folder = NodePastryGrid.rdvDirectory + message.appName + message.time
										+ "/" + message.appName+ "/" + "results/";
							destinatinationPath = folder + "output" + ((MyResult)message).task
													+ ".zip";
						}
		            	 
		            	  break;				
					case 11:
						message = new YourData(sib, endpoint);
						folder = NodePastryGrid.workDirectory + message.appName + message.time + "/"
									+ ((YourData)message).task;	
						
		            	 destinatinationPath = NodePastryGrid.workDirectory + message.appName + message.time + "/" + ((YourData)message).task + ".zip";
		            	 break;	
					case 20:
						message = new YourApplicationResult(sib, endpoint);
						folder = NodePastryGrid.pastryDirectory;//.submissionDirectory + message.time;						
		            	destinatinationPath = folder+ "/Result"
						+ message.appName + message.time + ".zip";
		            	break;	
	              }
	              new File(folder).mkdirs();
	              //System.out.println(NodePastryGrid.rdvDirectory+destinatinationPath);
	              File dest = new File(destinatinationPath);
	              System.out.println("Moving "+f+" to "+dest+" original:"+originalFileName);
	              if(!f.renameTo(dest)){
	            	  dest.delete();
	            	  f.renameTo(dest);
	              }
	              Unzip.unzip(destinatinationPath, folder);
	              message.response(App);
	            } catch (IOException ioe) {
	              System.out.println("Error deserializing file name. "+ioe);
	            }
	          }
	        
	          public void receiveException(Exception ioe) {
	            //System.out.println("FTC.receiveException() "+ioe);
	          }
	        },NPG.node.getEnvironment());
	        
	        fileTransfer.addListener(new FileListenerPastryGrid());
	        
	        // it's critical to call this to be able to accept multiple times
	        endpoint.accept(this);
	      }    

	      /**
	       * Called when the socket is ready for reading or writing.
	       */
	      public void receiveSelectResult(AppSocket socket, boolean canRead, boolean canWrite) {
	        throw new RuntimeException("Shouldn't be called.");
	      }
	    
	      /**
	       * Called if we have a problem.
	       */
	      public void receiveException(AppSocket socket, Exception e) {
	        e.printStackTrace();
	      }    
	    });
		endpoint.register();

	}

	public void routeMyMsg(Message message, Id id) {
		endpoint.route(id, message, null);
	}

	public void routeMyMsgDirect(Message message, NodeHandle nh) {
		if (!NPG.node.getLocalNodeHandle().equals(nh))
			endpoint.route(null, message, nh);

		else {
			// System.out.println("Error : function \"routeMyMsgDirect\" can't route the message to the node it self ");
			response((MessagePastryGrid) message);
		}

	}

	 public void sendFile(final MessagePastryGrid message, NodeHandle nh) {
		 transfertComplete = false;
		    System.out.println(this.NPG.node.getId().toString()+" opening to "+nh.getId().toString());    
		    endpoint.connect(nh, new AppSocketReceiver() {
		      
		      /**
		       * Called when the socket comes available.
		       */
		      public void receiveSocket(AppSocket socket) {        
		        // create the FileTransfer object
		        FileTransfer sender = new FileTransferImpl(socket, null, NPG.node.getEnvironment());         
		        
		        // add the listener
		        sender.addListener(new FileListenerPastryGrid());
/*		       
		        // Create a simple 4 byte message
		        ByteBuffer sendMe = ByteBuffer.allocate(4);
		        sendMe.put((byte)1);
		        sendMe.put((byte)2);
		        sendMe.put((byte)3);
		        sendMe.put((byte)4);
		        
		        // required when using a byteBuffer to both read and write
		        sendMe.flip();
		        
		        // Send the message
		        System.out.println("Sending "+sendMe);        
		        sender.sendMsg(sendMe, (byte)1, null);
	*/	        
		        final short type = message.getType();
		        String path = "";
   
    
    switch (type) {
    case 3:
    	path = MyApplication.AppPath;break;				
		case 7:
			path = ((MyResult) message).filepath;break;				
		case 11:
			path = ((YourData) message).filepath;break;		
		case 20:
			path = ((YourApplicationResult) message).filepath;break;		
    }
    if(path.compareTo("") == 0){
    	System.err.println("the file's path to send is null");
    	System.exit(1);
    }
		        try {
		          // get the file
		          final File f = new File(path);
		          
		          // make sure it exists
		          if (!f.exists()) {
		            System.err.println("File "+f+" does not exist.  Please create a file called "+f+" and run the tutorial again.");
		            System.exit(1);
		          }
		          
		          // serialize the filename, the type of message & the message
		          SimpleOutputBuffer sob = new SimpleOutputBuffer();
		          sob.writeUTF(f.getName());	          
		          //sob.writeUTF(destinatinationPath);		          
		          sob.writeShort(message.getType());
		          message.serialize(sob);
		          // request transfer of the file with priority MAX_PRIORITY
		          sender.sendFile(f,sob.getByteBuffer(),FileTransferImpl.MAX_PRIORITY,new Continuation<FileReceipt, Exception>() {

		            public void receiveException(Exception exception) {
		              System.out.println("Error sending: "+f+" "+exception);
		              transfertComplete = true; 
		            }

		            public void receiveResult(FileReceipt result) {
		              System.out.println("Send complete: "+f.getName()+" - "+f.length());
		              if(type != 3)
		            	  f.delete();
		              transfertComplete = true; 
		              
		 /*             if(type == 7){
		            	//  String xmlFilePath = NodePastryGrid.workDirectory + message.appName + message.time + "/"
						//	+ ((MyResult) message).task + "/Task.xml";
		            	  //if(((MyResult) message).task.getSharedSucc(xmlFilePath).size() > 0){
			            	  String line = "Search request task: " + ((MyResult) message).task.getName();
								System.out.println(line);
								App.NPG.updateHistoryFile(NodePastryGrid.nodeDirectory
										+ App.NPG.node.getId().hashCode() + "/history",
										line);
								SearchRequest searchrequest = new SearchRequest(
										App.NPG.node.getLocalNodeHandle(), message.appName, message.time,
										((MyResult) message).task);
								/// message.from contien le nh de ce noeud et non du rdv donc 
								//soi on utilise routemymsg ou chercher une otre sol
								App.routeMyMsgDirect(searchrequest, /*message.from);
			            //  }
		              }
		      */      	  
		              
		             
		            }
		          });
		          
		        } catch (IOException ioe) {
		          ioe.printStackTrace();
		          transfertComplete = true; 
		        }
		      }    

		      /**
		       * Called if there is a problem.
		       */
		      public void receiveException(AppSocket socket, Exception e) {
		        e.printStackTrace();
		        transfertComplete = true; 
		      }
		      
		      /**
		       * Example of how to write some bytes
		       */
		      public void receiveSelectResult(AppSocket socket, boolean canRead, boolean canWrite) {   
		        throw new RuntimeException("Shouldn't be called.");
		      }
		    }, 30000);
		  }

	public void deliver(rice.p2p.commonapi.Id id, Message message) {
		//System.out.println(message.getClass());
		response((MessagePastryGrid) message);		
	}

	public void response(MessagePastryGrid message) {
		message.response(this);
	}

	public boolean forward(RouteMessage arg0) {
		return true;
	}

	public void update(NodeHandle arg0, boolean arg1) {
		// TODO Auto-generated method stub
	}

	public Boolean isIdle() {
		return idle;
	}

	@SuppressWarnings("unchecked")
	public Vector<TaskPastryGrid> getTasks(TaskPastryGrid task,
			String AppXmlPath) {
		if (task != null)
			return task.Succ(AppXmlPath);
		else {

			Vector<TaskPastryGrid> W = new Vector<TaskPastryGrid>();
			Document doc = null;
			SAXBuilder sxb = new SAXBuilder();
			try {
				doc = sxb.build(new File(AppXmlPath));
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Element root = doc.getRootElement();
			Element table = root.getChild("Table");
			List tasks = table.getChildren("Task");

			for (Iterator i = tasks.iterator(); i.hasNext();) {
				Element current = (Element) i.next();
				Element input = current.getChild("Input");
				if (input == null) {
					String taskName = current.getAttributeValue("Description");
					TaskPastryGrid TPG = new TaskPastryGrid(taskName);
					W.add(TPG);
				}

			}
			return W;
		}

	}

	public String getApplicationName(String AppXmlPath) {
		if (!(new File(AppXmlPath)).exists()) {
			System.out.println("XmlError : " + AppXmlPath + " not found");
			return null;
		}

		Document doc = null;
		SAXBuilder sxb = new SAXBuilder();
		try {
			doc = sxb.build(new File(AppXmlPath));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Element root = doc.getRootElement();
		Element app = root.getChild("Application");
		String appName = app.getAttributeValue("ApplicationDescription");
		return appName;
	}

	public String getMyOS() {
		String myOS = "";
		myOS = System.getProperty("os.name");
		if(myOS == null || myOS.compareTo("")==0)
			return myOS;
		
		String caracteristicsFilePath = NodePastryGrid.nodeDirectory
				+ NPG.node.getId().hashCode() + "/" + NPG.caracteristicsFile;
		if (!(new File(caracteristicsFilePath)).exists()) {
			System.out.println("XmlError : " + caracteristicsFilePath
					+ " not found");
			return null;
		}

		Document doc = null;
		SAXBuilder sxb = new SAXBuilder();
		try {
			doc = sxb.build(new File(caracteristicsFilePath));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Element root = doc.getRootElement();

		Element os = root.getChild("OS");
		myOS = os.getText();
		return myOS;
	}

	public Boolean verifyRequirements(Requirements R) {

		String caracteristicsFilePath = NodePastryGrid.nodeDirectory
				+ NPG.node.getId().hashCode() + "/" + NPG.caracteristicsFile;
		if (!(new File(caracteristicsFilePath)).exists()) {
			System.out.println("XmlError : " + caracteristicsFilePath
					+ " not found");
			return false;
		}

		Document doc = null;
		SAXBuilder sxb = new SAXBuilder();
		try {
			doc = sxb.build(new File(caracteristicsFilePath));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Element root = doc.getRootElement();

		Element cpu = root.getChild("CPU");
		int myCPU = Integer.parseInt(cpu.getText());

		Element ram = root.getChild("RAM");
		int myRAM = Integer.parseInt(ram.getText());

		Element os = root.getChild("OS");
		String myOS = os.getText();

		// System.out.println(myIP+" "+myCPU+" "+myRAM+" "+myOS);
		if (myCPU >= R.CPU && myRAM >= R.RAM && myOS.indexOf(R.OS) != -1)
			return true;
		else
			return false;
	}

	public void runApplication(String AppPath) {
		if (!new File(AppPath).exists()) {
			System.out.println(AppPath + " not found");
			return;
		}
		long currentTime = NPG.environment.getTimeSource().currentTimeMillis();
		final String appTime = Long.toString(currentTime);

		Unzip.unzipFile(AppPath, NodePastryGrid.submissionDirectory + appTime, appXml);
		final String appName = getApplicationName(NodePastryGrid.submissionDirectory
				+ appTime + "/" + appXml);
		// Zip.deleteDir(new File(NPG.applicationDirectory+timeApp));
		System.out.println("ApplicationHandle: " + appName + appTime);
		/*
		 * int p =0; while(p < 1){ NPG.sleep(20); LeafSet l=
		 * NPG.node.getLeafSet();
		 * System.out.println("inf = "+-l.ccwSize()+" ** sup= "+l.ccwSize());
		 * for (int i=-l.ccwSize(); i<=l.cwSize(); i++)
		 * System.out.println(l.get(i).getId().toStringFull()); p++; }
		 */
		final Id idRdv = Id.build(appName + appTime);
		final Id idFtc = Id.build(appName + appTime + "FTC");
		// System.out.println(NPG.node.getNodeId());
		// System.out.println(idRdv);
		// System.out.println(idFtc);
		final InitRDV initrdv = new InitRDV(NPG.node.getLocalNodeHandle(),
				appName, appTime);
		final InitFTC initftc = new InitFTC(NPG.node.getLocalNodeHandle(),
				appName, appTime);

		new Thread() {
			public void run() {
				System.out.println("Sending InitRDV to RDV...");
				routeMyMsg(initrdv, idRdv);
				// wait a sec
				NPG.sleep(1);
			}
		}.start();

		new Thread() {
			public void run() {
				System.out.println("Sending InitFTC to FTC...");
				routeMyMsg(initftc, idFtc);
				// wait a sec
				NPG.sleep(1);
			}
		}.start();

		MyApplication.AppPath = AppPath;

		/*
		 * new Thread() { public void run() { NPG.sleep(60*4);
		 * System.out.println("Demand Application's result"+appName +
		 * appTime+"..."); final MyApplicationResult myApplicationResult = new
		 * MyApplicationResult(NPG.node.getLocalNodeHandle(), appName, appTime);
		 * routeMyMsg(myApplicationResult, idRdv); } }.start();
		 */
	}

}
