package gloria.gloriaOptimizer.graph.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import gloria.gloriaOptimizer.template.Edge;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
@Data
public class Plan_Node extends Node {


    public Plan_Node(Plan_Node node){
        this.ssets = new ArrayList<>();
        this.incomingTransitions = new ArrayList<>();
        this.outgoingTransitions = new ArrayList<>();
        this.edge = node.edge;
        this.ssets = node.ssets;
        this.incomingTransitions = node.incomingTransitions;
    }

    public Plan_Node(){
        super();
        this.ssets = new ArrayList<>();

    }

    public Plan_Node(Edge edge, ArrayList<Sset> ssets){
        this.ssets = new ArrayList<>();
        this.incomingTransitions = new ArrayList<>();
        this.outgoingTransitions = new ArrayList<>();
        this.edge = edge;
        this.ssets = ssets;
    }



    @Override
    public String toString(){
       return super.toString();
    }
}
