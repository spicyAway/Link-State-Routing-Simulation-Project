package main.java.socs.network.node;

import main.java.socs.network.message.LSA;
import main.java.socs.network.message.LinkDescription;

import java.util.HashMap;
//import java.util.LinkedList;
import java.util.Map;
//import java.util.List;

public class LinkStateDatabase {

  //linkID => LSAInstance
  HashMap<String, LSA> _store = new HashMap<String, LSA>();
  Graph graph = null;

  private RouterDescription rd = null;

  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
  }

  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  String getShortestPath(String destinationIP) {
	  constructGraph();
	  
      Node source = this.graph.findNode(this.rd.simulatedIPAddress);
      Node dest = this.graph.findNode(destinationIP);
      
      if(source == null || dest == null){
    	      return null;
          //System.out.print("Source node or Destination node is not inside the graph!");
      }else{
          this.graph.calculateShortestPath(source);
          dest = this.graph.findNode(destinationIP);
          return dest.toStringShortestPath();
      }
  }

  //initialize the linkstate database by adding an entry about the router itself
  private LSA initLinkStateDatabase() {
    LSA lsa = new LSA();
    lsa.linkStateID = rd.simulatedIPAddress;
    lsa.lsaSeqNumber = Integer.MIN_VALUE;
    LinkDescription ld = new LinkDescription();
    ld.linkID = rd.simulatedIPAddress;
    ld.portNum = -1;
    ld.tosMetrics = 0;
    lsa.links.add(ld);
    return lsa;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa: _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
                append(ld.tosMetrics).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * construct a weighted graph from the LSAs in the database where the nodes
   * are the routers' simulated IP, the edges are the links between routers, and
   * the weight of the edge is the weight of the link
   */
  public void constructGraph(){
    Graph g = new Graph();
    for (Map.Entry<String, LSA> entry : this._store.entrySet()) {

        String linkSID = entry.getKey();
        LSA lsa = entry.getValue();
        
        Node n = g.findNode(linkSID);
        if (n == null) {
        		n = new Node(linkSID);
        		g.addNode(n);
        }

        //add edges to node for each link
        for (int i = 0; i < lsa.links.size(); i++) {
            LinkDescription tempLink = lsa.links.get(i);
            if (!n.getName().equals(tempLink.linkID)) {
            		Node neighbor = g.findNode(tempLink.linkID);
                if (neighbor == null) {
                		neighbor = new Node(tempLink.linkID);
                		g.addNode(neighbor);
                }
                n.addAdjacent(neighbor, tempLink.tosMetrics);
            }
        }
    }
    this.graph = g;
  }
}


