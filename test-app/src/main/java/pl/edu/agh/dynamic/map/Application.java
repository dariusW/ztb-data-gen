package pl.edu.agh.dynamic.map;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import pl.edu.agh.dynamic.map.dao.ConnectionManager;
import pl.edu.agh.dynamic.map.dao.OsmosisDao;
import pl.edu.agh.dynamic.map.dao.RdnrDao;
import pl.edu.agh.dynamic.map.log.StatsCollector;
import pl.edu.agh.dynamic.map.test.SpecialTest;
import pl.edu.agh.dynamic.map.test.StressTest;
import pl.edu.agh.dynamic.map.test.Test1;
import pl.edu.agh.dynamic.map.test.Test2;
import pl.edu.agh.dynamic.map.test.Test3;
import pl.edu.agh.dynamic.map.test.Test4;
import pl.edu.agh.dynamic.map.test.Test5;

public class Application {

	public static Properties prop = new Properties();
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

	public static Logger log;
	private static ConnectionManager connectionManager;
	private static final String OSMOSIS = "Osmosis";
	private static final String RDNR = "RDNR";
	public static OsmosisDao osmosisDao;
	public static RdnrDao rdnrDao;
	public static StatsCollector statsCollector = new StatsCollector();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		initLoggers();
		initJDBC();

//		new Test1().runTest();
//		new Test2().runTest();
//		new Test3().runTest();
//		new Test4().runTest();
//		new Test5().runTest();
//
//		
//		new StressTest().runTest();
		
		new SpecialTest().runTest();
		osmosisDao.getConnection().close();
		rdnrDao.getConnection().close();
		log.info("All tests executed successfully :)");
		System.exit(0);
	}

	private static void initLoggers() {
		BasicConfigurator.configure();
		log = Logger.getRootLogger();
	}

	private static void initJDBC() throws SQLException, ClassNotFoundException {

		String host = prop.getProperty("osmosis.host");
		String username = prop.getProperty("osmosis.username");
		String password = prop.getProperty("osmosis.password");
		String port = prop.getProperty("osmosis.port");
		String db = prop.getProperty("osmosis.db");

		connectionManager = new ConnectionManager();

		log.debug("jdbc:postgresql://" + host + ":" + port + "/" + db + ":" + username + ":" + password);
		connectionManager.createConnection(OSMOSIS, "jdbc:postgresql://" + host + ":" + port + "/" + db, username, password);
		log.info("Osmosis connection established successfully!");

		host = prop.getProperty("rdnr.host");
		username = prop.getProperty("rdnr.username");
		password = prop.getProperty("rdnr.password");
		port = prop.getProperty("rdnr.port");
		db = prop.getProperty("rdnr.db");

		log.debug("jdbc:postgresql://" + host + ":" + port + "/" + db + ":" + username + ":" + password);
		connectionManager.createConnection(RDNR, "jdbc:postgresql://" + host + ":" + port + "/" + db, username, password);
		log.info("RDNR connection established successfully!");

		osmosisDao = new OsmosisDao(connectionManager.getConnection(OSMOSIS));
		rdnrDao = new RdnrDao(connectionManager.getConnection(RDNR));
	}

}
