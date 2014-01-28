package pl.edu.agh.dynamic.map.dao;

import pl.edu.agh.dynamic.map.model.Crossroad;
import pl.edu.agh.dynamic.map.model.Sensor;
import pl.edu.agh.dynamic.map.model.SensorType;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class RdnrDao {

    private static final String SENSOR_VAL = "sensorValue";

    private static final String SENSOR_VAL_COL = "val.value as " + SENSOR_VAL;

    private static final String SENSOR_NAME = "sensorName";

    private static final String SENSOR_INSTANCE_LABEL_COL = "inst.label as " + SENSOR_NAME;

    private static final String SENSOR_TYPE = "sensorType";

    private static final String SENSOR_TYPE_COL = "inst.type as " + SENSOR_TYPE;

    private static final String SENSOR_COLUMNS_TO_FETCH = "";

    private static final String SELECT_SENSORS_FOR_CROSSROADS = "SELECT " + SENSOR_VAL_COL + "," + SENSOR_INSTANCE_LABEL_COL + "," + SENSOR_TYPE_COL + " from param_monitoringparametervalue val \n" +
            "join param_monitoringparameterinstance inst on (val.instance = inst.id)\n" +
            "join param_instanceassignment ass on (inst.assignment = ass.id) \n" +
            "join param_monitoringparametertype type on (inst.type = type.id)\n" +
            "where ass.crossroads is not null and ass.crossroads in (select * from unnest(?)) and type.id in (select * from unnest(?));";

    private Connection connection;

    public Connection getConnection() {
		return connection;
	}

	private PreparedStatement sensorsOnCrossroadsStatement = null;

    public RdnrDao(Connection connection) {
        this.connection = connection;
    }

    public List<Sensor> getSensorsForCrossroads(List<Crossroad> crossroadList, List<SensorType> selectedSensorTypes) throws SQLException {
        if(sensorsOnCrossroadsStatement == null) {
            sensorsOnCrossroadsStatement = connection.prepareStatement(SELECT_SENSORS_FOR_CROSSROADS);
        }

        Long[] crossroadIds = prepareCrossroadIds(crossroadList);
        Integer[] sensorTypeIds = prepareSensorTypeIds(selectedSensorTypes);
        sensorsOnCrossroadsStatement.setArray(1, connection.createArrayOf("bigint", crossroadIds));
        sensorsOnCrossroadsStatement.setArray(2, connection.createArrayOf("integer", sensorTypeIds));
        ResultSet resultSet = sensorsOnCrossroadsStatement.executeQuery();
        List<Sensor> sensorList = createSensorListFromResultSet(resultSet);

        return sensorList;
    }

    private List<Sensor> createSensorListFromResultSet(ResultSet resultSet) throws SQLException {
        List<Sensor> sensorList = new ArrayList<Sensor>();
        while(resultSet.next()) {
            Sensor sensor = new Sensor();
            sensor.setValue(resultSet.getString(SENSOR_VAL));
            sensor.setName(resultSet.getString(SENSOR_NAME));
            sensor.setType(SensorType.fromInt(resultSet.getInt(SENSOR_TYPE)));
            sensorList.add(sensor);
        }
        return sensorList;
    }

    private Long[] prepareCrossroadIds(List<Crossroad> crossroadList) {
        Long[] crossroadIds = new Long[crossroadList.size()];
        for(int i=0;i<crossroadList.size();i++) {
            crossroadIds[i] = crossroadList.get(i).getId();
        }
        return crossroadIds;
    }

    private Integer[] prepareSensorTypeIds(List<SensorType> sensorTypes) {
        Integer[] sensorTypeIds = new Integer[sensorTypes.size()];
        for(int i=0;i<sensorTypes.size();i++) {
            sensorTypeIds[i] = sensorTypes.get(i).getId();
        }
        return sensorTypeIds;
    }
}
