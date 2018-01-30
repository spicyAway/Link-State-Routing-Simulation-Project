package main.java.socs.network.node;

//import main.java.socs.network.message.LSA;
//import main.java.socs.network.message.LinkDescription;
import main.java.socs.network.message.SOSPFPacket;
import main.java.socs.network.util.Configuration;

import java.net.*;
import java.io.*;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;

public class Router {

    protected LinkStateDatabase lsd;

    RouterDescription rd;

    //assuming that all routers are with 4 ports
    Link[] ports = new Link[4];

    public Router(Configuration config) {
    		rd = new RouterDescription(config.getString("socs.network.router.processIP"),
    				config.getShort("socs.network.router.processPort"),
    				config.getString("socs.network.router.ip"));
        lsd = new LinkStateDatabase(rd);

        System.out.println("My simulated IP address is: " + rd.simulatedIPAddress);
        
        //start server socket
        int port = rd.processPortNumber;
        try {
            new Thread(new Server(port, this)).start();
        } catch (IOException e) {
            System.out.print("Starting router server socket failed!");
        }
    }

    /**
     * output the shortest path to the given destination ip
     * <p/>
     * format: source ip address  -> ip address -> ... -> destination ip
     *
     * @param destinationIP the ip adderss of the destination simulated router
     */
    private void processDetect(String destinationIP) {

    }

    /**
     * disconnect with the router identified by the given destination ip address
     * Notice: this command should trigger the synchronization of database
     *
     * @param portNumber the port number which the link attaches at
     */
    private void processDisconnect(short portNumber) {

    }

    /**
     * attach the link to the remote router, which is identified by the given simulated ip;
     * to establish the connection via socket, you need to identify the process IP and process Port;
     * additionally, weight is the cost to transmitting data through the link
     * <p/>
     * NOTE: this command should not trigger link database synchronization
     */
    private void processAttach(String processIP, short processPort,
                               String simulatedIP, short weight) {
    	
    		addRouterToPorts(processIP, processPort, simulatedIP);
    	
    		/*int connectedPort = addRouterToPorts(processIP, processPort, simulatedIP);
    		if (connectedPort != -1) {
	        LinkDescription establishedLinkDescription = new LinkDescription(this.rd.simulatedIPAddress, connectedPort, weight);
	        LSA newLSA = lsd._store.get(this.rd.simulatedIPAddress);
	        newLSA.lsaSeqNumber++;
	        newLSA.links.add(establishedLinkDescription);
    		}*/
    }

	/*private void LDSysnchronization(int linkNumber, short weight){
      LinkDescription establishedLinkDescription = new LinkDescription(this.rd.simulatedIPAddress, linkNumber, weight);
      LSA newLSA = lsd._store.get(this.rd.simulatedIPAddress);
      newLSA.lsaSeqNumber++;
      newLSA.links.add(establishedLinkDescription);
      this.lsd._store.put(newLSA.linkStateID, newLSA);
	}*/

    /**
     * broadcast Hello to neighbors
     */
    private void processStart() {
        for (int i = 0; i < ports.length; i++) {
            if (ports[i] != null) {
            		//create HELLO message
                SOSPFPacket newPacket = new SOSPFPacket();
                newPacket.sospfType = 0;
                newPacket.srcProcessIP = this.rd.processIPAddress;
                newPacket.srcProcessPort = this.rd.processPortNumber;
                newPacket.neighborID = this.rd.simulatedIPAddress;

                String processIP = ports[i].router2.processIPAddress;
                int processPort = ports[i].router2.processPortNumber;
                new Thread(new Client(processIP, processPort, newPacket)).start();
            }

        }
    }

    /*public void LSAUPDATE(String initializer) {
        for (int i = 0; i < ports.length; i++) {
            if (ports[i] != null) {
                if (ports[i].router2.simulatedIPAddress != initializer) {
                    SOSPFPacket newPacket = new SOSPFPacket();
                    newPacket.srcProcessPort = this.rd.processPortNumber;
                    newPacket.srcProcessIP = this.rd.processIPAddress;
                    newPacket.srcIP = this.rd.simulatedIPAddress;
                    newPacket.dstIP = ports[i].router2.simulatedIPAddress;
                    newPacket.sospfType = 1;

                    Vector<LSA> lsaVector = new Vector<LSA>();
                    for (LSA lsa : lsd._store.values()) {
                        lsaVector.add(lsa);
                    }
                    newPacket.lsaArray = lsaVector;

                    String server = ports[i].router2.processIPAddress;
                    int port = ports[i].router2.processPortNumber;
                    //  new Thread(new Client(server, port, newPacket));
                }
            }
        }
    }*/


    /**
     * attach the link to the remote router, which is identified by the given simulated ip;
     * to establish the connection via socket, you need to identify the process IP and process Port;
     * additionally, weight is the cost to transmitting data through the link
     * <p/>
     * This command does trigger the link database synchronization
     */
    private void processConnect(String processIP, short processPort,
                                String simulatedIP, short weight) {

        processAttach(processIP, processPort, simulatedIP, weight);
        processStart();

    }

    /**
     * output the neighbors of the routers
     */
    private void processNeighbors() {
        System.out.print("My neighbours are: " + "\n");
        for (int i = 0; i < ports.length; i++) {
            if (ports[i] != null && ports[i].router2.status != null) {
                RouterDescription neighbour = ports[i].router2;
                System.out.print(neighbour.simulatedIPAddress + "\n");

            }
        }
    }

    /**
     * disconnect with all neighbors and quit the program
     */
    private void processQuit() {

    }

    public void terminal() {
        try {
            InputStreamReader isReader = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isReader);
            System.out.print(">> ");
            String command = br.readLine();
            while (true) {
                if (command.startsWith("detect ")) {
                    String[] cmdLine = command.split(" ");
                    processDetect(cmdLine[1]);
                } else if (command.startsWith("disconnect ")) {
                    String[] cmdLine = command.split(" ");
                    processDisconnect(Short.parseShort(cmdLine[1]));
                } else if (command.startsWith("quit")) {
                    processQuit();
                } else if (command.startsWith("attach ")) {
                    String[] cmdLine = command.split(" ");
                    processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                            cmdLine[3], Short.parseShort(cmdLine[4]));
                } else if (command.equals("start")) {
                    processStart();
                } else if (command.equals("connect ")) {
                    String[] cmdLine = command.split(" ");
                    processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                            cmdLine[3], Short.parseShort(cmdLine[4]));
                } else if (command.equals("neighbors")) {
                    //output neighbors
                    processNeighbors();
                } else {
                    //invalid command
                    break;
                }
                System.out.print(">> ");
                command = br.readLine();
            }
            isReader.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * multi-threaded router socket server
     */
    class Server implements Runnable {

        private ServerSocket serverSocket;
        Router router;

        public Server(int portNumber, Router router) throws IOException {
            serverSocket = new ServerSocket(portNumber);
            this.router = router;
            System.out.println("Opened server socket with IP: " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
        }

        public void run() {
            while (true) {
                try {
                		//accept incoming connections
                		Socket clientSocket = serverSocket.accept();
                		//start client service thread
                		ClientServiceThread clientThread = new ClientServiceThread(clientSocket, router);
                		clientThread.start();
                } catch (IOException e) {
                    System.out.print("Something wrong with the accept() function!");
                }
            }
        }
    }
    
    /**
     * find the port number that the specified router is attached to
     * @return the port number, -1 if no such port exists
     */
    public int findNeighborPort(String simulatedIP) {
	    	for (int i = 0; i < ports.length; i++) {
            if(this.ports[i] != null) {
                if (this.ports[i].router2.simulatedIPAddress.equals(simulatedIP)) {
                    return i;
                }
            }
        }
    		return -1;
    }
    
    /**
     * attach specified router to an empty port
     * @return the port number the router has been attached to, -1 if attach has failed
     */
    public int addRouterToPorts(String processIP, short processPort, String simulatedIP) {
    		
    		if (rd.simulatedIPAddress.equals(simulatedIP)) {
    			System.out.print("Cannot attach to self.\n");
    			return -1;
    		}
    		if (findNeighborPort(simulatedIP) != -1) {
    			System.out.print("Router has already been attached.\n");
    			return -1;
    		}
    		
    		//find an empty port
		int i = 0;
	    while (i < ports.length && ports[i] != null) {
	    		i++;
	    }
	
	    if (i == ports.length) {
	        System.out.print("No empty ports. Attach failed.\n");
	        return -1;
	    } else {
	    		//attach router to the empty port
	        	RouterDescription routerNeedToConnect = new RouterDescription(processIP, processPort, simulatedIP);
	        ports[i] = new Link(this.rd, routerNeedToConnect);
	        return i;
	    }

    }

    class ClientServiceThread extends Thread {
        Socket myClientSocket;
        Router router;

        ClientServiceThread(Socket s, Router router) {
            myClientSocket = s;
            this.router = router;
        }

        public void run() {

            ObjectInputStream in = null;
            ObjectOutputStream out = null;

            try {
                in = new ObjectInputStream(myClientSocket.getInputStream());
                out = new ObjectOutputStream(myClientSocket.getOutputStream());
                
                SOSPFPacket inputMessage = (SOSPFPacket) in.readObject();
                
                if (inputMessage.sospfType == 0) { 
                		//received HELLO message
                    System.out.print("received HELLO from " + inputMessage.neighborID + ";\n");
                    
                    if (findNeighborPort(inputMessage.neighborID) == -1) {
                    		//messaging router is not a neighbor, add it
                    		addRouterToPorts(inputMessage.srcProcessIP, inputMessage.srcProcessPort, inputMessage.neighborID);
                    	}
                    
                    int neighborPort = findNeighborPort(inputMessage.neighborID);
                    
                    if (neighborPort != -1) {
                    		//set messaging router status to INIT
                    		ports[neighborPort].router2.status = RouterStatus.INIT;
                    		System.out.println("set " + inputMessage.neighborID + " state to INIT;" + "\n");
                    		
                    		//send HELLO message to messaging router
                    		SOSPFPacket outputMessage = new SOSPFPacket();
                    		outputMessage.sospfType = 0;
                    		outputMessage.srcProcessIP = router.rd.processIPAddress;
                    		outputMessage.srcProcessPort = router.rd.processPortNumber;
                    		outputMessage.neighborID = router.rd.simulatedIPAddress;
                    		out.writeObject(outputMessage);
                    		
                    		inputMessage = (SOSPFPacket) in.readObject();
                    		
                    		if (inputMessage.sospfType == 0) {
                    			//received another HELLO message
                    			System.out.print("received HELLO from " + inputMessage.neighborID + ";\n");
                    			neighborPort = findNeighborPort(inputMessage.neighborID);
                    			if (neighborPort != -1) {
                    				//set status to TWO_WAY
                    				RouterStatus neighborStatus = ports[neighborPort].router2.status;
                    				if (neighborStatus == RouterStatus.INIT) {
                    					ports[neighborPort].router2.status = RouterStatus.TWO_WAY;
                    					System.out.println("set " + inputMessage.neighborID + " state to TWOWAY;" + "\n");
                    				} else if (neighborStatus == RouterStatus.TWO_WAY) {
                    					System.out.print("Already a TWOWAY neighbor with " + inputMessage.neighborID + ";\n");
                    				}
                    			}
                    		}
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
            		//close client service thread
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.out.print("couldn't close In/Output stream");
                }
                try {
                    myClientSocket.close();
                } catch (IOException e) {
                    System.out.print("couldn't close socket");
                }
            }
        }
    }


    class Client implements Runnable {
        private String processIP;
        private int processPort;
        private SOSPFPacket packet;

        public Client(String processIP, int processPort, SOSPFPacket packet) {
            this.processIP = processIP;
            this.processPort = processPort;
            this.packet = packet;
        }

        public void run() {
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            Socket client = null;
            try {
                client = new Socket(processIP, processPort);
                out = new ObjectOutputStream(client.getOutputStream());
                out.writeObject(this.packet);

                if (this.packet.sospfType == 0) {
                		//sent HELLO message
                		System.out.print("successfully forwarded the HELLO" + ";\n");
                		
                    in = new ObjectInputStream(client.getInputStream());
                    SOSPFPacket inputMessage = (SOSPFPacket) in.readObject();
                    if (inputMessage.sospfType == 0) {
                    		//received HELLO message
                        System.out.print("received HELLO from " + inputMessage.neighborID + ";\n");
                        
                        //set status to TWO_WAY
                        int neighborPort = findNeighborPort(inputMessage.neighborID);
                        if (ports[neighborPort].router2.status == RouterStatus.TWO_WAY) {
                        		System.out.print("Already a TWOWAY neighbor with " + inputMessage.neighborID + ";\n");
                        } else {
                        		ports[neighborPort].router2.status = RouterStatus.TWO_WAY;
                        		System.out.println("set " + inputMessage.neighborID + " state to TWO_WAY" + ";\n");
                        }
                    }
                    
                    //send another HELLO message
                    out.writeObject(packet);

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
            		//close client socket
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.out.print("couldn't close In/Output stream");
                }
                try {
                    client.close();
                } catch (IOException e) {
                    System.out.print("couldn't close socket");
                }
            }
        }
    }

}
