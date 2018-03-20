package main.java.socs.network.node;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * Graph data structure
 * Created by alicezhu on 25/2/2018.
 */
public class Graph {

    private Set<Node> nodes = new HashSet<>();

    /**
     * add node to graph
     * @param nodeA node to add
     */
    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }

    /**
     * print current graph structure
     */
    public void printGraph(){
        System.out.print("Printing Latest Graph: " + "\n");
        for (Node n : nodes) {
            System.out.println("Node: " + n.getName() + "\n");
            n.printNeighborNodes();
        }
    }
    
    /**
     * find and return the node with the given name in the graph
     * @param Name name of node
     * @return node object with name Name, null if no such node exists in the graph
     */
    public Node findNode(String Name){
        for(Node node : this.nodes){
            if(node.getName().equals(Name)){
                return node;
            }
        }
        return null;
    }

    /**
     * calculate the shortest path to each node in the graph from the source node 
     * using Dijkstra's shortest path algorithm
     * @param src the source node
     */
    public void calculateShortestPath(Node src){
    			Node source = findNode(src.getName());
    			
    			//distance from source to source is 0
            source.setDistance(0);
            
            Set<Node> settledNodes = new HashSet<>();
            
            Set<Node> unsettledNodes = new HashSet<>();
            unsettledNodes.add(source);

            while(unsettledNodes.size() != 0){
                Node currentNode = getLowestDistanceNode(unsettledNodes);
                unsettledNodes.remove(currentNode);
                
                //calculate costs of all unsettled nodes directly linked to current node
                for(Map.Entry<Node, Integer> adjacentNodePair : currentNode.getAdjacentNodes().entrySet()){
                    Node adjacentNode = adjacentNodePair.getKey();
                    int distance = adjacentNodePair.getValue();
                    if(!settledNodes.contains(adjacentNode)){
                        calculateMinimumDistance(adjacentNode, distance, currentNode);
                        unsettledNodes.add(adjacentNode);
                    }
                }
                settledNodes.add(currentNode);
            }
    }
    
    /**
     * find the closest node to the source node in the given set
     * @param unsettledNodes set of nodes
     * @return node with the lowest distance to the source
     */
    private Node getLowestDistanceNode (Set<Node> unsettledNodes){
            Node lowestDistanceNode = null;
            int lowestDistance = Integer.MAX_VALUE; //infinity

            for(Node node : unsettledNodes){
                int nodeDistance = node.getDistance();
                if(nodeDistance < lowestDistance){
                    lowestDistance = nodeDistance;
                    lowestDistanceNode = node;
                }
            }
            
            return lowestDistanceNode;
    }

    /**
     * re-calculate and set the shortest path between a node and the source node
     * @param evaluationNode node which distance to the source is being calculated
     * @param weight distance (weight of the link) between evaluationNode and currentNode
     * @param currentNode node that evaluationNode is linked to
     */
    private void calculateMinimumDistance(Node evaluationNode, int weight, Node currentNode){
        //distance from source node to evaluation node
    		int totalDistance = currentNode.getDistance() + weight;
    		
    		//shorter distance to source node has been found
        if(totalDistance < evaluationNode.getDistance()){
            evaluationNode.setDistance(totalDistance);
            List<Node> shortestPath = new LinkedList<>(currentNode.getShortestPath());
            List<Integer> shortestPathWeights = new LinkedList<>(currentNode.getShortestPathWeights());
            shortestPath.add(currentNode);
            shortestPathWeights.add(weight);
            evaluationNode.setShortestPath(shortestPath);
            evaluationNode.setShortestPathWeights(shortestPathWeights);
        }
    }

}
