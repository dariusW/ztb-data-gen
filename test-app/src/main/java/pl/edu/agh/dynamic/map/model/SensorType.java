package pl.edu.agh.dynamic.map.model;

/**
 * Created with IntelliJ IDEA.
 * User: Khajiit
 * Date: 25.01.14
 * Time: 23:01
 * To change this template use File | Settings | File Templates.
 */
public enum SensorType {
    QUEUE_LENGTH(1), SPEED(2), VEHICLE_COUNTER(3);

    private int id;

    private SensorType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SensorType fromInt(int id) {
        for(SensorType sensorType : SensorType.values()) {
            if(sensorType.getId() == id) {
                return sensorType;
            }
        }

        return null;
    }
}
