package gloria.gloriaOptimizer.graph.TransitionOperation;


import gloria.gloriaOptimizer.graph.node.Node;
import lombok.Data;
import gloria.gloriaOptimizer.template.Edge;
import gloria.gloriaOptimizer.graph.node.Sset;

@Data
//take in a S-set, output the S-set after the operation
public abstract class Operation {

    protected Node leftNode;
    protected Node rightNode;

    protected Sset leftNodeSset;
    protected Sset rightNodeSset;
    protected Double weight=0.0;

    public abstract Sset operate(Sset p_sset, Edge currentEdge);
    public abstract Double computeWeight();
}
