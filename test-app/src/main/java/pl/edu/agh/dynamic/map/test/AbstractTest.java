package pl.edu.agh.dynamic.map.test;

import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;

import pl.edu.agh.dynamic.map.Application;
import pl.edu.agh.dynamic.map.dao.OsmosisDao;
import pl.edu.agh.dynamic.map.dao.RdnrDao;
import pl.edu.agh.dynamic.map.log.StatsCollector;

public abstract class AbstractTest implements Runnable{

	protected static final Logger log = Application.log;
	protected static final StatsCollector statsCollector = Application.statsCollector;
	protected static OsmosisDao osmosisDao = Application.osmosisDao;
	protected static RdnrDao rdnrDao= Application.rdnrDao;
	
	@Override
	public final void run() {
		try {
			runTest();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void runTest() throws SQLException{
        log.warn("=== Executing "+name()+" ===");
        statsCollector.startActivity(name());
        
        test();       

        statsCollector.finishActivity(name());
        log.warn(name()+" executed successfully.");
        log.info(statsCollector.getLogFor(name()));
	}

	protected abstract String name();
	protected abstract void test() throws SQLException;
	
	private Long start;
	protected void start(){
		start = Calendar.getInstance().getTimeInMillis();
	}
	protected Long stop(){
		Long time = Calendar.getInstance().getTimeInMillis()-start;
		log.info("Request time: "+time+"ms");
		return time;
	}
}
