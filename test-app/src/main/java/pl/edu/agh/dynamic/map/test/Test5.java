package pl.edu.agh.dynamic.map.test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.dynamic.map.model.Crossroad;
import pl.edu.agh.dynamic.map.model.Sensor;
import pl.edu.agh.dynamic.map.model.SensorType;

public class Test5 extends AbstractTest {

	protected void test() throws SQLException {

		analyzeWay("('Bronowicka', 'Królewska')", "WAY_ONE");
		analyzeWay("('Nawojki', 'Czarnowiejska', 'Armii Krajowej')", "WAY_TWO");

	}

	private void analyzeWay(String way, String name) throws SQLException {
		String sql1 = "SELECT distinct id FROM krakow.way_tags NATURAL JOIN krakow.lanes WHERE v in " + way + ";";
		log.info("Selecting " + name + " lanes. Executing: " + sql1);
		PreparedStatement wayOne = osmosisDao.getConnection().prepareStatement(sql1);

		start();
		ResultSet wayOneParts = wayOne.executeQuery();
		stop();
		int count = 0;
		StringBuilder sb = new StringBuilder(
				"SELECT DISTINCT ON (label) label, time_measured,value FROM param_monitoringparametervalue v JOIN param_monitoringparameterinstance i ON (v.instance=i.id) JOIN param_instanceassignment a ON (i.assignment=a.id) JOIN param_instanceassignment_sm_lane l ON (l.param_instanceassignment=a.id) WHERE sm_lane IN (");
		
		List<Long> lanesIds = new ArrayList<Long>();
		while (wayOneParts.next()) {
			Long id = wayOneParts.getLong("id");
			if(count > 0){
				sb.append(",");
			}
			sb.append(id);
			count++;
			lanesIds.add(id);
		}
		sb.append(") ORDER BY label, time_measured DESC;");
		log.info("Lanes for " + name + " found: " + count);
		log.info("Static map data fetched.");
		
		String attachedMonitorSensorValuesSQL = sb.toString();
		log.info("Sensor data from dynamic map query: "+attachedMonitorSensorValuesSQL);
		PreparedStatement statement = rdnrDao.getConnection().prepareStatement(attachedMonitorSensorValuesSQL);

		log.info("sensor data query: "+attachedMonitorSensorValuesSQL);
		start();
		ResultSet rs = statement.executeQuery();
		stop();
		count=0;
		while(rs.next()){
			count++;
			log.info(rs.getString(1)+"|"+rs.getDate(2).toString()+"|"+rs.getString(3));
		}
		log.info("Monitor data found: " + count);
		

		
		
		// // find crossroasd
		// String sql4 = sb.toString();
		// log.info("Crossroads for "+name+". Executing: "+sql4);
		// statemant = osmosisDao.getConnection().prepareStatement(sql3);
		// rs = statemant.executeQuery();
		// count = 0;
		// List<Crossroad> crossroadList = new ArrayList<Crossroad>();
		// while(rs.next()){
		// count++;
		// Crossroad c = new Crossroad();
		// c.setId(rs.getLong("id"));
		// crossroadList.add(c);
		// }
		// log.info("crossroads for "+name+" found: "+count);
		//
		//
		//
		// List<SensorType> sensorTypes = new ArrayList<SensorType>(3);
		// sensorTypes.add(SensorType.SPEED);
		// List<Sensor> sensorList =
		// rdnrDao.getSensorsForCrossroads(crossroadList, sensorTypes);
		// log.info("Fetched " + sensorList.size() + " sensors");
		//
		// if(!sensorList.isEmpty()) {
		// int sensorValSum = 0;
		// for(Sensor sensor : sensorList) {
		// sensorValSum += Integer.valueOf(sensor.getValue());
		// }
		// double avgSpeed = sensorValSum / sensorList.size();
		// log.info("AVERAGE SPEED on selected crossroads is " + avgSpeed +
		// "km/h");
		// } else {
		// log.warn("Sensor list for selected crossroads is empty! Cannot calculate average speed");
		// }
	}

	@Override
	protected String name() {
		return "TEST 5";
	}
}
