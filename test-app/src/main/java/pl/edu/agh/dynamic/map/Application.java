package pl.edu.agh.dynamic.map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.postgis.Point;
import pl.edu.agh.dynamic.map.dao.ConnectionManager;
import pl.edu.agh.dynamic.map.dao.OsmosisDao;
import pl.edu.agh.dynamic.map.dao.RdnrDao;
import pl.edu.agh.dynamic.map.log.StatsCollector;
import pl.edu.agh.dynamic.map.model.Crossroad;
import pl.edu.agh.dynamic.map.model.Sensor;
import pl.edu.agh.dynamic.map.model.SensorType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Application {

	protected static Properties prop = new Properties();
	private static InputStream input = null;

	static {
		try {

			input = new FileInputStream("config.properties");

			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
    private static final String ACTIVITY_TEST1 = "Test 1";
    private static final String ACTIVITY_TEST2 = "Test 2";
    private static final String ACTIVITY_TEST3 = "Test 3";
    private static final String ACTIVITY_TEST4 = "Test 4";
    private static final String ACTIVITY_TEST5 = "Test 5";
    private static final String ACTIVITY_TEST6 = "Test 6";

    private static Logger log;
    private static ConnectionManager connectionManager;
    private static final String OSMOSIS = "Osmosis";
    private static final String RDNR = "RDNR";
    private static OsmosisDao osmosisDao;
    private static RdnrDao rdnrDao;
    private static StatsCollector statsCollector = new StatsCollector();

    /**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        initLoggers();
        initJDBC();

        test1();
        test2();

        test6();
        log.info("All tests executed successfully :)");
    }

    private static void test1() throws SQLException {
        log.info("=== Executing test 1 ===");
        statsCollector.startActivity(ACTIVITY_TEST1);
        Point point = new Point("POINT(19.964204 50.071905)");
        List<Crossroad> crossroadList = osmosisDao.getCrossroadsNearPoint(point, 10000);
        log.info("Fetched " + crossroadList.size() + " crossroads");

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

        statsCollector.finishActivity(ACTIVITY_TEST1);
        log.info("Test 1 executed successfully.");
        log.info(statsCollector.getLogFor(ACTIVITY_TEST1));
    }

    private static void test2() throws SQLException {
        log.info("=== Executing test 2 ===");
        //getting crossroads - not included in test time
        Point point = new Point("POINT(19.964204 50.071905)");
        List<Crossroad> crossroadList = osmosisDao.getCrossroadsNearPoint(point, 10000);
        statsCollector.startActivity(ACTIVITY_TEST2);

        List<SensorType> sensorTypes = new ArrayList<SensorType>(3);
        sensorTypes.add(SensorType.QUEUE_LENGTH);
        List<Sensor> sensorList = rdnrDao.getSensorsForCrossroads(crossroadList, sensorTypes);
        log.info("Fetched " + sensorList.size() + " sensors");
        if(!sensorList.isEmpty()) {
            int queueSums = 0;
            for(Sensor sensor : sensorList) {
                queueSums += Integer.valueOf(sensor.getValue());
            }
            double avgQueue = queueSums / sensorList.size();
            log.info("There are average " + avgQueue + " cars waiting on every crossroad");
        }
        statsCollector.finishActivity(ACTIVITY_TEST2);
        log.info("Test 2 executed successfully.");
        log.info(statsCollector.getLogFor(ACTIVITY_TEST2));
    }

    private static void initLoggers() {
        BasicConfigurator.configure();
        log = Logger.getRootLogger();
    }

    private static void initJDBC() throws SQLException, ClassNotFoundException {

		String host =prop.getProperty("osmosis.host");
		String username =prop.getProperty("osmosis.username");
		String password =prop.getProperty("osmosis.password");
		String port =prop.getProperty("osmosis.port");
		String db =prop.getProperty("osmosis.db");

    	
        connectionManager = new ConnectionManager();

		log.debug("jdbc:postgresql://" + host + ":" + port + "/" + db + ":" + username + ":" + password);
        connectionManager.createConnection(OSMOSIS, "jdbc:postgresql://" + host + ":" + port + "/" + db, username, password);
        log.info("Osmosis connection established successfully!");
        

		 host =prop.getProperty("rdnr.host");
		 username =prop.getProperty("rdnr.username");
		 password =prop.getProperty("rdnr.password");
		 port =prop.getProperty("rdnr.port");
		 db =prop.getProperty("rdnr.db");


		log.debug("jdbc:postgresql://" + host + ":" + port + "/" + db + ":" + username + ":" + password);
        connectionManager.createConnection(RDNR, "jdbc:postgresql://" + host + ":" + port + "/" + db, username, password);
        log.info("RDNR connection established successfully!");

        osmosisDao = new OsmosisDao(connectionManager.getConnection(OSMOSIS));
        rdnrDao = new RdnrDao(connectionManager.getConnection(RDNR));
    }


    private static void test6() throws SQLException {
        log.info("=== Executing test 6 ===");
        statsCollector.startActivity(ACTIVITY_TEST6);

        String sql1 = "SELECT distinct way_id FROM krakow.way_tags WHERE v in ('Bronowicka', 'Kr�lewska');";
		log.info("Selecting wayOne parts. Executing: "+sql1);
        PreparedStatement wayOne = osmosisDao.getConnection().prepareStatement(sql1);
        
        ResultSet wayOneParts = wayOne.executeQuery();
        int count = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct id FROM krakow.way_segments WHERE id in (");
        while(wayOneParts.next()){
        	if(count>0)
        		sb.append(",");
        	count++;
        	sb.append(wayOneParts.getLong("way_id"));
        }
        sb.append(");");
        log.info("Ways for wayOne found: "+count);
        String sql3 = sb.toString();
        log.info("Selecting wayOne segments ids. Executing: "+sql3);
        
        
        String sql2 = "SELECT distinct way_id FROM krakow.way_tags WHERE v in ('Nawojki', 'Czarnowiejska', 'Armii Krajowej');";
        log.info("Selecting wayTwo parts. Executing: "+sql2);
        PreparedStatement wayTwo = osmosisDao.getConnection().prepareStatement(sql2);
        
        ResultSet wayTwoParts = wayTwo.executeQuery();
        count = 0;
        while(wayTwoParts.next()){
        	count++;
        }
        log.info("Ways for wayOne found: "+count);
        
        
        statsCollector.finishActivity(ACTIVITY_TEST6);
        log.info("Test 6 executed successfully.");
        log.info(statsCollector.getLogFor(ACTIVITY_TEST6));
    }
}
