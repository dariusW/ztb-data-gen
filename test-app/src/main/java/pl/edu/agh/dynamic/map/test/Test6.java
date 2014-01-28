package pl.edu.agh.dynamic.map.test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.dynamic.map.model.Crossroad;
import pl.edu.agh.dynamic.map.model.Sensor;
import pl.edu.agh.dynamic.map.model.SensorType;

public class Test6 extends AbstractTest{

	
	
	
    protected void test() throws SQLException {

    	analyzeWay("('Bronowicka', 'Królewska')", "WAY_ONE");
    	analyzeWay("('Nawojki', 'Czarnowiejska', 'Armii Krajowej')", "WAY_TWO");
        
        
        
    }

	private void analyzeWay(String way, String name) throws SQLException {
		//find ways
        String sql1 = "SELECT distinct way_id FROM krakow.way_tags WHERE v in "+way+";";
		log.info("Selecting "+name+" parts. Executing: "+sql1);
        PreparedStatement wayOne = osmosisDao.getConnection().prepareStatement(sql1);
        
        ResultSet wayOneParts = wayOne.executeQuery();
        int count = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct id FROM krakow.way_segments WHERE way_id in (");
        while(wayOneParts.next()){
        	if(count>0)
        		sb.append(",");
        	count++;
        	sb.append(wayOneParts.getLong("way_id"));
        }
        sb.append(");");
        log.info("Ways for "+name+" found: "+count);
        
        //find way_segments
        String sql3 = sb.toString();
        log.info("Selecting "+name+" segments ids. Executing: "+sql3);
        
        PreparedStatement statemant = osmosisDao.getConnection().prepareStatement(sql3);
        ResultSet rs = statemant.executeQuery();
        count = 0;
        sb = new StringBuilder();
        sb.append("SELECT distinct crosroad_id id FROM krakow.crossroad_ways WHERE way_segment_id in (");
        while(rs.next()){
        	if(count>0)
        		sb.append(",");
        	count++;
        	sb.append(rs.getLong("id"));
        }
        sb.append(");");
        log.info("segments for "+name+" found: "+count);
        
        // find crossroasd
        String sql4 = sb.toString();
        log.info("Crossroads for "+name+". Executing: "+sql4);
         statemant = osmosisDao.getConnection().prepareStatement(sql3);
         rs = statemant.executeQuery();
         count = 0;
         List<Crossroad> crossroadList = new ArrayList<Crossroad>();
         while(rs.next()){
         	count++;
         	Crossroad c = new Crossroad();
         	c.setId(rs.getLong("id"));
         	crossroadList.add(c);
         }
         log.info("crossroads for "+name+" found: "+count);
        
        
        
        List<SensorType> sensorTypes = new ArrayList<SensorType>(3);
        sensorTypes.add(SensorType.SPEED);
        List<Sensor> sensorList = rdnrDao.getSensorsForCrossroads(crossroadList, sensorTypes);
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

	@Override
	protected String name() {
		return "TEST 6";
	}
}
