package pl.agh.ztb.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.postgis.PGgeometry;
import org.postgresql.PGConnection;

public class RdnrConnector extends Connector {

	private static final RdnrConnector rdnr = new RdnrConnector();

	public static RdnrConnector getInstance() {
		return rdnr;
	}

	private static final Logger log = Logger.getLogger(RdnrConnector.class);

	public Connection connect() throws ClassNotFoundException, SQLException {
		log.debug("-------- PostgreSQL " + "JDBC Connection Testing ------------");
		Class.forName("org.postgresql.Driver");
		String host = "localhost";
		String username = "postgres";
		String password = "admin";
		String port = "5432";
		String db = "rdnr";

		log.debug("jdbc:postgresql://" + host + ":" + port + "/" + db + ":" + username + ":" + password);
		connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + db, username, password);
		((PGConnection) connection).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));

		log.debug("Connected");
		return connection;
	}

	@Override
	public void disconnect() throws SQLException {
		super.disconnect();
		log.debug("Discnected");
	}
	
	

}
