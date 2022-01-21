package gloria.gloriaOptimizer.graph.TransitionOperation;

import gloria.gloriaOptimizer.costModel.CostModel;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.graph.node.Sset;

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
