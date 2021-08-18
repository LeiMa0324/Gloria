package gloriaOptimizer.graph.TransitionOperation;

import gloriaOptimizer.costModel.CostModel;
import org.apache.commons.collections4.CollectionUtils;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.graph.node.Sset;

import java.util.ArrayList;

public class LocalOperation extends Operation {

    @Override
    public Sset operate(Sset p_sset, Edge currentEdge) {
        Sset newSset = new Sset();

        newSset.setQueries((ArrayList<Integer>) CollectionUtils.intersection(p_sset.getQueries(), currentEdge.getEdgeQueries()));
        newSset.getCheckpoints().add(currentEdge.getLeftEventType());
        newSset.setEdge(currentEdge);
        this.leftNodeSset = p_sset;
        this.rightNodeSset = newSset;
        return newSset;
    }

    @Override
    public Double computeWeight(){
        weight = CostModel.anyToLocalCheckCost(this);
        return weight;
    }


    @Override
    public String toString(){
        return "Local Operation";
    }
}
