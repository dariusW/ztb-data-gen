package pl.edu.agh.dynamic.map.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgis.Point;

import pl.edu.agh.dynamic.map.model.Crossroad;
import pl.edu.agh.dynamic.map.model.Sensor;
import pl.edu.agh.dynamic.map.model.SensorType;

public class Test2 extends AbstractTest {

	@Override
	protected String name() {
		return "Test 2";
	}

	@Override
	protected void test() throws SQLException {
		//getting crossroads - not included in test time
        Point point = new Point("POINT(19.964204 50.071905)");
        start();
        List<Crossroad> crossroadList = osmosisDao.getCrossroadsNearPoint(point, 10000);
        stop();

        List<SensorType> sensorTypes = new ArrayList<SensorType>(3);
        sensorTypes.add(SensorType.QUEUE_LENGTH);
        start();
        List<Sensor> sensorList = rdnrDao.getSensorsForCrossroads(crossroadList, sensorTypes);
        stop();
        log.info("Fetched " + sensorList.size() + " sensors");
        if(!sensorList.isEmpty()) {
            int queueSums = 0;
            for(Sensor sensor : sensorList) {
                queueSums += Integer.valueOf(sensor.getValue());
            }
            double avgQueue = queueSums / sensorList.size();
            log.info("There are average " + avgQueue + " cars waiting on every crossroad");
        }

	}

}
