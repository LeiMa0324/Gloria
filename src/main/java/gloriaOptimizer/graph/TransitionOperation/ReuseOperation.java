package gloriaOptimizer.graph.TransitionOperation;

import gloriaOptimizer.costModel.CostModel;
import org.apache.commons.collections4.CollectionUtils;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.graph.node.Sset;

import java.util.ArrayList;

public class ReuseOperation extends Operation {

    //a new Sset with reuse checkpoint
    @Override
    public Sset operate(Sset p_sset, Edge currentEdge) {
        Sset newSset = new Sset();

        newSset.setQueries((ArrayList<Integer>) CollectionUtils.intersection(p_sset.getQueries(), currentEdge.getEdgeQueries()));
        if (p_sset.isShared()) {
            newSset.getCheckpoints().addAll(p_sset.getCheckpoints());
        }else {
            newSset.setShared(false);
        }

        newSset.setEdge(currentEdge);

        this.leftNodeSset = p_sset;
        this.rightNodeSset = newSset;
        return newSset;
    }
    @Override
    public Double computeWeight(){
        this.weight = CostModel.checkToReuseCost(this);
        return this.weight;
    }

    @Override
    public String toString(){
        return "Reuse Operation";
    }
}
