package main.java.socs.network.node;

import com.sun.deploy.util.SessionState;
import main.java.socs.network.message.LSA;
import main.java.socs.network.message.LinkDescription;
import main.java.socs.network.message.SOSPFPacket;
import main.java.socs.network.util.Configuration;

import java.net.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;


public class Router {

    protected LinkStateDatabase lsd;

    RouterDescription rd = new RouterDescription();

    //assuming that all routers are with 4 ports
    Link[] ports = new Link[4];

    public Router(Configuration config) {
        rd.simulatedIPAddress = config.getString("socs.network.router.ip");
        rd.processPortNumber = config.getShort("socs.network.router.processPort");
        rd.processIPAddress = config.getString("socs.network.router.processIP");
        lsd = new LinkStateDatabase(rd);

        System.out.println("My simulated IP address is: " + rd.simulatedIPAddress);
        int port = rd.processPortNumber;
        try {
            new Thread(new routerServerSocket(port, this)).start();
        } catch (IOException e) {
            System.out.print("Start router server socket failed!");
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
     * to establish the connection via socket, you need to indentify the process IP and process Port;
     * additionally, weight is the cost to transmitting data through the link
     * <p/>
     * NOTE: this command should not trigger link database synchronization
     */
    private void processAttach(String processIP, short processPort,
                               String simulatedIP, short weight) {
        int i = 0;
        while (i < ports.length) {
            if (ports[i] == null) {
                RouterDescription routerNeedToConnect = new RouterDescription(processIP, processPort, simulatedIP);
                Link establishedLink = new Link(this.rd, routerNeedToConnect);
                ports[i] = establishedLink;

                LinkDescription establishedLinkDescription = new LinkDescription(this.rd.simulatedIPAddress, i, weight);
                LSA newLSA = lsd._store.get(this.rd.simulatedIPAddress);
                newLSA.lsaSeqNumber++;
                newLSA.links.add(establishedLinkDescription);

                break;
            }
            i++;
        }

        if (i == ports.length) {
            System.out.print("All the ports are fully connected. Failed to attach.");
        }
    }

 /* private void LDSysnchronization(int linkNumber, short weight){
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
                SOSPFPacket newPacket = new SOSPFPacket();
                newPacket.sospfType = 0;
                newPacket.srcProcessIP = this.rd.processIPAddress;
                newPacket.srcProcessPort = this.rd.processPortNumber;
                newPacket.neighborID = this.rd.simulatedIPAddress;

                String server = ports[i].router2.processIPAddress;
                int port = ports[i].router2.processPortNumber;
                new Thread(new Client(server, port, newPacket)).run();
            }

        }
        //LSAUPDATE(null);

    }

    public void LSAUPDATE(String initializer) {
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
    }


    /**
     * attach the link to the remote router, which is identified by the given simulated ip;
     * to establish the connection via socket, you need to indentify the process IP and process Port;
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
        System.out.print("Neighbours are: ");
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

    class routerServerSocket implements Runnable {

        private ServerSocket serverSocket;
        Router router;

        public routerServerSocket(int port, Router r) throws IOException {
            serverSocket = new ServerSocket(port);
            router = r;
            System.out.println("Opened a server socket with IP: " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
        }

        public void run() {
            while (true) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    System.out.print("Something wrong with the accept() function!");
                }
                ClientServiceThread cliThread = new ClientServiceThread(clientSocket, router);
                cliThread.start();
            }
        }
    }

    public RouterDescription findNeighbor(SOSPFPacket in) {
        String neighborID = in.neighborID;
        for (int i = 0; i < ports.length; i++) {
            if(this.ports[i] != null) {
                if (this.ports[i].router2.simulatedIPAddress.equals(neighborID)) {
                    return this.ports[i].router2;
                }
            }
        }
        System.out.print("No such neighbor lol");
        return null;
    }

    public boolean setINITstate(SOSPFPacket in) {

        String neighborID = in.neighborID;
        for (int i = 0; i < ports.length; i++) {
            if (this.ports[i].router2.simulatedIPAddress.equals(neighborID)) {
                this.ports[i].router2.status = RouterStatus.INIT;
                return true;
            }
        }

        return false;
    }

    public boolean setTWOWAYstate(SOSPFPacket in) {

        String neighborID = in.neighborID;
        for (int i = 0; i < ports.length; i++) {
            if (this.ports[i].router2.simulatedIPAddress.equals(neighborID)
                    && this.ports[i].router2.status == RouterStatus.INIT) {
                this.ports[i].router2.status = RouterStatus.TWO_WAY;
                return true;
            }
        }
        return false;
    }

    public void addNeighbor(String processIP, short processPort, String simulatedIP) {
        short i = 0;
        for (i = 0; i < ports.length; i++) {
            if (ports[i] == null) {
                RouterDescription remoteRouterDescription=new RouterDescription();
                remoteRouterDescription.simulatedIPAddress=simulatedIP;
                remoteRouterDescription.processIPAddress=processIP;
                remoteRouterDescription.processPortNumber=processPort;
                remoteRouterDescription.status=RouterStatus.INIT;
                ports[i]=new Link(this.rd,remoteRouterDescription);
                break;
            }
        }
    }


    class ClientServiceThread extends Thread {
        Socket myClientSocket;
        Router r;

        ClientServiceThread(Socket s, Router router) {
            myClientSocket = s;
            r = router;
        }

        public void run() {

            ObjectInputStream in = null;
            ObjectOutputStream out = null;

            try {
                in = new ObjectInputStream(myClientSocket.getInputStream());
                out = new ObjectOutputStream(myClientSocket.getOutputStream());
                SOSPFPacket inputMessage = (SOSPFPacket) in.readObject();

                if (inputMessage.sospfType == 0) { //This is a HELLO message
                    System.out.print("Received HELLO from : " + inputMessage.neighborID);
                    RouterDescription neighbor_rd = findNeighbor((inputMessage));
                    if (neighbor_rd == null) {
                        System.out.print("Received a HELLO from a non-existing neighbor!");
                        addNeighbor(inputMessage.srcProcessIP, inputMessage.srcProcessPort, inputMessage.neighborID);
                    } else {
                        if (setINITstate(inputMessage)) {
                            System.out.println("Set " + inputMessage.neighborID + " state to INIT");
                        }
                    }

                    SOSPFPacket outputMessage = new SOSPFPacket();
                    outputMessage.sospfType = 0;
                    outputMessage.srcProcessIP = r.rd.processIPAddress;
                    outputMessage.srcProcessPort = r.rd.processPortNumber;
                    outputMessage.neighborID = r.rd.simulatedIPAddress;
                    out.writeObject(outputMessage);

                    inputMessage = (SOSPFPacket) in.readObject();
                    if (inputMessage.sospfType == 0) { //This is a HELLO message
                        System.out.print("Received HELLO from : " + inputMessage.neighborID);
                        neighbor_rd = findNeighbor((inputMessage));
                        if (neighbor_rd == null) {
                            System.out.print("Received a HELLO from a non-existing neighbor!");
                            //add neighbor
                        } else {
                            if (setTWOWAYstate(inputMessage)) {
                                System.out.println("Set " + inputMessage.neighborID + " state to TWOWAY");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
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
        private String serverIP;
        private int port;
        private SOSPFPacket packet;

        public Client(String serverIP, int port, SOSPFPacket packet) {
            this.serverIP = serverIP;
            this.port = port;
            this.packet = packet;
        }

        public void run() {
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            Socket client = null;
            System.out.print("Here!!!");
            try {
                client = new Socket(serverIP, port);
                out = new ObjectOutputStream(client.getOutputStream());
                out.writeObject(this.packet);
                System.out.print("successfully forward the HELLO");

                if (this.packet.sospfType == 0) {
                    in = new ObjectInputStream(client.getInputStream());
                    SOSPFPacket inputMessage = (SOSPFPacket) in.readObject();

                    if (inputMessage.sospfType == 0) {
                        System.out.print("Received HELLO from : " + inputMessage.neighborID);
                        if (setINITstate(inputMessage)) {
                            if (setTWOWAYstate(inputMessage))
                                System.out.println("Set " + inputMessage.neighborID + " state to TWO_WAY");
                        }
                    }

                    out.writeObject(packet);

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {

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
