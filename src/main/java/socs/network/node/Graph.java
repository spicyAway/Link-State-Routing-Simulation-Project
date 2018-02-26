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
    public Node findNode(String Name){
        for(Node node : this.nodes){
            if(node.getName() == Name){
                return node;
            }
        }
        return null;
    }

    public static Graph calculateShortestPath(Graph graph, Node source){
            source.setDistance(0);
            Set<Node> settledNodes = new HashSet<>();
            Set<Node> unsettledNodes = new HashSet<>();
            unsettledNodes.add(source);

            while(unsettledNodes.size() != 0){
                Node currentNode = getLowestDistanceNode(unsettledNodes);
                unsettledNodes.remove(currentNode);
                for(Map.Entry<Node, Integer> adjacentNodePair : currentNode.getAdjacentNodes().entrySet()){
                    Node adjacentNode = adjacentNodePair.getKey();
                    int distance = adjacentNodePair.getValue();

                    if(!unsettledNodes.contains(adjacentNode)){
                        calculateMinimumDistance(adjacentNode, distance, currentNode);
                        unsettledNodes.add(adjacentNode);
                    }
                }
                settledNodes.add(currentNode);
            }
        return graph;
    }

    private static Node getLowestDistanceNode (Set<Node> unsettledNodes){

            Node lowestDistanceNode = null;
            int lowestDistance = Integer.MAX_VALUE;

            for(Node node : unsettledNodes){
                int nodeDistance = node.getDistance();
                if(nodeDistance < lowestDistance){
                    lowestDistance = nodeDistance;
                    lowestDistanceNode = node;
                }
            }
            return lowestDistanceNode;
    }

    private static void calculateMinimumDistance(Node evaluationNode, int weight, Node currentNode){
        int totalDistance = currentNode.getDistance() + weight;
        if(totalDistance < evaluationNode.getDistance()){
            evaluationNode.setDistance(totalDistance);
            LinkedList<Node> shortestPath = new LinkedList<>(evaluationNode.getShortestPath());
            shortestPath.add(currentNode);
            evaluationNode.setShortestPath(shortestPath);
        }
    }

}
