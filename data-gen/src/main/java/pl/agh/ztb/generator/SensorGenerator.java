package pl.agh.ztb.generator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

import pl.agh.ztb.connector.OsmosisConnector;
import pl.agh.ztb.connector.RdnrConnector;

public class SensorGenerator {
	private static final Logger log = Logger.getLogger(SensorGenerator.class);
	private static final OsmosisConnector osmosis = OsmosisConnector.getInstance();
	private static final RdnrConnector rdnr = RdnrConnector.getInstance();

	private static final String CROSS_ROAD = "SELECT id, boundary FROM krakow.crossroads";

	private static final String LANES_ID_QUERY = "SELECT distinct l.lane_id id FROM krakow.smnodes s JOIN krakow.lane_smnodes l ON s.id = l.smnode_id WHERE ST_Intersects(ST_GeomFromText('%s'),geom) =true;";

	private static final String IA_INSERT = "INSERT INTO param_instanceassignment(crossroads) VALUES (?);";

	private static final String IA_META_INSERT = "INSERT INTO meta_param_pk_table_class (pk, tbl, cls) VALUES (?,'param_instanceassignment','http://mapserv.kt.agh.edu.pl/ontologies/param.owl#param.LaneInstanceAssignment');";

	private static final String MPI_INSERT = "INSERT INTO param_monitoringparameterinstance (time_interval,type,label,assignment,min_frequency,max_frequency,lifetime,time_created) VALUES (90,?,?,?,0.0166,0.0083,360,?);";

	private static final String MPI_META_INSERT = "INSERT INTO meta_param_pk_table_class (pk, tbl, cls) VALUES (?,'param_monitoringparameterinstance','http://mapserv.kt.agh.edu.pl/ontologies/param.owl#param.FixedMonitoringParameterInstance');";

	private static final String IA_LANE_INSERT = "INSERT INTO param_instanceassignment_sm_lane (param_instanceassignment,sm_lane) VALUES (?,?);";
	
	private static final String MPS_INSERT = "INSERT INTO param_monitoringparameterstate (parameter_instance,time_entered,type) VALUES (?,?,'http://mapserv.kt.agh.edu.pl/ontologies/param.owl#MonitoringParameterStateType.Active');";
	
	private static final String MPS_META_INSERT = "INSERT INTO meta_param_pk_table_class (pk, tbl, cls) VALUES (?,'param_monitoringparameterstate','http://mapserv.kt.agh.edu.pl/ontologies/param.owl#param.MonitoringaParameterState');";
	
	private static final String STATE_UPDATE = "UPDATE param_monitoringparameterinstance SET current_state=? WHERE id=?;";
	
	public void generate() throws SQLException {

		ResultSet rs = osmosis.execute(CROSS_ROAD);
		Map<Long, Geometry> crossroadBoundrys = new HashMap<>();
		while (rs.next()) {
			Long id = rs.getLong("id");
			PGgeometry boundry = (PGgeometry) rs.getObject("boundary");
			crossroadBoundrys.put(id, boundry.getGeometry());
		}
		log.debug("Executed crossroad search: " + crossroadBoundrys.keySet());

		for (Long crossId : crossroadBoundrys.keySet()) {
			Geometry geom = crossroadBoundrys.get(crossId);
			rs = osmosis.execute(String.format(LANES_ID_QUERY, geom.toString()));
			while (rs.next()) {
				Long laneId = rs.getLong("id");
				log.debug("processing lane:" + laneId + " for crossroad:" + crossId);
				String[] prefixes = new String[]{"K","V","T"};
				Long[] types = new Long[]{1L,2L,3L};
				for(int i=0; i<types.length; i++){
					createSensor(crossId, laneId, prefixes[i], types[i]);
					log.debug("Sensor "+prefixes[i]+" for type "+types[i]+"ready");
				}
			}
		}

	}
	private void createSensor(Long crossId, Long laneId, String prefix, Long type) throws SQLException {
		Long instanceassignmentId = insertInstanceAssignment(crossId);
		log.debug("instanceassignmentId " + instanceassignmentId);
		insertInstanceAssignmentMeta(instanceassignmentId);
		Long monitoringParameterInstanceId = insertMonitoringParameterInstance(type, getInstanceName(prefix), instanceassignmentId, new Date(Calendar.getInstance().getTimeInMillis()));
		log.debug("insertMonitoringParameterInstanceId " + monitoringParameterInstanceId);
		insertMonitoringParameterInstanceMeta(monitoringParameterInstanceId);
		insertInstanceAssignmentSmLane(instanceassignmentId, laneId);
		Long monitoringParameterStateId = insertMonitoringParameterState(monitoringParameterInstanceId, new Date(Calendar.getInstance().getTimeInMillis())); 
		log.debug("monitoringParameterStateId " + monitoringParameterStateId);
		insertMonitoringParameterStateMeta(monitoringParameterStateId);
	}
	void insertStateUpdate(Long monitoringParameterInstanceId,Long monitoringParameterStateId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(STATE_UPDATE);
		statement.setLong(1, monitoringParameterStateId);
		statement.setLong(2, monitoringParameterInstanceId);

		rdnr.update(statement);
	}

	Long insertMonitoringParameterStateMeta(Long monitoringParameterStateId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(MPS_META_INSERT, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, monitoringParameterStateId);

		return rdnr.insert(statement);
	}
	
	Long insertMonitoringParameterState(Long monitoringParameterInstance, Date date) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(MPS_INSERT, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, monitoringParameterInstance);
		statement.setDate(2, date);

		return rdnr.insert(statement);
	}
	Long insertInstanceAssignmentSmLane(Long instanceAssign, Long laneId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(IA_LANE_INSERT, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, instanceAssign);
		statement.setLong(2, laneId);

		return rdnr.insert(statement);
	}

	private String getInstanceName(String prefix) {
		return  prefix+"-"+RandomStringUtils.randomAlphanumeric(10);
	}

	Long insertMonitoringParameterInstanceMeta(Long monitoringParameterInstanceId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(MPI_META_INSERT, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, monitoringParameterInstanceId);

		return rdnr.insert(statement);
	}

	Long insertMonitoringParameterInstance(Long type, String name, Long instanceId, Date date) throws SQLException {

		PreparedStatement statement = rdnr.getConnection().prepareStatement(MPI_INSERT, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, type);
		statement.setString(2, name);
		statement.setLong(3, instanceId);
		statement.setDate(4, date);

		return rdnr.insert(statement);
	}

	Long insertInstanceAssignment(Long crossId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(IA_INSERT, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, crossId);

		return rdnr.insert(statement);
	}

	Long insertInstanceAssignmentMeta(Long iaId) throws SQLException {
		PreparedStatement statement = rdnr.getConnection().prepareStatement(IA_META_INSERT, Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, iaId);

		return rdnr.insert(statement);
	}
}
