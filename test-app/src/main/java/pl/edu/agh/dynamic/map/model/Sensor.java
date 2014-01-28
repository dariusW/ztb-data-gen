package pl.edu.agh.dynamic.map.model;

/**
 * Created with IntelliJ IDEA.
 * User: Khajiit
 * Date: 25.01.14
 * Time: 22:56
 * To change this template use File | Settings | File Templates.
 */
public class Sensor {

    private String value;

    private SensorType type;

    private String name;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
