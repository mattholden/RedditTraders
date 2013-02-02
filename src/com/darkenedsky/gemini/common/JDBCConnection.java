package com.darkenedsky.gemini.common;
import java.sql.*;



/**
 * Defines a connection to a JDBC resource. Must be derived for each SQL connection type (PostgreSQL, MySQL, etc.).
 * @author Jaeden
 *
 */
public class JDBCConnection implements SQLDriverNames  {
	
	public void setAutoCommit(boolean ac) throws SQLException { 
		connection.setAutoCommit(ac);
	}
	
	public void commit() throws SQLException { 
		connection.commit();
	}
	
	public void rollback() throws SQLException { 
		connection.rollback();
	}
	
	    /** Connection to the DB */
	    protected Connection connection;
	   
	    /** Creates a new instance of JDBCConnection
        @param sser user name
        @param pass password
        @param dbPath path to the database
        @param _driver FQN for JDBC driver
        */
	    public JDBCConnection(String user, String pass, String dbPath, String _driver) throws SQLException, ClassNotFoundException
	    {	    
	       Class.forName(_driver); 
	        connection = DriverManager.getConnection(dbPath, user, pass);
	     }

    /** Close the connection */
    public void close() throws SQLException { if (connection != null) connection.close(); }  

    /** Get the count of rows in a ResultSet
     *  @param rs ResultSet to count rows on
     *  @return number of rows (might be zero) */
    public static int getRowCount(ResultSet set) throws Exception
    {
        if (set == null) return -1;

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
    

	
	
    /** Build a prepared statement
     *  
     * @param statement the query to build
     * @return a prepared statement object
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String statement) throws SQLException { 
    	return connection.prepareStatement(statement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    	
    }
    
    public ResultSet executeQuery(String statement) throws SQLException { 
    	PreparedStatement ps = prepareStatement(statement);
    	return ps.executeQuery();
    }
    
    public boolean execute(String statement) throws SQLException { 
    	PreparedStatement ps = prepareStatement(statement);
    	return ps.execute();
    }
  
}
