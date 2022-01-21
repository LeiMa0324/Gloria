package gloria.gloriaOptimizer.template.EventType;

import gloria.base.DatasetSchema;
import gloria.gloriaOptimizer.template.Edge;

/**
 * the start vertex of each gloria.query
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
