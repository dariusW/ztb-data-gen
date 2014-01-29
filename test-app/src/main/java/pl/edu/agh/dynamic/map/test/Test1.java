package pl.edu.agh.dynamic.map.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgis.Point;

import pl.edu.agh.dynamic.map.model.Crossroad;
import pl.edu.agh.dynamic.map.model.Sensor;
import pl.edu.agh.dynamic.map.model.SensorType;

public class Test1 extends AbstractTest {

	@Override
	protected String name() {
		return "Test 1";
	}

	@Override
	protected void test() throws SQLException {
		Point point = new Point("POINT(19.964204 50.071905)");
		start();
        List<Crossroad> crossroadList = osmosisDao.getCrossroadsNearPoint(point, 10000);
        stop();
        log.info("Fetched " + crossroadList.size() + " crossroads");

        List<SensorType> sensorTypes = new ArrayList<SensorType>(3);
        sensorTypes.add(SensorType.SPEED);
		start();
        List<Sensor> sensorList = rdnrDao.getSensorsForCrossroads(crossroadList, sensorTypes);
        stop();
        log.info("Fetched " + sensorList.size() + " sensors");

        if(!sensorList.isEmpty()) {
            int sensorValSum = 0;
            for(Sensor sensor : sensorList) {
                sensorValSum += Integer.valueOf(sensor.getValue());
            }
            double avgSpeed = sensorValSum / sensorList.size();
            log.info("AVERAGE SPEED on selected crossroads is " + avgSpeed + "km/h");
        } else {
            log.warn("Sensor list for selected crossroads is empty! Cannot calculate average speed");
        }

	}

}
