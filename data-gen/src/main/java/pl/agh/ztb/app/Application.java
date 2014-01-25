package pl.agh.ztb.app;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import pl.agh.ztb.connector.OsmosisConnector;
import pl.agh.ztb.connector.RdnrConnector;
import pl.agh.ztb.generator.LaneGenerator;
import pl.agh.ztb.generator.SensorFiller;
import pl.agh.ztb.generator.SensorGenerator;
import asg.cliche.CLIException;
import asg.cliche.Command;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;

public class Application {

	private static final Logger log = Logger.getLogger(Application.class);

	private static final OsmosisConnector osmosis = OsmosisConnector.getInstance();
	private static final RdnrConnector rdnr = RdnrConnector.getInstance();

	public static void main(String[] args) throws IOException, CLIException {
		Shell sh = ShellFactory.createConsoleShell("hello", "", new Application());
		sh.processLine("help");
		sh.commandLoop();
	}

	@Command
	public void help(){
		System.out.println("Commands: osmosis to generate osmosisDB data | rdnr to generate sensors in rdnrDB | sensors to fill sensors with fresh sample data | clean to clean DBs | exit");
	}
	
	
	@Command
	public void osmosis() throws ClassNotFoundException, SQLException {
		osmosis.connect();

		LaneGenerator lanes = new LaneGenerator();
		lanes.generate();

		osmosis.disconnect();
	}

	@Command
	public void rdnr() throws ClassNotFoundException, SQLException {
		osmosis.connect();
		rdnr.connect();

		SensorGenerator lanes = new SensorGenerator();
		lanes.generate();

		osmosis.disconnect();
		rdnr.disconnect();
	}

	@Command
	public void sensors() throws ClassNotFoundException, SQLException {
		osmosis.connect();
		rdnr.connect();

		SensorFiller lanes = new SensorFiller();
		lanes.generate();

		osmosis.disconnect();
		rdnr.disconnect();
	}


	@Command
	public void clean() throws ClassNotFoundException, SQLException {

		osmosis.connect();

		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.crossroads;");
		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.lanes WHERE \"number\" IS NULL;");
		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.smnodes WHERE id > 25 ;");
		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.lane_smnodes WHERE lane_id > 25 ;");

		osmosis.disconnect();

	}

	@Command
	public void exit() {
		System.exit(0);
	}

}
