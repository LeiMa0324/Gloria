package gloria.base;

import lombok.Data;
import gloria.users.busUser.busAttributeEnum;
import gloria.users.stockUser.stockAttributeEnum;
import gloria.users.taxiUser.taxiAttributeEnum;

import java.util.ArrayList;

@Data
public class DatasetSchema {
    private String datasetName;
    private ArrayList<Attribute> attributes;

    public DatasetSchema(String datasetName){
        this.attributes = new ArrayList<>();
        this.datasetName = datasetName;

        if (datasetName.equals("stock")){
            for (stockAttributeEnum a: stockAttributeEnum.values()){
                this.addAttribute(new Attribute(a.toString()));
            }
        }

        if (datasetName.equals("bus")){
            for (busAttributeEnum a: busAttributeEnum.values()){
                this.addAttribute(new Attribute(a.toString()));
            }
        }

        if (datasetName.equals("taxi")){
            for (taxiAttributeEnum a: taxiAttributeEnum.values()){
                this.addAttribute(new Attribute(a.toString()));
            }
        }
    }

    public DatasetSchema(ArrayList<Attribute> attributes){
        this.attributes = attributes;
    }

    public void addAttribute(Attribute attribute){
        attributes.add(attribute);
    }

    public Attribute getAttributeByName(String attrName){
        for (Attribute attr: attributes){
            if (attr.getName().equals(attrName)){
                return attr;
            }
        }
        return null;
    }
}
