package pl.agh.ztb.connector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public abstract class Connector {

	protected static Properties prop = new Properties();
	private static InputStream input = null;

	static {
		try {

			input = new FileInputStream("config.properties");

			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	Connection connection = null;
	Statement statement = null;

	public Statement getStatement() throws SQLException {
		return statement = connection.createStatement();
	}

	public ResultSet execute(String query) throws SQLException {
		statement = connection.createStatement();
		return statement.executeQuery(query);
	}

	public void disconnect() throws SQLException {
		connection.close();
	}

	public int update(PreparedStatement statement) throws SQLException {
		return statement.executeUpdate();

	}

	public Long insert(PreparedStatement statement) throws SQLException {
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

	abstract public Connection connect() throws ClassNotFoundException, SQLException;
}
