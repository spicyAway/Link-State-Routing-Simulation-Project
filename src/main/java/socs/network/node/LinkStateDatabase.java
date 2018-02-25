package main.java.socs.network.node;

import main.java.socs.network.message.LSA;
import main.java.socs.network.message.LinkDescription;

import java.util.HashMap;
import java.util.Map;

public class LinkStateDatabase {

  //linkID => LSAInstance
  HashMap<String, LSA> _store = new HashMap<String, LSA>();
  Graph graph = new Graph();

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
    //TODO: fill the implementation here
    return null;
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

  public void constructGraph(){

    Graph g = new Graph();
    for (Map.Entry<String, LSA> entry : this._store.entrySet()) {

        String linkSID = entry.getKey();
        LSA lsa = entry.getValue();
        Node n = new Node(linkSID);

        for (int i = 0; i < lsa.links.size(); i++) {
            LinkDescription tempLink = lsa.links.get(i);
            Node neighbor = new Node(tempLink.linkID);
            if (neighbor.getName() != n.getName()) {
                n.addAdjacent(neighbor, tempLink.tosMetrics);
            }
        }
        g.addNode(n);
    }
    this.graph = g;
    this.graph.printGraph();
  }
}


