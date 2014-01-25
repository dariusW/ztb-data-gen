package pl.agh.ztb.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.postgresql.PGConnection;

public class OsmosisConnector extends Connector {

	private static final OsmosisConnector osmosis = new OsmosisConnector();
	
	public static OsmosisConnector getInstance(){
		return osmosis;
	}
	
	private static final Logger log = Logger.getLogger(OsmosisConnector.class);

	public static final String CROSSROAD_WAYS ="CROSSROAD_WAYS";
	public static final String CROSSROADS ="CROSSROADS";
	public static final String CROSSROADS_ID_SEQ ="CROSSROADS_ID_SEQ";
	public static final String GEOGRAPHY_COLUMNS ="GEOGRAPHY_COLUMNS";
	public static final String GEOMETRY_COLUMNS ="GEOMETRY_COLUMNS";
	public static final String LANE_PROPERTIES ="LANE_PROPERTIES";
	public static final String LANE_PROPERTY_TYPES ="LANE_PROPERTY_TYPES";
	public static final String LANE_SMNODES ="LANE_SMNODES";
	public static final String LANES ="LANES";
	public static final String LANES_ID_SEQ ="LANES_ID_SEQ";
	public static final String NODE_TAGS ="NODE_TAGS";
	public static final String NODES ="NODES";
	public static final String RELATION_MEMBERS ="RELATION_MEMBERS";
	public static final String RELATION_TAGS ="RELATION_TAGS";
	public static final String RELATIONS ="RELATIONS";
	public static final String ROADSIGN_TYPES ="ROADSIGN_TYPES";
	public static final String ROADSIGNS ="ROADSIGNS";
	public static final String ROADSIGNS_ID_SEQ ="ROADSIGNS_ID_SEQ";
	public static final String SCHEMA_INFO ="SCHEMA_INFO";
	public static final String SMNODES ="SMNODES";
	public static final String SMNODES_ID_SEQ ="SMNODES_ID_SEQ";
	public static final String SPATIAL_REF_SYS ="SPATIAL_REF_SYS";
	public static final String TURN_PROPERTIES ="TURN_PROPERTIES";
	public static final String TURN_PROPERTY_TYPES ="TURN_PROPERTY_TYPES";
	public static final String TURN_VIAS ="TURN_VIAS";
	public static final String TURNS ="TURNS";
	public static final String TURNS_ID_SEQ ="TURNS_ID_SEQ";
	public static final String UNITS ="UNITS";
	public static final String UNITS_ID_SEQ ="UNITS_ID_SEQ";
	public static final String USERS ="USERS";
	public static final String WAY_NODES ="WAY_NODES";
	public static final String WAY_SEGMENTS ="WAY_SEGMENTS";
	public static final String WAY_SEGMENTS_ID_SEQ ="WAY_SEGMENTS_ID_SEQ";
	public static final String WAY_TAGS ="WAY_TAGS";
	public static final String WAYS ="WAYS";

	public Connection connect() throws ClassNotFoundException, SQLException {
		log.debug("-------- PostgreSQL" +"JDBC Connection Testing ------------");
		Class.forName("org.postgresql.Driver");
		String host ="localhost";
		String username ="postgres";
		String password ="admin";
		String port ="5432";
		String db ="osmosis";

		log.debug("jdbc:postgresql://" + host +":" + port +"/" + db +":" + username +":" + password);
		connection = DriverManager.getConnection("jdbc:postgresql://" + host +":" + port +"/" + db, username, password);
		((PGConnection) connection).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));
		try{
			execute("SET search_path TO krakow, public;");	
		} catch(SQLException e){
			log.debug(e);
		}
		
		log.debug("Connected");
		return connection;
	}

	@Override
	public void disconnect() throws SQLException {
		super.disconnect();
		log.debug("Disconnected");
	}

}
