package gloriaOptimizer.template.EventType;

import base.DatasetSchema;
import lombok.Data;

@Data
/**
 * the end vertex of each query
 */
public class EndEventType extends EventType{

    public EndEventType(DatasetSchema schema){
        super(schema);
        this.isEnd=true;
        this.name = "END";
    }

    @Override
    public String toString(){
        return super.toString();
    }
}
