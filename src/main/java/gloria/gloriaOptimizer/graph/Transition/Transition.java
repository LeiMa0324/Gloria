package gloria.gloriaOptimizer.graph.Transition;

import gloria.gloriaOptimizer.graph.TransitionOperation.MergeOperation;
import gloria.gloriaOptimizer.graph.TransitionOperation.Operation;
import gloria.gloriaOptimizer.graph.node.Node;
import gloria.gloriaOptimizer.graph.node.Sset;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
/**
 * atomic transition is not in left, right node's transition list
 */
public class Transition implements Serializable {
    protected Node leftNode;
    protected Node rightNode;
    protected Double weight=0.0;
    ArrayList<Operation> operations;
    protected boolean isMerged = false;
    protected MergeOperation mergeOperation;


    public Transition(){
        super();
        this.operations = new ArrayList<>();
    }


    public void addOperation(Operation operation){
        operation.setLeftNode(this.leftNode);
        operation.setRightNode(this.rightNode);
        this.operations.add(operation);
        if (!rightNode.getSsets().contains(operation.getRightNodeSset())){
            this.rightNode.getSsets().add(operation.getRightNodeSset());
        }


    }

    public void addMergedOperation(MergeOperation mergeOperation){
        this.mergeOperation = mergeOperation;
        this.mergeOperation.setLeftNode(this.leftNode);
        this.mergeOperation.setRightNode(this.rightNode);
        this.isMerged = true;
        if (this.getRightNode().getSsets().isEmpty()){
            this.rightNode.getSsets().add(mergeOperation.getRightNodeSset());

        }
    }

    public Node finish(){
        computeWeight();
        leftNode.getOutgoingTransitions().add(this);
        rightNode.getIncomingTransitions().add(this);
        return rightNode;
    }

    public void redirectTo(Node rightNode){
        this.rightNode = rightNode;
        rightNode.getIncomingTransitions().add(this);
    }

    public void  disconnect(){
        this.leftNode.getOutgoingTransitions().remove(this);
        setLeftNode(null);
        this.rightNode.getIncomingTransitions().remove(this);
        setRightNode(null);
    }

    public Double computeWeight(){
        if (isMerged){
            double relatedQueryNum =0.0;

            for (Sset sset: this.leftNode.getRelatedSsets(this.rightNode.getEdge())){
                relatedQueryNum+= CollectionUtils.intersection(sset.getQueries(), this.mergeOperation.getRightNodeSset().getQueries()).size();
            }
            mergeOperation.setWeightPortion(relatedQueryNum/(this.mergeOperation.getRightNodeSset().getQueries().size()+0.0));
            this.weight = mergeOperation.computeWeight();
            System.out.println(mergeOperation);
        }
        for (Operation oper: this.operations){
            this.weight += oper.computeWeight();
        }

        return weight;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Left Node:").append(this.leftNode.toString()).append("Right Node:").append(this.rightNode.toString())
                .append("Weight: ").append(this.weight).append("\n");

        return sb.toString();
    }
}
