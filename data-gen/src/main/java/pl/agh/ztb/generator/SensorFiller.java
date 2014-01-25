package pl.agh.ztb.generator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import pl.agh.ztb.connector.OsmosisConnector;
import pl.agh.ztb.connector.RdnrConnector;

public class SensorFiller {
	private static final Logger log = Logger.getLogger(SensorFiller.class);
	private static final RdnrConnector rdnr = RdnrConnector.getInstance();

	private static final String SENSORS_ID_SELECT = "SELECT id FROM param_monitoringparameterinstance;";
	
	private static final String INSERT_PARAM = "INSERT INTO param_monitoringparametervalue (confidence, precision, valid_until, time_measured,value,instance) VALUES (1,'1',?,?,?,?);";
	
	private static final String INSERT_PARAM_META = "INSERT INTO meta_param_pk_table_class (pk, tbl, cls) VALUES (?,'param_monitoringparametervalue','http://mapserv.kt.agh.edu.pl/ontologies/param.owl#param.MonitoringaParameterValue');";

	public static void clean() throws SQLException{

		rdnr.getConnection().createStatement().execute("DELETE FROM param_monitoringparametervalue;");
		rdnr.getConnection().createStatement().execute("DELETE FROM meta_param_pk_table_class WHERE tbl = 'param_monitoringparametervalue';");
	}
	Long random(){
		return RandomUtils.nextLong()%80L;
	}
	
	public void generate() throws SQLException {
	
		ResultSet rs = rdnr.execute(SENSORS_ID_SELECT);
		Timestamp date = new Timestamp(Calendar.getInstance().getTimeInMillis());
		while(rs.next()){
			Long instanceId = rs.getLong("id");
			log.debug("creating data for "+instanceId);
			Long valueId = insertParam(date, random(), instanceId);
			insertParamMeta(valueId);
			log.debug("data created "+instanceId);
		}
	}
	Long insertParamMeta(Long valueId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(INSERT_PARAM_META, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, valueId);

		return rdnr.insert(statement);
	}
	
	Long insertParam(Timestamp date, Long value, Long instanceId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(INSERT_PARAM, Statement.RETURN_GENERATED_KEYS);
		statement.setTimestamp(1, date);
		statement.setTimestamp(2, date);
		statement.setLong(3, value);
		statement.setLong(4, instanceId);

		return rdnr.insert(statement);
	}
}
