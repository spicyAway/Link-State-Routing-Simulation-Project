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
    startRouterServer();


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
    while( i < ports.length){
      if(ports[i] == null){
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

    if(i == ports.length){
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
      for(int i = 0; i < ports.length; i++) {
          if(ports[i] != null){
              SOSPFPacket newPacket = new SOSPFPacket();
              newPacket.sospfType = 0;
              newPacket.srcProcessIP = this.rd.processIPAddress;
              newPacket.srcProcessPort = this.rd.processPortNumber;
              newPacket.neighborID = this.rd.simulatedIPAddress;

              String server = ports[i].router2.processIPAddress;
              int port = ports[i].router2.processPortNumber;

              //new Thread(new Client(server, port, newPacket));
          }

      }
      LSAUPDATE(null);

  }

  public void LSAUPDATE(String initializer){
        for(int i = 0; i < ports.length; i++){
            if(ports[i] != null) {
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

        processAttach(processIP,processPort,simulatedIP,weight);
        processStart();

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
      System.out.print("Neighbours are: ");
      for(int i = 0; i < ports.length; i++){
          if(ports[i] != null && ports[i].router2.status != null){
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

  public void startRouterServer(){
      int port = rd.processPortNumber;
      new Thread(new routerServerSocket(port, this)).start();

  }

  public

}
