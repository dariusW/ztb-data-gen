package pl.edu.agh.dynamic.map.test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;

import pl.edu.agh.dynamic.map.Application;

public class StressTest extends AbstractTest {

	@Override
	protected String name() {
		return "STRESS TEST";
	}

	@Override
	protected void test() throws SQLException {
		log.info("-------------------------------------ATTENTION STRESS TESTING---------------------");
		String pools = Application.prop.getProperty("fixed.pools");
		String tries = Application.prop.getProperty("stress.steps");
		String time = Application.prop.getProperty("stress.limit.time");
		int fixedPools = 2;
		int steps = 2;
		int ttime = 2;
		if (pools != null) {
			fixedPools = Integer.parseInt(pools);
		}
		if (tries != null) {
			steps = Integer.parseInt(tries);
		}
		if (time != null) {
			ttime = Integer.parseInt(time);
		}
		ExecutorService executorService = Executors.newFixedThreadPool(fixedPools);

		List<AbstractTest> tests = new LinkedList<AbstractTest>();

		for (int i = 0; i < steps; i++) {
			tests.add(new Test1());
			tests.add(new Test2());
			tests.add(new Test3());
			tests.add(new Test4());
			tests.add(new Test5());
		}

		log.setLevel(Level.WARN);
		for (AbstractTest t : tests) {
			executorService.execute(t);
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(ttime, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			log.error("STRESS TEST FAILED it took longer than "+ttime+" minutes");
		} finally{
			log.setLevel(Level.ALL);
		}
		
		log.info("-------------------------------------ATTENTION STRESS TESTING---------------------");
	}

}
