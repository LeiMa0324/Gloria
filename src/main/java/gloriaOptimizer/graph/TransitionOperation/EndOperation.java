package gloriaOptimizer.graph.TransitionOperation;

import gloriaOptimizer.costModel.CostModel;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.graph.node.Sset;

public class EndOperation extends Operation{
    public Sset operate(Sset p_sset, Edge currentEdge){
        this.leftNodeSset = p_sset;
        this.rightNodeSset = p_sset;
        return p_sset;

    }

    @Override
    public Double computeWeight(){
        weight = CostModel.anyToEnd(this);
        return weight;
    }

    @Override
    public String toString(){
        return "End Operation";
    }
}
