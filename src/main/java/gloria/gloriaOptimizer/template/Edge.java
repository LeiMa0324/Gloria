package gloria.gloriaOptimizer.template;

import gloria.gloriaOptimizer.graph.node.Sset;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import gloria.gloriaOptimizer.template.EventType.EventType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

@Data
@ToString
/**
 * edge between two event types
 */
public class Edge implements Serializable {
    private EventType leftEventType;
    private EventType rightEventType;
    private ArrayList<Integer> edgeQueries;
    private boolean isKleene = false;


    private ArrayList<Sset> ssets;

    public Edge(){
        this.edgeQueries = new ArrayList<>();
        this.ssets = new ArrayList<>();

    }

    public void addSset(Sset sset){
        this.ssets.add(sset);
    }

    public void setSsets(ArrayList<Sset> ssets) {
        for (Sset sset: ssets){
            addSset(sset);
        }
    }

    public void connect(EventType left, EventType right, Integer qid){
        this.leftEventType = left;
        this.rightEventType = right;

        if (!this.edgeQueries.contains(qid)){
            this.edgeQueries.add(qid);
        }
        if (this.rightEventType.isEnd()){
            this.leftEventType.getEndQueries().add(qid);
        }
        if (this.leftEventType.isStart()){
            this.rightEventType.getStartQueries().add(qid);
        }
    }

    public ArrayList<Edge> getPrecedingSEQEdges(){
        ArrayList<Edge> p_edges = new ArrayList<>();
        for (Edge p_edge: this.leftEventType.getIncomingSEQEdges()){
            if (!CollectionUtils.intersection(p_edge.getEdgeQueries(), this.getEdgeQueries()).isEmpty()){
                p_edges.add(p_edge);
            }
        }

        return p_edges;
    }

    public Edge getPrecedingSelfKleeneEdge(){
        if (this.leftEventType.isSelfKleene()){
            return this.leftEventType.getIncomingKleeneEdge();
        }
        return null;
    }

    @Override
    public String toString(){
        return "("+this.leftEventType.toString()+", "+this.rightEventType.toString()+")"+this.edgeQueries;
    }
}
