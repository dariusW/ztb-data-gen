package pl.edu.agh.dynamic.map.test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;

public class Test4 extends AbstractTest {

	@Override
	protected String name() {
		return "Test 4";
	}

	private int i = 0;

	@Override
	protected void test() throws SQLException {
		osmosisDao.getConnection().createStatement().execute("SET search_path TO krakow, public;");

		String pointA = "POINT(19.964204 50.071905)";

		String dest1 = "POINT(19.973701 50.077868)";
		String dest2 = "POINT(20.0830667 50.1605263)";
		String dest3 = "POINT(19.974204 50.061905)";
		String dest4 = "POINT(19.950651 50.061822)";
		String dest5 = "POINT(19.935109 50.042258)";

		find(pointA, dest1);
		find(pointA, dest2);
		find(pointA, dest3);
		find(pointA, dest4);
		find(pointA, dest5);
	}

	private static double delta = 0.01;

	private void find(String dPointA, String dDest1) {
		try {
			Point pointA = new Point(dPointA);
			Point dest1 = new Point(dDest1);

			Point[] p = new Point[] { new Point(pointA.x - delta, pointA.y - delta),//
					new Point(pointA.x, pointA.y), //
					new Point(pointA.x + delta, pointA.y + delta),//
					new Point(dest1.x - delta, dest1.y - delta),//
					new Point(dest1.x, dest1.y),//
					new Point(dest1.x + delta, dest1.y + delta),//
					new Point(pointA.x - delta, pointA.y - delta), };
			LinearRing[] linearRing = new LinearRing[] { new LinearRing(p) };

			Polygon mask = new Polygon(linearRing);

			log.info("Testing road from A " + dPointA + " to destination nb" + i + " using mask " + mask.toString());

			String getLanesAndStreetsForpath = "SELECT distinct l.lane_id id, w.v sname FROM krakow.smnodes sm JOIN krakow.lane_smnodes l ON l.smnode_id = sm.id JOIN krakow.lanes lane ON lane.id = l.lane_id JOIN krakow.way_tags w ON w.way_id = lane.way_id WHERE w.k = 'name' AND ST_Intersects(sm.geom,ST_GeomFromText(?, 4326));";
			log.info("Executing sql " + getLanesAndStreetsForpath);
			PreparedStatement st = osmosisDao.getConnection().prepareStatement(getLanesAndStreetsForpath);
			st.setString(1, mask.toString());

			start();
			ResultSet rs = st.executeQuery();
			stop();
			int count = 0;
			StringBuilder sb = new StringBuilder(
					"SELECT DISTINCT ON (label) label, time_measured,value FROM param_monitoringparametervalue v JOIN param_monitoringparameterinstance i ON (v.instance=i.id) JOIN param_instanceassignment a ON (i.assignment=a.id) JOIN param_instanceassignment_sm_lane l ON (l.param_instanceassignment=a.id) WHERE sm_lane IN (");

			List<Long> lanesIds = new ArrayList<Long>();
			StringBuilder streetPathSB = new StringBuilder("Path from A to dest nb" + i + ": ");
			while (rs.next()) {
				Long id = rs.getLong("id");
				String sname = rs.getString("sname");
				streetPathSB.append(sname);
				streetPathSB.append(" -> ");
				if (count > 0) {
					sb.append(",");
				}
				sb.append(id);
				count++;
				lanesIds.add(id);
			}
			sb.append(") ORDER BY label, time_measured DESC;");
			log.info("Lanes for road A to dest" + i + " found: " + count);
			log.info("Static map data fetched.");

			String attachedMonitorSensorValuesSQL = sb.toString();
			log.info("Sensor data from dynamic map query: " + attachedMonitorSensorValuesSQL);
			PreparedStatement statement = rdnrDao.getConnection().prepareStatement(attachedMonitorSensorValuesSQL);

			log.info("sensor data query: " + attachedMonitorSensorValuesSQL);
			count = 0;
			if (lanesIds.isEmpty() == false) {
				start();
				rs = statement.executeQuery();
				stop();
				while (rs.next()) {
					count++;
					log.info(rs.getString(1) + "|" + rs.getDate(2).toString() + "|" + rs.getString(3));
				}
			}
			log.info("Monitor data found: " + count);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		i++;

	}

}
