package gloriaOptimizer.graph.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import gloriaOptimizer.template.Edge;
import gloriaOptimizer.template.EventType.EventType;

import java.io.Serializable;
import java.util.ArrayList;

@AllArgsConstructor
@Data
/**
 * a s-set on each edge
 */
public class Sset implements Serializable {
    private ArrayList<Integer> queries;
    private ArrayList<EventType> checkpoints;
    private boolean isShared = true;
    private Edge edge;

    public Sset(){this.queries = new ArrayList<>();
    this.checkpoints = new ArrayList<>();}


    public void setEdge(Edge edge) {
        if (!edge.getSsets().contains(this)){
            edge.addSset(this);
        }
        this.edge = edge;
    }

    public boolean equals(Sset sset){

        if (isShared){
            return this.queries.equals(sset.queries)&&this.checkpoints.equals(sset.checkpoints)&&this.edge.equals(sset.edge);
        }else {
            return this.queries.equals(sset.queries)&&this.edge.equals(sset.edge);
        }

    }

    public Integer getCheckpointNum(){
        int snapNum = 0;
        for (EventType check:this.checkpoints){
            snapNum+=check.getFrequency();
        }

        return snapNum;
    }

    public boolean containsCheck(String EventTypeName){
        for (EventType et: this.checkpoints){
            if (et.getName().equals(EventTypeName)){
                return true;
            }
        }

        return false;
    }

    public String queryString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i=0; i<this.queries.size();i++){
            if (i!=0){
                sb.append(",");
            }
            sb.append(queries.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean containCheckpoint(String etName){
        for (EventType et:this.checkpoints){
            if (etName.equals(et.getName())){
                return true;
            }
        }

        return false;
    }


}
