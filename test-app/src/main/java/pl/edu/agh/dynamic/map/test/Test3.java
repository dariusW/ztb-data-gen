package pl.edu.agh.dynamic.map.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.postgis.Polygon;

import pl.edu.agh.dynamic.map.model.Crossroad;
import pl.edu.agh.dynamic.map.model.Sensor;
import pl.edu.agh.dynamic.map.model.SensorType;

public class Test3 extends AbstractTest {

	@Override
	protected String name() {
		return "Test 3";
	}

	@Override
	protected void test() throws SQLException {
		Polygon polygon = new Polygon("POLYGON((36.994204 40.071905,40.064204 40.071905,40.964204 55.071905,10.964204 55.001905,36.994204 40.071905))");

		start();
		List<Crossroad> crossroadList = osmosisDao.getCrossroadsInArea(polygon);
		stop();
		log.info("Fetched " + crossroadList.size() + " crossroads");
		List<SensorType> sensorTypes = new ArrayList<SensorType>(3);
		sensorTypes.add(SensorType.SPEED);

		start();
		List<Sensor> sensorList = rdnrDao.getSensorsForCrossroads(crossroadList, sensorTypes);
		stop();
		log.info("Fetched " + sensorList.size() + " sensors");

		Set<Long> blockedCrossroads = new HashSet<Long>();
		for (Sensor sensor : sensorList) {
			if (Integer.valueOf(sensor.getValue()) < 5) {
				blockedCrossroads.add(sensor.getCrossroadId());
			}
		}
		log.info("There are " + blockedCrossroads.size() + " blocked crossroads in selected area");

	}

}
