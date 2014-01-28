package pl.edu.agh.dynamic.map.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Khajiit
 * Date: 25.01.14
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionManager {

    private Map<String, Connection> connectionMap = new HashMap<String, Connection>();

    public ConnectionManager() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection createConnection(String connName, String connUrl, String connUser, String connPassword) throws SQLException {
        Connection connection = DriverManager.getConnection(connUrl, connUser, connPassword);
        connectionMap.put(connName, connection);
        return connection;
    }

    public Connection getConnection(String connectionName) throws SQLException, ClassNotFoundException {
        return connectionMap.get(connectionName);
    }

}
