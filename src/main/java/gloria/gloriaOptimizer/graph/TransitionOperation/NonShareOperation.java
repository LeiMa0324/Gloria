package gloria.gloriaOptimizer.graph.TransitionOperation;

import gloria.gloriaOptimizer.costModel.CostModel;
import org.apache.commons.collections4.CollectionUtils;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.graph.node.Sset;

import java.util.ArrayList;

public class NonShareOperation extends Operation {

    @Override
    public Sset operate(Sset p_sset, Edge currentEdge) {
        Sset newSset = new Sset();

        newSset.setQueries((ArrayList<Integer>) CollectionUtils.intersection(p_sset.getQueries(), currentEdge.getEdgeQueries()));
        newSset.setShared(false);
        newSset.setEdge(currentEdge);
        this.leftNodeSset = p_sset;
        this.rightNodeSset = newSset;
        return newSset;
    }

    @Override
    public Double computeWeight(){
        weight = CostModel.anyToNotShareCost(this);
        return weight;
    }

    @Override
    public String toString(){
        return "NonShare Operation";
    }
}
