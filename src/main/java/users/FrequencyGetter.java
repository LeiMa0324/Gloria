package users;

import users.busUser.busHighFrequencyEventEnum;
import users.busUser.busLowFrequencyEventEnum;
import users.stockUser.TestEnum;
import users.stockUser.stockHighFrequencyEventTypeEnum;
import users.stockUser.stockLowFrequencyEventTypeEnum;
import users.taxiUser.taxiHighFrequencyEventEnum;
import users.taxiUser.taxiLowFrequencyEventEnum;

import java.awt.font.FontRenderContext;

public class FrequencyGetter {

    public static Integer get(String eventType, String dataset) {
        Integer frequency = -1;

        if (dataset.equals("stock")) {
            for (stockHighFrequencyEventTypeEnum stat : stockHighFrequencyEventTypeEnum.values()) {
                if (stat.name().equals(eventType)) {
                    frequency = stat.frequency;
                }
            }

            for (stockLowFrequencyEventTypeEnum stat : stockLowFrequencyEventTypeEnum.values()) {
                if (stat.name().equals(eventType)) {
                    frequency = stat.frequency;
                }
            }
            for (TestEnum testEnum: TestEnum.values()){
                if (testEnum.name().equals(eventType)) {
                    frequency = testEnum.frequency;
                }
            }

            return frequency;
        }


    if(dataset.equals("taxi")){
        for (taxiHighFrequencyEventEnum stat : taxiHighFrequencyEventEnum.values()) {
            if (stat.name().equals(eventType)) {
                frequency = stat.frequency;
            }
        }

        for (taxiLowFrequencyEventEnum stat : taxiLowFrequencyEventEnum.values()) {
            if (stat.name().equals(eventType)) {
                frequency = stat.frequency;
            }
        }
        return frequency;
    }

        if(dataset.equals("bus")){
            for (busHighFrequencyEventEnum stat : busHighFrequencyEventEnum.values()) {
                if (stat.name().equals(eventType)) {
                    frequency = stat.frequency;
                }
            }

            for (busLowFrequencyEventEnum stat : busLowFrequencyEventEnum.values()) {
                if (stat.name().equals(eventType)) {
                    frequency = stat.frequency;
                }
            }
            return frequency;
        }

    return -1;
    }
}
