package gloriaOptimizer.template.EventType;

import base.DatasetSchema;
import gloriaOptimizer.template.Edge;

/**
 * the start vertex of each query
 */
public class StartEventType extends EventType{

    public StartEventType(DatasetSchema schema){
        super(schema);
        this.isStart=true;
        this.name = "START";
    }

    public EventType getFirstEventType(){

        for (Edge edge: this.getOutgoingEdgesToSuc().values()){
            return edge.getRightEventType();
        }

        return null;
    }

    @Override
    public String toString(){
        return super.toString();
    }
}
