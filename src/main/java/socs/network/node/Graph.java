package main.java.socs.network.node;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alicezhu on 25/2/2018.
 */
public class Graph {

    private Set<Node> nodes = new HashSet<>();

    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }

    public void printGraph(){
        System.out.print("Printing Lastest Graph: " + "\n");
        for (Node n : nodes) {
            System.out.println(n.getName());
            n.printNeighborNodes();
        }
    }

}
