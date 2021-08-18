package gloriaOptimizer.template.EventType;

import base.DatasetSchema;
import lombok.Data;

@Data
/**
 * real event type
 */
public class RealEventType extends EventType{


    public RealEventType(String name, DatasetSchema schema){
        super(schema);
        this.name = name;
    }

    @Override
    public String toString(){
        return super.toString();
    }
}
