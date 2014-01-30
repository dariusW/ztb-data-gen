package pl.agh.ztb.generator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.postgis.LinearRing;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import pl.agh.ztb.connector.OsmosisConnector;

public class LaneGenerator {
	private static final Logger log = Logger.getLogger(LaneGenerator.class);
	private static final OsmosisConnector osmosis = OsmosisConnector.getInstance();

	private static final String CROSS_NODES_SEARCH_QYERY = "select node_id as id from way_nodes n  join way_tags t on t.way_id = n.way_id  where t.k = 'highway' and t.v in ('primary','secondary','tertiary','trunk','unclassified') group by node_id having count(node_id) = 4";

	private static final String WAY_ID_SEARCH_FOR_NODE = "select way_segments.id as segmentId, way_id as idd, node1_id as n1, node2_id as n2, n1.geom as g1, n2.geom as g2 from way_segments join krakow.nodes n1 on n1.id = node1_id join krakow.nodes n2 on n2.id=node2_id where node1_id = %d OR node2_id = %d";

	private static final String SNODE_INSERT = "INSERT INTO smnodes (geom) VALUES (ST_GeomFromText(?, 4326));";
	
	private static final String LANE_INSERT = "INSERT INTO lanes (way_id) VALUES (?);";
	
	private static final String LANE_SMNODE_INSERT = "INSERT INTO lane_smnodes (lane_id, smnode_id, sequence_id) VALUES (?, ?, ?);";
	
	private static final String CREOSSROAD_INSERT = "INSERT INTO crossroads (name, boundary) VALUES (?, ST_GeomFromText(?, 4326));";
	
	private static final String WAY_SEGMENTS_CROSS = "INSERT INTO krakow.crossroad_ways(crosroad_id, way_segment_id) VALUES (?, ?);";
	
	public static void clean() throws SQLException{

		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.crossroad_ways;");
		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.crossroads;");
		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.lane_smnodes WHERE lane_id > 25 ;");
		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.lanes WHERE \"number\" IS NULL;");
		osmosis.getConnection().createStatement().execute("DELETE FROM krakow.smnodes WHERE id > 25 ;");

	}
	
	public void generate() {
		log.debug("Start");
		try {
			//search for nodes with more then one way attached
			List<Long> nodesIds = new ArrayList<Long>();
			ResultSet result1 = osmosis.execute(CROSS_NODES_SEARCH_QYERY);

			while (result1.next()) {
				nodesIds.add(result1.getLong("id"));
			}
			log.debug("nodes found "+nodesIds);
			
			//get ways 
			for(Long nodeId:nodesIds){				
				Map<Long, PGgeometry> wayIds = new HashMap<Long, PGgeometry>();
				ResultSet result2 = osmosis.execute(String.format(WAY_ID_SEARCH_FOR_NODE, nodeId,nodeId));

				List<Long> waySegmentIds = new ArrayList<Long>();
				while (result2.next()) {
					waySegmentIds.add(result2.getLong("segmentId"));
					Long id = result2.getLong("idd");
					Long n1 = result2.getLong("n1");
					PGgeometry geom = null;
					if(n1.equals(id)){
						geom = (PGgeometry) result2.getObject("g1");
					} else {
						geom = (PGgeometry) result2.getObject("g2");						
					}
					
					wayIds.put(id,geom);
				}
				log.debug("ways for node "+nodeId+"  found "+wayIds);
				
				boolean first = true;
				for(Long laneStartWayId : wayIds.keySet()){		
					Long laneId = insertLane(laneStartWayId);
					log.debug("Lane created for way "+laneStartWayId+" id: "+laneId);
					int size = wayIds.keySet().size();
					Point[] points = new Point[size+1];
					int sequence = 0;
					for(Long wayId : wayIds.keySet()){
						PGgeometry geom = wayIds.get(wayId);
						points[sequence] = (Point)geom.getGeometry();
						Long smnode = insertSmnode(geom);
						insertLaneSmnode(laneId, smnode, sequence);
						sequence++;						
					}
					points[sequence] = points[0];
					if(first){
						String crossroadName = genName();
						Long crossId = insertCrossroad(crossroadName, points);
						log.debug("Crossroad "+crossroadName+ " "+ crossId);
						
						for(Long segmentId: waySegmentIds){
							insertCrossWays(crossId, segmentId);
						}
						first = false;
					}
					
				}
				
								
			}
			
		} catch (SQLException e) {
			log.warn(e.getMessage());
		}

		log.debug("end");
	}
	

	Long insertCrossWays(Long crossId, Long waySegmentId) throws SQLException{
		PreparedStatement  statement=osmosis.getConnection().prepareStatement(WAY_SEGMENTS_CROSS, Statement.RETURN_GENERATED_KEYS);
        statement.setLong(1,crossId);
        statement.setLong(2, waySegmentId);

        return osmosis.insert(statement);
	}
	
	Long insertCrossroad(String name, Point[] points) throws SQLException{
		Polygon polygon = new Polygon(new LinearRing[] {new LinearRing(points)});
		
		PreparedStatement  statement=osmosis.getConnection().prepareStatement(CREOSSROAD_INSERT, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, name);
        statement.setString(2, polygon.toString());

        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        } else {
            throw new SQLException("Creating user failed, no generated key obtained.");
        }
	}
	
	Long insertSmnode(PGgeometry geom) throws SQLException{
		PreparedStatement  statement=osmosis.getConnection().prepareStatement(SNODE_INSERT, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, geom.toString());

        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        } else {
            throw new SQLException("Creating user failed, no generated key obtained.");
        }
	}
	

	Long insertLane(Long wayId) throws SQLException{
		PreparedStatement  statement=osmosis.getConnection().prepareStatement(LANE_INSERT, Statement.RETURN_GENERATED_KEYS);
        statement.setLong(1, wayId);

        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        } else {
            throw new SQLException("Creating user failed, no generated key obtained.");
        }
	}
	
	String genName(){
		return RandomStringUtils.randomAlphabetic(10);
	}
	

	Long insertLaneSmnode(Long laneId, Long smNodeId, int seq) throws SQLException{
		PreparedStatement  statement=osmosis.getConnection().prepareStatement(LANE_SMNODE_INSERT, Statement.RETURN_GENERATED_KEYS);
        statement.setLong(1, laneId);
        statement.setLong(2, smNodeId);
        statement.setInt(3, seq);

        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating user failed, no rows affected.");
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        } else {
            throw new SQLException("Creating user failed, no generated key obtained.");
        }
	}

}
