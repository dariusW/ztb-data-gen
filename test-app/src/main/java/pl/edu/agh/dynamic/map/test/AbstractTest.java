package pl.edu.agh.dynamic.map.test;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import pl.edu.agh.dynamic.map.Application;
import pl.edu.agh.dynamic.map.dao.OsmosisDao;
import pl.edu.agh.dynamic.map.dao.RdnrDao;
import pl.edu.agh.dynamic.map.log.StatsCollector;

public abstract class AbstractTest {

	protected static final Logger log = Application.log;
	protected static final StatsCollector statsCollector = Application.statsCollector;
	protected static OsmosisDao osmosisDao = Application.osmosisDao;
	protected static RdnrDao rdnrDao= Application.rdnrDao;
	
	public void run() throws SQLException{
        log.info("=== Executing "+name()+" ===");
        statsCollector.startActivity(name());
        
        test();       

        statsCollector.finishActivity(name());
        log.info("Test 6 executed successfully.");
        log.info(statsCollector.getLogFor(name()));
	}

	protected abstract String name();
	protected abstract void test() throws SQLException;
}
