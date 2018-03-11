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
            System.out.println("Node: " + n.getName() + "\n");
            n.printNeighborNodes();
        }
    }
    
    public Node findNode(String Name){
        for(Node node : this.nodes){
            if(node.getName().equals(Name)){
                return node;
            }
        }
        return null;
    }

    /**
     * calculate shortest path to each node from the source node using Dijkstra's 
     * shortest path algorithm.
     */
    public void calculateShortestPath(Node src){
    			Node source = findNode(src.getName());
            source.setDistance(0);
            Set<Node> settledNodes = new HashSet<>();
            Set<Node> unsettledNodes = new HashSet<>();
            unsettledNodes.add(source);

            while(unsettledNodes.size() != 0){
                Node currentNode = getLowestDistanceNode(unsettledNodes);
                //System.out.println("current node: " + currentNode.getName());
                unsettledNodes.remove(currentNode);
                for(Map.Entry<Node, Integer> adjacentNodePair : currentNode.getAdjacentNodes().entrySet()){
                    Node adjacentNode = adjacentNodePair.getKey();
                    int distance = adjacentNodePair.getValue();
                    //System.out.println("neighbor node: " + adjacentNode.getName() + " " + distance);
                    if(!settledNodes.contains(adjacentNode)){
                        calculateMinimumDistance(adjacentNode, distance, currentNode);
                        //System.out.println("add to unsettled: " + adjacentNode.getName());
                        unsettledNodes.add(adjacentNode);
                    }
                }
                settledNodes.add(currentNode);
            }
        //return graph;
    }

    private Node getLowestDistanceNode (Set<Node> unsettledNodes){

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

    private void calculateMinimumDistance(Node evaluationNode, int weight, Node currentNode){
        int totalDistance = currentNode.getDistance() + weight;
        if(totalDistance < evaluationNode.getDistance()){
            evaluationNode.setDistance(totalDistance);
            List<Node> shortestPath = new LinkedList<>(currentNode.getShortestPath());
            List<Integer> shortestPathWeights = new LinkedList<>(currentNode.getShortestPathWeights());
            //System.out.println(currentNode.getName() +"'s shortest path: " + currentNode.getDistance() + ", " + currentNode.toStringShortestPath());
            shortestPath.add(currentNode);
            shortestPathWeights.add(weight);
            evaluationNode.setShortestPath(shortestPath);
            evaluationNode.setShortestPathWeights(shortestPathWeights);
            //System.out.println(evaluationNode.getName() +"'s shortest path: " + evaluationNode.getDistance() + ", " + evaluationNode.toStringShortestPath());
        }
    }

}
