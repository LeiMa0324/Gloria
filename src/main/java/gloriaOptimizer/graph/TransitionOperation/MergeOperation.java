package gloriaOptimizer.graph.TransitionOperation;

import gloriaOptimizer.graph.node.Node;
import gloriaOptimizer.graph.node.Sset;
import gloriaOptimizer.costModel.CostModel;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import gloriaOptimizer.template.Edge;

import java.util.ArrayList;

@Data
public class MergeOperation{


    protected Node leftNode;
    protected Node rightNode;
    protected ArrayList<Sset> leftNodeSsets;
    protected Sset rightNodeSset;
    protected Double weight=0.0;
    protected double weightPortion =1.0;


    public Sset operate(ArrayList<Sset> p_ssets, Edge currentEdge) {
        Sset newSset = new Sset();

        ArrayList<Integer> queries = new ArrayList<>();

        for (Sset p: p_ssets){
            queries.addAll(CollectionUtils.intersection(p.getQueries(), currentEdge.getEdgeQueries()));
        }

        newSset.setQueries(queries);
        newSset.getCheckpoints().add(currentEdge.getLeftEventType());
        newSset.setEdge(currentEdge);

        this.leftNodeSsets = p_ssets;
        this.rightNodeSset = newSset;
        return newSset;
    }


    public Double computeWeight(){
        weight = CostModel.anyToMerge(this)*weightPortion;
        return weight;
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Left Node").append(leftNode.toString()).append(", Right Node").append(rightNode.toString());
        sb.append("To Merge Ssets ").append(leftNodeSsets).append(", Merged Sset").append(rightNodeSset.toString());
        sb.append("Merge weight portition:").append(this.weightPortion);
        sb.append("Merge weight:").append(this.weight);

        return sb.toString();
    }
}
