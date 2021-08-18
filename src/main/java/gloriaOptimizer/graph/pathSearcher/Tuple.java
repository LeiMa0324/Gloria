package gloriaOptimizer.graph.pathSearcher;

import gloriaOptimizer.graph.Transition.Transition;
import gloriaOptimizer.graph.node.Node;
import lombok.Data;

import java.util.ArrayList;
import java.util.Formattable;

@Data
public class Tuple {
    private ArrayList<Node> combination;
    private Double sumWeight=0.0;
    private ArrayList<Transition> transitions;
    private Tuple precedingTuple;

    public Tuple(){
        this.combination = new ArrayList<>();
        this.transitions = new ArrayList<>();
    }

    public Tuple(Tuple tuple){
        this.combination = new ArrayList<>(tuple.getCombination());
        this.transitions = new ArrayList<>(tuple.getTransitions());
        this.sumWeight = tuple.getSumWeight();
        this.precedingTuple = tuple.precedingTuple;
    }

    public void addNode(Node node){
        this.combination.add(node);
        for (Transition transition: node.getIncomingTransitions()){
            Node l_node = transition.getLeftNode();
            for (Node p_node: this.precedingTuple.getCombination()){
                if (p_node.getEdge().toString().equals(l_node.getEdge().toString())&&p_node.toString().equals(l_node.toString())){
                    this.transitions.add(transition);
                    sumWeight +=transition.getWeight();
                }
            }
        }
    }

    public boolean doesConnectToNode(Node node){
        for (Node node1: this.combination){
            for (Transition transition: node1.getOutgoingTransitions()){
                if (transition.getRightNode().toString().equals(node.toString())){
                    return true;
                }
            }

        }

        return false;
    }
}
