package com.darkenedsky.gemini.common;

import java.sql.*;

/**
 * Defines a connection to a JDBC resource. Must be derived for each SQL
 * connection type (PostgreSQL, MySQL, etc.).
 * 
 * @author Matt Holden (matt@mattholden.com)
 * 
 */
public class JDBCConnection {

	/** Connection to the DB */
	protected Connection connection;

	/**
	 * Toggles auto-commit on a connection
	 * 
	 * @param ac
	 *            true if autocommit is to be on
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean ac) throws SQLException {
		connection.setAutoCommit(ac);
	}

	/**
	 * Commits a transaction (only needed if autocommit is off)
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		connection.commit();
	}

	/**
	 * Roll back a transaction not yet committed
	 * 
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		connection.rollback();
	}

	
	/**
	 * Creates a new instance of JDBCConnection
	 * 
	 * @param user
	 *            user name
	 * @param pass
	 *            password
	 * @param dbPath
	 *            path to the database
	 * @param _driver
	 *            FQN for JDBC driver
	 */
	public JDBCConnection(String user, String pass, String dbPath,
			String _driver) throws SQLException, ClassNotFoundException {
		Class.forName(_driver);
		connection = DriverManager.getConnection(dbPath, user, pass);
	}

	/**
	 * Close the database connection
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (connection != null)
			connection.close();
	}

	/**
	 * Get the count of rows in a ResultSet
	 * 
	 * @param set
	 *            ResultSet to count rows on
	 * @return number of rows (might be zero)
	 */
	public static int getRowCount(ResultSet set) throws Exception {
		if (set == null)
			return -1;

		// the current row
		int current = set.getRow();
		if (current == -1) {
			set.first();
			current = 0;
		}

		// special case - no records
		if (set.last() == false)
			return 0;

		// get the value to return
		int last = set.getRow();

		// put it back where it was
		set.absolute(current);

		return last;
	}

	/**
	 * Build a prepared statement
	 * 
	 * @param statement
	 *            the query to build
	 * @return a prepared statement object
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(String statement)
			throws SQLException {
		return connection.prepareStatement(statement,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

	}


}
