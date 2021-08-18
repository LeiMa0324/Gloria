package query.predicate;

import base.Attribute;
import base.DatasetSchema;
import base.Event;
import lombok.Data;

import java.util.ArrayList;

/**
 * the class of predicate
 */
@Data
public abstract class Predicate {
    protected ArrayList<String> eventTypeNames;
    protected String operator;
    protected ArrayList<Attribute> predAttributes;

    public Predicate(){}

    public Predicate(ArrayList<String> eventTypeNames, String operator, ArrayList<String> attributeNames,
                     DatasetSchema schema){

        this.eventTypeNames = eventTypeNames;
        this.operator = operator;
        this.predAttributes = new ArrayList<>();
        for (String attrName: attributeNames){
            predAttributes.add(schema.getAttributeByName(attrName));
        }
    }

    /**
     * verify if an event satisfy the predicate
     * @param events
     * @return
     */

    public boolean verify(ArrayList<Event> events){

        boolean res = false;
        switch (operator){
            case ">":
                res = greater(events);
                break;
            case "=":
                res = equal(events);
                break;
            case "<":
                res =less(events);
                break;
        }

        return res;
    };
    protected abstract boolean greater(ArrayList<Event> events);
    protected abstract boolean equal(ArrayList<Event> events);
    protected abstract boolean less(ArrayList<Event> events);
    public abstract String toString();

}
