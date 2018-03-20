package main.java.socs.network.node;

import main.java.socs.network.message.LSA;
import main.java.socs.network.message.LinkDescription;
import main.java.socs.network.message.SOSPFPacket;
import main.java.socs.network.util.Configuration;

import java.net.*;
import java.util.Vector;
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
    		String shortestPath = lsd.getShortestPath(destinationIP);
    		if (shortestPath == null) {
    			System.out.print("No path to " + destinationIP + ".\n");
    		} else {
    			System.out.print(shortestPath + "\n");
    		}
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
    	
    		addRouterToPorts(processIP, processPort, simulatedIP, weight);
    }

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
                newPacket.routerID = ports[i].router2.simulatedIPAddress;
                newPacket.linkWeight = ports[i].weight;

                //send HELLO message to router attached to port
                String processIP = ports[i].router2.processIPAddress;
                int processPort = (int) ports[i].router2.processPortNumber;
                new Thread(new Client(processIP, processPort, newPacket)).start();
            }
        }
    }

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
                if(neighbour.status == RouterStatus.TWO_WAY) {
                    System.out.print(neighbour.simulatedIPAddress + "\n");
                }
            }
        }
    }

    /**
     * disconnect with all neighbors and quit the program
     */
    private void processQuit() {
    		for (int i = 0; i < ports.length; i++) {
            if (ports[i] != null) {
            		processDisconnect((short) i);
            }
        }
    		System.exit(0);
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
    public int findRouterPort(String simulatedIP) {
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
    public int addRouterToPorts(String processIP, short processPort, String simulatedIP, short weight) {
    		
    		if (rd.simulatedIPAddress.equals(simulatedIP)) {
    			System.out.print("Cannot attach to self.\n");
    			return -1;
    		}
    		if (findRouterPort(simulatedIP) != -1) {
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
	        ports[i] = new Link(this.rd, routerNeedToConnect, weight);
	        return i;
	    }

    }
    
    /**
     * send link state update packet to all neighbors
     * @param excludedPort the port number of the neighbor excluded from the update, -1 if none
     */
    public void sendLSP(int excludedPort) {
		for (int i = 0; i < ports.length; i++) {
            if (i != excludedPort && ports[i] != null && ports[i].router2.status == RouterStatus.TWO_WAY) {
                	//create packet
            		SOSPFPacket lsp = new SOSPFPacket();
	        		lsp.sospfType = 1;
	        		lsp.srcIP = rd.simulatedIPAddress;
	        		lsp.lsaArray = new Vector<LSA>();
	        		for (LSA lsa : lsd._store.values()) {
	        			lsp.lsaArray.add(lsa);
	        		}
                	
	        		String processIP = ports[i].router2.processIPAddress;
	        		int processPort = (int) ports[i].router2.processPortNumber;
	        		new Thread(new Client(processIP, processPort, lsp)).start();
            }
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
                    
                    if (findRouterPort(inputMessage.neighborID) == -1) {
                    		//messaging router is not already attached to a port, add it
                    		addRouterToPorts(inputMessage.srcProcessIP, inputMessage.srcProcessPort, inputMessage.neighborID, inputMessage.linkWeight);
                    	}
                    
                    int routerPort = findRouterPort(inputMessage.neighborID);
                    
                    if (routerPort != -1) {
                    		//set messaging router status to INIT
                    		ports[routerPort].router2.status = RouterStatus.INIT;
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
                    			routerPort = findRouterPort(inputMessage.neighborID);
                    			if (routerPort != -1) {
                    				//set status to TWO_WAY
                    				ports[routerPort].router2.status = RouterStatus.TWO_WAY;
                					System.out.println("set " + inputMessage.neighborID + " state to TWOWAY;" + "\n");
                					
                					//add new link to LSA
                					LinkDescription newLinkDescription = new LinkDescription(inputMessage.neighborID, routerPort, ports[routerPort].weight);
                					LSA routerLSA = lsd._store.get(rd.simulatedIPAddress);
                					routerLSA.lsaSeqNumber++;
                					routerLSA.links.add(newLinkDescription);
                					//System.out.print(routerLSA.toString());
                					
                					sendLSP(-1);
                    			}
                    		}
                    }
                } else if (inputMessage.sospfType == 1) {
                		//System.out.print("original lsd:\n");
                		//System.out.print(lsd.toString());
                	
                		//received LSA update packet
                		System.out.print("received LSAUPDATE from " + inputMessage.srcIP + ";\n");
                		int neighborPort = findRouterPort(inputMessage.srcIP);
                		
                		for (LSA receivedLSA : inputMessage.lsaArray) {
                			LSA lsa = lsd._store.get(receivedLSA.linkStateID);
                			if (lsa == null || lsa.lsaSeqNumber < receivedLSA.lsaSeqNumber) {
            					//update database with newer LSA
            					lsd._store.put(receivedLSA.linkStateID, receivedLSA);
            					//lsd.constructGraph();
            					
            					//update all neighbors except neighbor that sent the LSP
            					sendLSP(neighborPort);
            				}
                		}
                		
                		//System.out.print("new lsd:\n");
                		//System.out.print(lsd.toString());
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
            //ObjectInputStream in = null;
            ObjectOutputStream out = null;
            Socket client = null;
            try {
            		client = new Socket(processIP, processPort);
                out = new ObjectOutputStream(client.getOutputStream());
                out.writeObject(this.packet);

                if (this.packet.sospfType == 0) {
                		//sent HELLO message
                		//System.out.print("successfully forwarded the HELLO" + ";\n");
                    ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                    SOSPFPacket inputMessage = (SOSPFPacket) in.readObject();
                    if (inputMessage.sospfType == 0) {
                    		//received HELLO message
                        System.out.print("received HELLO from " + inputMessage.neighborID + ";\n");
                        
                        //set status to TWO_WAY
                        int neighborPort = findRouterPort(inputMessage.neighborID);
                    		ports[neighborPort].router2.status = RouterStatus.TWO_WAY;
                    		System.out.println("set " + inputMessage.neighborID + " state to TWO_WAY" + ";\n");
                    		
                    		//send another HELLO message
        					out.writeObject(packet);
        					
        					//add new link to LSA
        					LinkDescription newLinkDescription = new LinkDescription(inputMessage.neighborID, neighborPort, ports[neighborPort].weight);
        					LSA routerLSA = lsd._store.get(rd.simulatedIPAddress);
        					routerLSA.lsaSeqNumber++;
        					routerLSA.links.add(newLinkDescription);
        					//System.out.print(routerLSA.toString());
        					
        					sendLSP(-1);
                    }
                    in.close();
                }
                //out.close();
            } catch (EOFException e) {
            		System.out.print("No message back!");
            		int port = findRouterPort(packet.routerID);
            		ports[port] = null;
            } catch (Exception e) {
            		e.printStackTrace();
            /*} catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();   	*/
            } finally {
            		//close client socket
                try {
                    //in.close();
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
