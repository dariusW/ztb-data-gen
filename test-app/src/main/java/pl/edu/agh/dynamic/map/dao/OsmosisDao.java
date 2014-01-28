package pl.edu.agh.dynamic.map.dao;

import org.postgis.Point;
import org.postgis.Polygon;
import org.postgresql.PGConnection;
import pl.edu.agh.dynamic.map.model.Crossroad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OsmosisDao {

    private static final String SELECT_CROSSROADS_IN_RANGE_QUERY = "SELECT * from crossroads c where ST_Distance_sphere(c.boundary,ST_GeomFromText(?, 4326)) < ?;";

    private static final String SELECT_CROSSROADS_IN_AREA_QUERY = "SELECT * from crossroads c where ST_Contains(ST_GeomFromText(?, 4326),c.boundary);";

    private Connection connection;

    private PreparedStatement crossroadsNearPointStatement = null;

    private PreparedStatement crossroadsInAreaStatement = null;

    public OsmosisDao(Connection connection) {
        this.connection = connection;
        try {
            ((PGConnection) connection).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET search_path TO krakow, public;");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public List<Crossroad> getCrossroadsNearPoint(Point point, int distance) throws SQLException {
        if (crossroadsNearPointStatement == null) {
            crossroadsNearPointStatement = connection.prepareStatement(SELECT_CROSSROADS_IN_RANGE_QUERY);
        }
        crossroadsNearPointStatement.setString(1, point.toString());
        crossroadsNearPointStatement.setInt(2, distance);

        ResultSet resultSet = crossroadsNearPointStatement.executeQuery();
        List<Crossroad> crossroadList = createCrossroadsFromResultSet(resultSet);

        return crossroadList;
    }

    private List<Crossroad> createCrossroadsFromResultSet(ResultSet resultSet) throws SQLException {
        List<Crossroad> crossroadList = new ArrayList<Crossroad>();
        while(resultSet.next()) {
            Crossroad crossroad = new Crossroad();
            crossroad.setId(resultSet.getLong("ID"));
            crossroad.setName(resultSet.getString("NAME"));
            crossroadList.add(crossroad);
        }
        return crossroadList;
    }

    public List<Crossroad> getCrossroadsInArea(Polygon area) throws SQLException {
        if(crossroadsInAreaStatement == null) {
            crossroadsInAreaStatement = connection.prepareStatement(SELECT_CROSSROADS_IN_AREA_QUERY);
        }

        crossroadsInAreaStatement.setString(1, area.toString());
        ResultSet resultSet = crossroadsInAreaStatement.executeQuery();
        List<Crossroad> crossroadList = createCrossroadsFromResultSet(resultSet);

        return crossroadList;
    }

	
	public Connection getConnection() {
		return connection;
	}

	
}
