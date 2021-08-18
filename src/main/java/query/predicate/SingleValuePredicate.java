package query.predicate;

import base.Attribute;
import base.DatasetSchema;
import base.Event;

import java.util.ArrayList;

/**
 * the local predicate
 */
public class SingleValuePredicate extends Predicate{

    //the condition value
    //could be a number or a string
    private Float value;
    private Attribute singleAttribute;

    public SingleValuePredicate(ArrayList<String> eventTypeNames,
                                String operator,
                                ArrayList<String> attributeNames,
                                DatasetSchema schema,
                                Float value){

        super(eventTypeNames, operator, attributeNames, schema);
        this.value = value;
        this.singleAttribute = this.predAttributes.get(0);
    }

    protected boolean greater(ArrayList<Event> events){
        Event event = events.get(0);
        return Float.parseFloat((String)event.getAttributeValueByName(singleAttribute.getName()))>value ;

    }

    protected boolean less(ArrayList<Event> events){
        Event event = events.get(0);

        return (Float)event.getAttributeValueByName(singleAttribute.getName())<(Float) value;

    }

    protected boolean equal(ArrayList<Event> events){
        Event event = events.get(0);
        Float dataValue = Float.parseFloat((String) event.getAttributeValueByName(singleAttribute.getName()));

        return dataValue.equals(value);

    }

    public String toString(){
        return singleAttribute.getName()+ this.operator + value;
    }
}
