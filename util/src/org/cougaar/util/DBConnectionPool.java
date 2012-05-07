/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

/*
 * Originally from delta/fgi package mil.darpa.log.alpine.delta.plugin;
 * Copyright 1997 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
 */

package org.cougaar.util;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.cougaar.bootstrap.SystemProperties;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * A database connection manager that creates pools of db connections
 * that can be reused to improve performance. DBConnectionPool should
 * be used in exactly the same way as the DriverManager class. That
 * is, call DBConnectionPool.getConnection() to get a Connection, use
 * the connection and close it. The same issues that exist for
 * Connections obtained from the DriverManager exist for Connections
 * obtained from DBConnectionPool. In particular, ResultSets should be
 * closed when you are finished with them so that you do not exceed
 * the limit on open ResultSets and Statements should be closed when
 * you are finished with them so you do not exceed the limit on open
 * Statements. In the same way as for Connections obtained from the
 * DriverManager, ResultSets that are not closed will be closed when
 * you close the Statement and Statements that are not closed will be
 * closed when you close the Connection.
 * 
 * @property org.cougaar.util.DBConnectionPool.maxConnections number
 * of simulataneous connections allowed per pool (10).
 * @property org.cougaar.util.DBConnectionPool.timeoutCheckInterval
 * milliseconds between checks to see if any old connections should be
 * collected (5000).
 * @property org.cougaar.util.DBConnectionPool.timeout milliseconds
 * that a connection must be idle in order to be collected by the
 * reaper (10000).
 * @note The property org.cougaar.util.DBConnectionPool.verbosity used
 * to be used to control logging behavior.  Now it uses standard cougaar
 * logging with the name org.cuogaar.util.DBConnectionPool at the log levels
 * ERROR, WARN and DEBUG.
 **/
public class DBConnectionPool {
  // keep a logger around for status.
  protected static final Logger logger = Logging.getLogger(DBConnectionPool.class);
  
  /**
   * A hash table relating the database URL and user to a connection
   * pool.
   */
  private static HashMap dbConnectionPools = new HashMap();

  /**
   * The separator inserted between the database URL and user to form
   * the key to the dbConnectionPools hash table.
   */
  private static final String SEP = "#";

  /** How often to run the timeout out checker, in milliseconds. */
  private static long TIMEOUT_CHECK_INTERVAL = 5*1000L;

  /**
   * How long to keep old connections before closing and releasing
   * them.
   *
   * The default value may be controlled with the
   * System Property org.cougaar.util.DBConnectionPool.maxConnections
   */
  private static long TIMEOUT = 10*1000L;

  /**
   * The number of cursors created after which we always release the
   * connection. This tries to avoid an accumulation of never released
   * cursors as the pooled connection is re-used.  Default is 5.
   */
  private static int MAX_CONNECTIONS = 5;

  static {
    String prefix = "org.cougaar.util.DBConnectionPool.";

    TIMEOUT_CHECK_INTERVAL = SystemProperties.getLong(prefix+"timeoutCheckInterval", TIMEOUT_CHECK_INTERVAL);
    TIMEOUT = SystemProperties.getLong(prefix+"timeout", TIMEOUT);
    MAX_CONNECTIONS = SystemProperties.getInt(prefix+"maxConnections", MAX_CONNECTIONS);
  }

  /**
   * Record the key for this pool for debugging purposes.
   */
  private String key;

  /**
   * Construct a new pool. Record the key for debugging.
   */
  private DBConnectionPool(String key, int max_connections) {
    this.key = key;
    this.maxConnections = (max_connections>0?max_connections:MAX_CONNECTIONS);
  }

  /**
   * Construct a new pool. Record the key for debugging.
   */
  private DBConnectionPool(String key) {
    this.key = key;
    this.maxConnections = MAX_CONNECTIONS;
  }

  int entryCounter = 0;

  /** how many clients are waiting for a connection in this pool? **/
  int waitingCounter = 0;

  /**
   * Inner class to record individual connections
   */
  class DBConnectionPoolEntry {
    int entryNumber = ++entryCounter;
    boolean defaultAutoCommit;

    /**
     * Construct an entry for a given connection that is not in use.
     */
    DBConnectionPoolEntry(Connection aConnection) throws SQLException {
      theConnection = aConnection;
      defaultAutoCommit = theConnection.getAutoCommit();
    }

    /**
     * Return the pool that this entry is in.
     */
    DBConnectionPool getDBConnectionPool() {
      return DBConnectionPool.this;
    }

    /**
     * Create a PoolConnection to return to the user.
     */
    Connection getPoolConnection() throws SQLException {
      return new PoolConnection(theConnection);
    }

    /**
     *  This assumes that the entry will no longer be used.  There is no mechanism
     *  to reopen theConnection.
     */
    private void destroy() {
      try {
        theConnection.close();
      } catch (SQLException sqle) {
        sqle.printStackTrace();
      }
    }
    
    /**
     * The connection of this entry.
     */
    Connection theConnection;

    /**
     * Indicates if this entry is in use.
     */
    boolean inUse = false;

    /**
     * Records when this connection was last used.
     */
    long lastUsed = System.currentTimeMillis();

    /**
     * This is a wrapper for a Connection object that delegates most
     * functions to the wrapped object, but interposes some processing
     * of its own to keep track of operations that have been done to
     * the connection. This record permits the connection to be
     * logically closed, but to remain actually open. In particular,
     * Statement objects created for the connection can be closed if
     * the program has not already done so. In this way, the
     * connection starts out in a clean state when reused.
     */
    class PoolConnection implements Connection {
      Connection c;
      boolean supportsTransactions;
      boolean closed = false;
      ArrayList statements = new ArrayList();
      PoolConnection(Connection realConnection) throws SQLException {
	c = realConnection;
	supportsTransactions = c.getMetaData().supportsTransactions();
        if (supportsTransactions) realConnection.setAutoCommit(defaultAutoCommit);
      }	  
      
      private void destroyPool() {
	DBConnectionPoolEntry entry = DBConnectionPoolEntry.this;
        // If this Connection is in the pool, then destroy the pool,
        // otherwise leave it alone.
        if (entry.getDBConnectionPool().containsConnection(entry))
          entry.getDBConnectionPool().destroyPool();
      }
      private void closeStatement(PoolStatement statement) throws SQLException {
	synchronized (statements) {
	  statement.theStatement.close();
	  statements.remove(statement);
	}
      }
      public Statement createStatement() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
	Statement statement = null;
        int rc = 0;             // retry count
        while (true) {
          try {
            statement = new PoolStatement(c.createStatement());
            statements.add(statement);
            return statement;
          } catch (SQLException sqle) {
            if (rc < maxRetries && isRetryable(sqle)) {
              rc++;
              try {
                Thread.sleep(retryTimeout);
              } catch (InterruptedException ie) {}
            } else {
              destroyPool();
              throw sqle;
            }
          }
        }
      }
      public Statement createStatement(int a, int b) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
	Statement statement = null;

        int rc = 0;             // retry count
        while (true) {
          try {
            statement = new PoolStatement(c.createStatement(a, b));
            statements.add(statement);
            return statement;
          } catch (SQLException sqle) {
            if (rc < maxRetries && isRetryable(sqle) ) {
              rc++;
              try {
                Thread.sleep(retryTimeout);
              } catch (InterruptedException ie) {}
            } else {
              destroyPool();
              throw sqle;
            }
          }
        }
      }
      public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
	PreparedStatement statement = null;
        try {
          statement = (PreparedStatement)new PoolPreparedStatement(c.prepareStatement(sql));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;
      }
      public PreparedStatement prepareStatement(String sql, int a, int b) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
	PreparedStatement statement = null;
        try {
          statement = (PreparedStatement)new PoolPreparedStatement(c.prepareStatement(sql, a, b));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;
      }
      public CallableStatement prepareCall(String sql) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
	CallableStatement statement = null;
        try {
          statement = new PoolCallableStatement(c.prepareCall(sql));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;
      }
      public CallableStatement prepareCall(String sql, int a, int b) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
	CallableStatement statement = null;
        try {
          statement = new PoolCallableStatement(c.prepareCall(sql, a, b));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;
      }
      public String nativeSQL(String sql) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        String str = null;
        try {
	  str = c.nativeSQL(sql);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return str;
      }
      public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        try {
	  c.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public boolean getAutoCommit() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        boolean b;
        try {
	  b = c.getAutoCommit();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return b;
      }
      public void commit() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        try {
	  if (supportsTransactions) c.commit();
	} catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public void rollback() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        try {
	  c.rollback();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public void finalize() {
	if (closed) return;	// Connection already closed (normal case)
	try {			// Connection was never closed (abandoned)
	  close();		// Simply close it now (it will get reused)
	}
	catch (SQLException e ) {
	  e.printStackTrace();	// Ignore exceptions
	}
      }
      public void close() throws SQLException {
	if (closed) return;
        closed = true;
        try {
	  if (supportsTransactions && !c.getAutoCommit()) c.commit();
	  synchronized (statements) {
	    while (statements.size() > 0) {
	      PoolStatement statement = (PoolStatement) statements.get(0);
	      closeStatement(statement);
	    }
	  }
        } catch (SQLException sqle) {
          // Since the entry that contains this Connection still exists, we
          // should explicitly kill this entry after the pool is destroyed
          destroyPool();
          DBConnectionPoolEntry entry = DBConnectionPoolEntry.this;
          entry.destroy();
          throw sqle;
        }
        // If the pool contains the entry, return it, otherwise, the pool has
        // been destroyed so destroy the entry
        DBConnectionPoolEntry entry = DBConnectionPoolEntry.this;
        if (entry.getDBConnectionPool().containsConnection(entry))
          entry.getDBConnectionPool().release(entry);
        else {
          entry.destroy();
        }
      }
      public boolean isClosed() throws SQLException {
	return closed;
      }
      public DatabaseMetaData getMetaData() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        DatabaseMetaData data = null;
        try {
          data = c.getMetaData();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return data;        
      }
      public void setReadOnly(boolean readOnly) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        try {
	  c.setReadOnly(readOnly);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public boolean isReadOnly() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        boolean b;
        try {
          b = c.isReadOnly();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return b;
      }
      public void setCatalog(String catalog) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        try {
	  c.setCatalog(catalog);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public String getCatalog() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        String str = null;
        try {
	  str = getCatalog();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return str;
      }
      public void setTransactionIsolation(int level) throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        try {
	  c.setTransactionIsolation(level);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public int getTransactionIsolation() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        int i;
        try {
	  i = c.getTransactionIsolation();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return i;
      }
      public SQLWarning getWarnings() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        SQLWarning warn = null;
        try {
	  warn = c.getWarnings();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return warn;
      }
      public void clearWarnings() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        try {
	  c.clearWarnings();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public java.util.Map getTypeMap() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
        java.util.Map map = null;
        try {
	  map = c.getTypeMap();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
        return map;
      }
      public void setTypeMap(java.util.Map map) throws SQLException {
	if (closed) throw new SQLException("Connection is closed");
        try {
	  c.setTypeMap(map);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      // begin jdk1.4 compatability
      public void setHoldability(int holdability) throws SQLException {
	if (closed) throw new SQLException("Connection is closed");
        try {
	  c.setHoldability(holdability);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public int getHoldability() throws SQLException {
	if (closed) throw new SQLException("Connection is closed");
        try {
	  return c.getHoldability();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Savepoint setSavepoint() throws SQLException {
	if (closed) throw new SQLException("Connection is closed");
        try {
	  return c.setSavepoint();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Savepoint setSavepoint(String s) throws SQLException {
	if (closed) throw new SQLException("Connection is closed");
        try {
	  return c.setSavepoint(s);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public void rollback(Savepoint savepoint) throws SQLException {
	if (closed) throw new SQLException("Connection is closed");
        try {
	  c.rollback(savepoint);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public void releaseSavepoint(Savepoint savepoint) throws SQLException {
	if (closed) throw new SQLException("Connection is closed");
        try {
	  c.releaseSavepoint(savepoint);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Statement createStatement(int a, int b, int p) throws SQLException
      {
        if (closed) throw new SQLException("Connection is closed");
	Statement statement = null;

        int rc = 0;             // retry count
        while (true) {
          try {
            statement = new PoolStatement(c.createStatement(a, b, p));
            statements.add(statement);
            return statement;
          } catch (SQLException sqle) {
            if (rc < maxRetries && isRetryable(sqle)) {
              rc++;
              try {
                Thread.sleep(retryTimeout);
              } catch (InterruptedException ie) {}
            } else {
              destroyPool();
              throw sqle;
            }
          }
        }
      }
      public PreparedStatement prepareStatement(String sql, int a, int b, int p)
        throws SQLException
      {
        if (closed) throw new SQLException("Connection is closed");
	PreparedStatement statement = null;
        try {
          statement = (PreparedStatement)new PoolPreparedStatement(c.prepareStatement(sql, a, b,p));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;
      }
      public CallableStatement prepareCall(String sql, int a, int b, int p)
        throws SQLException
      {
        if (closed) throw new SQLException("Connection is closed");
	CallableStatement statement = null;
        try {
          statement = new PoolCallableStatement(c.prepareCall(sql,a,b,p));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;

      }
      public PreparedStatement prepareStatement(String sql, int a)
        throws SQLException
      {
        if (closed) throw new SQLException("Connection is closed");
	PreparedStatement statement = null;
        try {
          statement = (PreparedStatement)new PoolPreparedStatement(c.prepareStatement(sql, a));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;

      }
      public PreparedStatement prepareStatement(String sql,
                                                int ci[] )
        throws SQLException
      {
        if (closed) throw new SQLException("Connection is closed");
	PreparedStatement statement = null;
        try {
          statement = (PreparedStatement)new PoolPreparedStatement(c.prepareStatement(sql, ci));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;
      }
      public PreparedStatement prepareStatement(String sql,
                                                String cn[])
        throws SQLException
      {
        if (closed) throw new SQLException("Connection is closed");
	PreparedStatement statement = null;
        try {
          statement = (PreparedStatement)new PoolPreparedStatement(c.prepareStatement(sql, cn));
	  statements.add(statement);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
	return statement;
      }

      // example of where I'd like to go here:
      public PreparedStatement x_prepareStatement(final String sql,
                                                  final String cn[])
        throws SQLException
      {
        return recordPreparedStatement(new PreparedStatementConstructor() {
            public PreparedStatement create() throws SQLException { return c.prepareStatement(sql, cn);}});
      }

      private final PreparedStatement recordPreparedStatement(PreparedStatementConstructor c) 
        throws SQLException
      {
        checkOpen();
        try {
          PreparedStatement statement = new PoolPreparedStatement(c.create());
	  statements.add(statement);
          return statement;
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }

      private final void checkOpen() throws SQLException {
        if (closed) throw new SQLException("Connection is closed");
      }

      // end jdk1.4 compatability

      // begin jdk1.6 compatability
      public <T> T unwrap(java.lang.Class<T> iface) throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.unwrap(iface);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public boolean isWrapperFor(java.lang.Class<?> iface) throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.isWrapperFor(iface);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Clob createClob() throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.createClob();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Blob createBlob() throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.createBlob();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public NClob createNClob() throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.createNClob();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public SQLXML createSQLXML() throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.createSQLXML();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public boolean isValid(int timeout) throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.isValid(timeout);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public void setClientInfo(String name, String value) throws SQLClientInfoException {
	    if (closed) throw new SQLClientInfoException("Connection is closed", null);
        try {
    	  c.setClientInfo(name, value);
        } catch (SQLClientInfoException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public void setClientInfo(Properties properties) throws SQLClientInfoException {
	    if (closed) throw new SQLClientInfoException("Connection is closed", null);
        try {
    	  c.setClientInfo(properties);
        } catch (SQLClientInfoException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public String getClientInfo(String name) throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.getClientInfo(name);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Properties getClientInfo() throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.getClientInfo();
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.createArrayOf(typeName, elements);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
	    if (closed) throw new SQLException("Connection is closed");
        try {
    	  return c.createStruct(typeName, attributes);
        } catch (SQLException sqle) {
          destroyPool();
          throw sqle;
        }
      }
      // end jdk1.6 compatibility

      /**
       * A wrapper for a Statement object. Most operations are
       * delegated to the wrapped object. The close operation goes
       * through the PoolConnection wrapper to keep track of which
       * statements have been closed and which haven't.
       */
      class PoolStatement implements java.sql.Statement {
	java.sql.Statement theStatement;
	public PoolStatement(java.sql.Statement theStatement) {
	  this.theStatement = theStatement;
	}
	public void addBatch( String sql )  throws java.sql.SQLException {
          try {
            theStatement.addBatch( sql );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void clearBatch()  throws java.sql.SQLException {
          try {
            theStatement.clearBatch();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public int[] executeBatch()  throws java.sql.SQLException {
          int[] i;
          try {
            i = theStatement.executeBatch();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public Connection getConnection()  throws java.sql.SQLException {
          Connection conn = null;
          try {
            conn = theStatement.getConnection();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return conn;
	}
	public int getFetchDirection()  throws java.sql.SQLException {
          int i;
          try {
            i = theStatement.getFetchDirection();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public int getFetchSize()  throws java.sql.SQLException {
          int i;
          try {
            i = theStatement.getFetchSize();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public int getResultSetConcurrency()  throws java.sql.SQLException {
          int i;
          try {
            i = theStatement.getResultSetConcurrency();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public int getResultSetType()  throws java.sql.SQLException {
          int i;
          try {
            i = theStatement.getResultSetType();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public void setFetchDirection( int direction )  throws java.sql.SQLException {
          try {
            theStatement.setFetchDirection( direction );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setFetchSize( int rows )  throws java.sql.SQLException  {
          try {
            theStatement.setFetchSize( rows );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public java.sql.ResultSet executeQuery(java.lang.String arg0) throws java.sql.SQLException {
          java.sql.ResultSet rs = null;
          try {
	    rs = theStatement.executeQuery(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return rs;
	}
	public int executeUpdate(java.lang.String arg0) throws java.sql.SQLException {
          int i;
          try {
	    i = theStatement.executeUpdate(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public void close() throws java.sql.SQLException {
          try {
	    closeStatement(this);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public int getMaxFieldSize() throws java.sql.SQLException {
          int i;
          try {
	    i = theStatement.getMaxFieldSize();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public void setMaxFieldSize(int arg0) throws java.sql.SQLException {
          try {
	    theStatement.setMaxFieldSize(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public int getMaxRows() throws java.sql.SQLException {
          int i;
          try {
	    i = theStatement.getMaxRows();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public void setMaxRows(int arg0) throws java.sql.SQLException {
          try {
	    theStatement.setMaxRows(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setEscapeProcessing(boolean arg0) throws java.sql.SQLException {
          try {
	    theStatement.setEscapeProcessing(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public int getQueryTimeout() throws java.sql.SQLException {
          int i;
          try {
	    i = theStatement.getQueryTimeout();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public void setQueryTimeout(int arg0) throws java.sql.SQLException {
          try {
	    theStatement.setQueryTimeout(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void cancel() throws java.sql.SQLException {
          try {
	    theStatement.cancel();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
          java.sql.SQLWarning warn = null;
          try {
	    warn = theStatement.getWarnings();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return warn;
	}
	public void clearWarnings() throws java.sql.SQLException {
          try {
	    theStatement.clearWarnings();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setCursorName(java.lang.String arg0) throws java.sql.SQLException {
          try {
	    theStatement.setCursorName(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public boolean execute(java.lang.String arg0) throws java.sql.SQLException {
          boolean b;
          try {
	    b = theStatement.execute(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
	public java.sql.ResultSet getResultSet() throws java.sql.SQLException {
          java.sql.ResultSet rs = null;
          try {
	    rs = theStatement.getResultSet();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return rs;
	}
	public int getUpdateCount() throws java.sql.SQLException {
          int i;
          try {
	    i = theStatement.getUpdateCount();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public boolean getMoreResults() throws java.sql.SQLException {
          boolean b;
          try {
	    b = theStatement.getMoreResults();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
        // begin jdk 1.4 compatability
        public boolean getMoreResults(int current) throws java.sql.SQLException {
          try {
            return theStatement.getMoreResults(current);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public ResultSet getGeneratedKeys() throws java.sql.SQLException {
          try {
            return theStatement.getGeneratedKeys();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public int executeUpdate(String sql, int agk) throws java.sql.SQLException {
          try {
            return theStatement.executeUpdate(sql, agk);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public int executeUpdate(String sql, int ci[]) throws java.sql.SQLException { 
          try {
            return theStatement.executeUpdate(sql, ci);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public int executeUpdate(String sql, String cn[]) throws java.sql.SQLException { 
          try {
            return theStatement.executeUpdate(sql, cn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public boolean execute(String sql, int agk)  throws java.sql.SQLException {
          try {
            return theStatement.execute(sql, agk);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public boolean execute(String sql, int ci[])  throws java.sql.SQLException {
          try {
            return theStatement.execute(sql, ci);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public boolean execute(String sql, String cn[])  throws java.sql.SQLException {
          try {
            return theStatement.execute(sql,cn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public int getResultSetHoldability() throws java.sql.SQLException {
          try {
            return theStatement.getResultSetHoldability();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        // end jdk 1.4 compatability

        // begin jdk 1.6 compatability
        public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException {
          try {
            return theStatement.unwrap(iface);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException {
          try {
            return theStatement.isWrapperFor(iface);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public boolean isClosed() throws java.sql.SQLException {
          try {
            return theStatement.isClosed();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setPoolable(boolean poolable) throws java.sql.SQLException {
          try {
            theStatement.setPoolable(poolable);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public boolean isPoolable() throws java.sql.SQLException {
          try {
            return theStatement.isPoolable();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        // end jdk 1.6 compatability
      }
      /**
       * A wrapper for a PreparedStatement object. All operations are
       * delegated to the wrapped object. The close operation in the
       * base class goes through the PoolConnection wrapper to keep
       * track of which statements have been closed and which haven't.
       */
      class PoolPreparedStatement extends PoolStatement implements java.sql.PreparedStatement {
	private java.sql.PreparedStatement thePreparedStatement;
	public PoolPreparedStatement(java.sql.PreparedStatement thePreparedStatement) {
	  super(thePreparedStatement);
	  this.thePreparedStatement = thePreparedStatement;
	}
	public void addBatch() throws java.sql.SQLException {
          try {
            thePreparedStatement.addBatch();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public ResultSetMetaData getMetaData() throws java.sql.SQLException {
          ResultSetMetaData data = null;
          try {
            data = thePreparedStatement.getMetaData();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return data;
	}
	public void setArray( int i, Array x ) throws java.sql.SQLException {
          try {
            thePreparedStatement.setArray( i, x );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setBlob( int i, Blob x ) throws java.sql.SQLException {
          try {
            thePreparedStatement.setBlob( i, x );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setCharacterStream( int paramIndex, java.io.Reader reader, int length ) throws java.sql.SQLException {
          try {
            thePreparedStatement.setCharacterStream( paramIndex, reader, length );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setClob( int i, Clob x ) throws java.sql.SQLException {
          try {
            thePreparedStatement.setClob( i,x );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setRef( int i, Ref x )  throws java.sql.SQLException {
          try {
            thePreparedStatement.setRef( i, x );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setDate( int i, java.sql.Date myDate, Calendar cal ) throws java.sql.SQLException {
          try {
            thePreparedStatement.setDate( i, myDate, cal );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setTime( int paramIndex, Time x, Calendar cal ) throws java.sql.SQLException {
          try {
            thePreparedStatement.setTime( paramIndex, x, cal );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setTimestamp( int paramIndex, java.sql.Timestamp x, Calendar cal ) throws java.sql.SQLException {
          try {
            thePreparedStatement.setTimestamp( paramIndex, x ,cal );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public java.sql.ResultSet executeQuery() throws java.sql.SQLException {
          java.sql.ResultSet rs = null;
          try {
            rs = thePreparedStatement.executeQuery();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return rs;
	}
	public int executeUpdate() throws java.sql.SQLException {
          int i;
          try {
	    i = thePreparedStatement.executeUpdate();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public void setNull(int arg0, int arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setNull(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setNull(int arg0, int arg1, String typeName ) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setNull(arg0, arg1, typeName);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setBoolean(int arg0, boolean arg1) throws java.sql.SQLException {
          try {
            thePreparedStatement.setBoolean(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setByte(int arg0, byte arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setByte(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setShort(int arg0, short arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setShort(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setInt(int arg0, int arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setInt(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setLong(int arg0, long arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setLong(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setFloat(int arg0, float arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setFloat(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setDouble(int arg0, double arg1) throws java.sql.SQLException {
          try {
 	    thePreparedStatement.setDouble(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setBigDecimal(int arg0, java.math.BigDecimal arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setBigDecimal(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setString(int arg0, java.lang.String arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setString(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setBytes(int arg0, byte[] arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setBytes(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setDate(int arg0, java.sql.Date arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setDate(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setTime(int arg0, java.sql.Time arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setTime(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setTimestamp(int arg0, java.sql.Timestamp arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setTimestamp(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setAsciiStream(int arg0, java.io.InputStream arg1, int arg2) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setAsciiStream(arg0, arg1, arg2);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
        /**
         * @deprecated
         **/
	public void setUnicodeStream(int arg0, java.io.InputStream arg1, int arg2) throws java.sql.SQLException {
          throw new java.sql.SQLException("Method not supported");
          //	  thePreparedStatement.setUnicodeStream(arg0, arg1, arg2);
	}
	public void setBinaryStream(int arg0, java.io.InputStream arg1, int arg2) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setBinaryStream(arg0, arg1, arg2);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void clearParameters() throws java.sql.SQLException {
          try {
	    thePreparedStatement.clearParameters();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setObject(int arg0, java.lang.Object arg1, int arg2, int arg3) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setObject(arg0, arg1, arg2, arg3);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setObject(int arg0, java.lang.Object arg1, int arg2) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setObject(arg0, arg1, arg2);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void setObject(int arg0, java.lang.Object arg1) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setObject(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public boolean execute() throws java.sql.SQLException {
          boolean b;
          try {
	    b = thePreparedStatement.execute();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
        // begin jdk 1.4 compatability
        public void setURL(int param, URL x) throws java.sql.SQLException {
          try {
	    thePreparedStatement.setURL(param, x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public ParameterMetaData getParameterMetaData() throws java.sql.SQLException {
          try {
	    return thePreparedStatement.getParameterMetaData();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        // end jdk 1.4 compatability

        // begin jdk 1.6 compatability
        public void setRowId(int parameterIndex, RowId x) throws SQLException {
            try {
                thePreparedStatement.setRowId(parameterIndex, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNString(int parameterIndex, String value) throws SQLException {
            try {
                thePreparedStatement.setNString(parameterIndex, value);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
            try {
                thePreparedStatement.setNCharacterStream(parameterIndex, value, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNClob(int parameterIndex, NClob value) throws SQLException {
            try {
                thePreparedStatement.setNClob(parameterIndex, value);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
            try {
                thePreparedStatement.setClob(parameterIndex, reader, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
            try {
                thePreparedStatement.setBlob(parameterIndex, inputStream, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
            try {
                thePreparedStatement.setNClob(parameterIndex, reader, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
            try {
                thePreparedStatement.setSQLXML(parameterIndex, xmlObject);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setAsciiStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException {
            try {
                thePreparedStatement.setAsciiStream(parameterIndex, x, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBinaryStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException {
            try {
                thePreparedStatement.setBinaryStream(parameterIndex, x, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setCharacterStream(int parameterIndex, java.io.Reader reader, long length) throws SQLException {
            try {
                thePreparedStatement.setCharacterStream(parameterIndex, reader, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setAsciiStream(int parameterIndex, java.io.InputStream x) throws SQLException {
            try {
                thePreparedStatement.setAsciiStream(parameterIndex, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBinaryStream(int parameterIndex, java.io.InputStream x) throws SQLException {
            try {
                thePreparedStatement.setBinaryStream(parameterIndex, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setCharacterStream(int parameterIndex, java.io.Reader reader) throws SQLException {
            try {
                thePreparedStatement.setCharacterStream(parameterIndex, reader);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
            try {
                thePreparedStatement.setNCharacterStream(parameterIndex, value);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setClob(int parameterIndex, Reader reader) throws SQLException {
            try {
                thePreparedStatement.setClob(parameterIndex, reader);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
            try {
                thePreparedStatement.setBlob(parameterIndex, inputStream);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNClob(int parameterIndex, Reader reader) throws SQLException {
            try {
                thePreparedStatement.setNClob(parameterIndex, reader);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        // end jdk 1.6 compatability
      }
      /**
       * A wrapper for a CallableStatement object. All operations are
       * delegated to the wrapped object. The close operation in the
       * base class goes through the PoolConnection wrapper to keep
       * track of which statements have been closed and which haven't.
       */
      class PoolCallableStatement extends PoolPreparedStatement implements java.sql.CallableStatement {
	private java.sql.CallableStatement theCallableStatement;
	public PoolCallableStatement(java.sql.CallableStatement theCallableStatement) {
	  super(theCallableStatement);
	  this.theCallableStatement = theCallableStatement;
	}
	public Array getArray( int i )  throws java.sql.SQLException {
          Array a = null;
          try {
            a = theCallableStatement.getArray( i );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return a;
	}
        /** @deprecated **/
	public java.math.BigDecimal getBigDecimal(int paramIndex)  throws java.sql.SQLException {
          java.math.BigDecimal bd;
          try {
            bd = theCallableStatement.getBigDecimal( paramIndex );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return bd;
	}
	public Blob getBlob( int i )  throws java.sql.SQLException {
          Blob b = null;
          try {
            b = theCallableStatement.getBlob( i );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
	public Clob getClob( int i )  throws java.sql.SQLException {
          Clob c = null;
          try {
            c = theCallableStatement.getClob( i );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return c;
	}
	public java.sql.Date getDate( int paramIndex, Calendar cal )  throws java.sql.SQLException {
          java.sql.Date d = null;
          try {
            d = theCallableStatement.getDate( paramIndex, cal );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return d;
	}
	public Object getObject( int i, Map map )  throws java.sql.SQLException {
          Object o = null;
          try {
            o = theCallableStatement.getObject ( i, map );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return o;
	}
	public Ref getRef ( int i )  throws java.sql.SQLException {
          Ref r = null;
          try {
            r = theCallableStatement.getRef( i );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return r;
	}
	public Time getTime ( int paramIndex, Calendar cal )  throws java.sql.SQLException {
          Time t = null;
          try {
            t = theCallableStatement.getTime ( paramIndex, cal );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return t;
	}
	public Timestamp getTimestamp( int paramIndex, Calendar cal )  throws java.sql.SQLException {
          Timestamp ts = null;
          try {
            ts = theCallableStatement.getTimestamp( paramIndex, cal );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return ts;
	}
	public void registerOutParameter( int paramIndex, int sqlType, String typeName )  throws java.sql.SQLException {
          try {
            theCallableStatement.registerOutParameter( paramIndex, sqlType, typeName );
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void registerOutParameter(int arg0, int arg1) throws java.sql.SQLException {
          try {
	    theCallableStatement.registerOutParameter(arg0, arg1);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public void registerOutParameter(int arg0, int arg1, int arg2) throws java.sql.SQLException {
          try {
	    theCallableStatement.registerOutParameter(arg0, arg1, arg2);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public boolean wasNull() throws java.sql.SQLException {
          boolean b;
          try {
	    b = theCallableStatement.wasNull();
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
	public java.lang.String getString(int arg0) throws java.sql.SQLException {
          java.lang.String str = null;
          try {
	    str = theCallableStatement.getString(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return str;
	}
	public boolean getBoolean(int arg0) throws java.sql.SQLException {
          boolean b;
          try {
	    b = theCallableStatement.getBoolean(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
	public byte getByte(int arg0) throws java.sql.SQLException {
          byte b;
          try {
	    b = theCallableStatement.getByte(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
	public short getShort(int arg0) throws java.sql.SQLException {
          short s;
          try {
	    s = theCallableStatement.getShort(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return s;
	}
	public int getInt(int arg0) throws java.sql.SQLException {
          int i;
          try {
	    i = theCallableStatement.getInt(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return i;
	}
	public long getLong(int arg0) throws java.sql.SQLException {
          long l;
          try {
	    l = theCallableStatement.getLong(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return l;
	}
	public float getFloat(int arg0) throws java.sql.SQLException {
          float f;
          try {
	    f = theCallableStatement.getFloat(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return f;
	}
	public double getDouble(int arg0) throws java.sql.SQLException {
          double d;
          try {
	    d = theCallableStatement.getDouble(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return d;
	}
        /** @deprecated **/
	public java.math.BigDecimal getBigDecimal(int arg0, int arg1) throws java.sql.SQLException {
          try {
            return theCallableStatement.getBigDecimal(arg0, arg1); 
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
	}
	public byte[] getBytes(int arg0) throws java.sql.SQLException {
          byte[] b;
          try {
	    b = theCallableStatement.getBytes(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return b;
	}
	public java.sql.Date getDate(int arg0) throws java.sql.SQLException {
          java.sql.Date date = null;
          try {
	    date = theCallableStatement.getDate(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return date;
	}
	public java.sql.Time getTime(int arg0) throws java.sql.SQLException {
          java.sql.Time time = null;
          try {
	    time = theCallableStatement.getTime(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return time;
	}
	public java.sql.Timestamp getTimestamp(int arg0) throws java.sql.SQLException {
          java.sql.Timestamp ts = null;
          try {
	    ts = theCallableStatement.getTimestamp(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return ts;
	}
	public java.lang.Object getObject(int arg0) throws java.sql.SQLException {
          java.lang.Object o = null;
          try {
	    o = theCallableStatement.getObject(arg0);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
          return o;
	}
        // begin jdk 1.4 compatability
        public void registerOutParameter(String pn, int sqltype) throws java.sql.SQLException {
          try {
            theCallableStatement.registerOutParameter(pn, sqltype);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void registerOutParameter(String pn, int sqltype, int scale) throws java.sql.SQLException {
          try {
            theCallableStatement.registerOutParameter(pn, sqltype,scale);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void registerOutParameter(String pn, int sqltype, String tn) throws java.sql.SQLException {
          try{
            theCallableStatement.registerOutParameter(pn, sqltype,tn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public URL getURL(int pi)  throws java.sql.SQLException {
          try {
            return theCallableStatement.getURL(pi);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setURL(String pn, URL v)  throws java.sql.SQLException {
          try {
            theCallableStatement.setURL(pn,v);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setNull(String pn, int sqlt) throws java.sql.SQLException {
          try {
            theCallableStatement.setNull(pn,sqlt);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setBoolean(String pn, boolean x) throws java.sql.SQLException {
          try {
            theCallableStatement.setBoolean(pn, x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setByte(String pn, byte x) throws java.sql.SQLException {
          try {
            theCallableStatement.setByte(pn, x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setShort(String pn, short x) throws java.sql.SQLException {
          try {
            theCallableStatement.setShort(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setInt(String pn, int x) throws java.sql.SQLException {
          try {
            theCallableStatement.setInt(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setLong(String pn, long x) throws java.sql.SQLException {
          try {
            theCallableStatement.setLong(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setFloat(String pn, float x) throws java.sql.SQLException {
          try {
            theCallableStatement.setFloat(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setDouble(String pn, double x) throws java.sql.SQLException {
          try {
            theCallableStatement.setDouble(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setBigDecimal(String pn, BigDecimal x) throws java.sql.SQLException {
          try {
            theCallableStatement.setBigDecimal(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setString(String pn, String x) throws java.sql.SQLException {
          try {
            theCallableStatement.setString(pn, x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setBytes(String pn, byte[] x) throws java.sql.SQLException {
          try {
            theCallableStatement.setBytes(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setDate(String pn, java.sql.Date x) throws java.sql.SQLException {
          try {
            theCallableStatement.setDate(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setTime(String pn, Time x) throws java.sql.SQLException {
          try {
            theCallableStatement.setTime(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setTimestamp(String pn, Timestamp x) throws java.sql.SQLException {
          try {
            theCallableStatement.setTimestamp(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setAsciiStream(String pn, InputStream x, int l) throws java.sql.SQLException {
          try {
            theCallableStatement.setAsciiStream(pn,x,l);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setBinaryStream(String pn, InputStream x, int l) throws java.sql.SQLException {
          try {
            theCallableStatement.setBinaryStream(pn,x,l);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setObject(String pn, Object x, int tt, int s) throws java.sql.SQLException {
          try {
            theCallableStatement.setObject(pn,x,tt,s);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setObject(String pn, Object x, int tt) throws java.sql.SQLException {
          try {
            theCallableStatement.setObject(pn,x,tt);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setObject(String pn, Object x) throws java.sql.SQLException {
          try {
            theCallableStatement.setObject(pn,x);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setCharacterStream(String pn, Reader x, int l) throws java.sql.SQLException {
          try {
            theCallableStatement.setCharacterStream(pn,x,l);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setDate(String pn, java.sql.Date x, Calendar cal) throws java.sql.SQLException {
          try {
            theCallableStatement.setDate(pn,x,cal);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setTime(String pn, Time x, Calendar cal) throws java.sql.SQLException {
          try {
            theCallableStatement.setTime(pn,x,cal);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setTimestamp(String pn, Timestamp x, Calendar cal) throws java.sql.SQLException {
          try {
            theCallableStatement.setTimestamp(pn,x,cal);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public void setNull(String pn, int st, String tn) throws java.sql.SQLException {
          try {
            theCallableStatement.setNull(pn,st,tn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public String getString(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getString(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public boolean getBoolean(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getBoolean(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public byte getByte(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getByte(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public short getShort(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getShort(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public int getInt(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getInt(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public long getLong(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getLong(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public float getFloat(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getFloat(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public double getDouble(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getDouble(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public byte[] getBytes(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getBytes(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public java.sql.Date getDate(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getDate(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Time getTime(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getTime(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Timestamp getTimestamp(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getTimestamp(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Object getObject(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getObject(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public BigDecimal getBigDecimal(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getBigDecimal(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Object getObject(String pn,Map m) throws java.sql.SQLException {
          try {
            return theCallableStatement.getObject(pn,m);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Ref getRef(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getRef(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Blob getBlob(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getBlob(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Clob getClob(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getClob(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Array getArray(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getArray(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public java.sql.Date getDate(String pn,Calendar c) throws java.sql.SQLException {
          try {
            return theCallableStatement.getDate(pn,c);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Time getTime(String pn,Calendar c) throws java.sql.SQLException {
          try {
            return theCallableStatement.getTime(pn,c);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public Timestamp getTimestamp(String pn,Calendar c) throws java.sql.SQLException {
          try {
            return theCallableStatement.getTimestamp(pn,c);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        public URL getURL(String pn) throws java.sql.SQLException {
          try {
            return theCallableStatement.getURL(pn);
          } catch (SQLException sqle) {
            PoolConnection.this.destroyPool();
            throw sqle;
          }
        }
        // end jdk 1.4 compatability

        // begin jdk 1.6 compatability
        public RowId getRowId(int parameterIndex) throws SQLException {
            try {
                return theCallableStatement.getRowId(parameterIndex);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public RowId getRowId(String parameterName) throws SQLException {
            try {
                return theCallableStatement.getRowId(parameterName);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setRowId(String parameterName, RowId x) throws SQLException {
            try {
                theCallableStatement.setRowId(parameterName, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNString(String parameterName, String value) throws SQLException {
            try {
                theCallableStatement.setNString(parameterName, value);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
            try {
                theCallableStatement.setNCharacterStream(parameterName, value, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNClob(String parameterName, NClob value) throws SQLException {
            try {
                theCallableStatement.setNClob(parameterName, value);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setClob(String parameterName, Reader reader, long length) throws SQLException {
            try {
                theCallableStatement.setClob(parameterName, reader, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
            try {
                theCallableStatement.setBlob(parameterName, inputStream, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
            try {
                theCallableStatement.setNClob(parameterName, reader, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public NClob getNClob (int parameterIndex) throws SQLException {
            try {
                return theCallableStatement.getNClob (parameterIndex);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public NClob getNClob (String parameterName) throws SQLException {
            try {
                return theCallableStatement.getNClob (parameterName);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
            try {
                theCallableStatement.setSQLXML(parameterName, xmlObject);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public SQLXML getSQLXML(int parameterIndex) throws SQLException {
            try {
                return theCallableStatement.getSQLXML(parameterIndex);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public SQLXML getSQLXML(String parameterName) throws SQLException {
            try {
                return theCallableStatement.getSQLXML(parameterName);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public String getNString(int parameterIndex) throws SQLException {
            try {
                return theCallableStatement.getNString(parameterIndex);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public String getNString(String parameterName) throws SQLException {
            try {
                return theCallableStatement.getNString(parameterName);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public java.io.Reader getNCharacterStream(int parameterIndex) throws SQLException {
            try {
                return theCallableStatement.getNCharacterStream(parameterIndex);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public java.io.Reader getNCharacterStream(String parameterName) throws SQLException {
            try {
                return theCallableStatement.getNCharacterStream(parameterName);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public java.io.Reader getCharacterStream(int parameterIndex) throws SQLException {
            try {
                return theCallableStatement.getCharacterStream(parameterIndex);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public java.io.Reader getCharacterStream(String parameterName) throws SQLException {
            try {
                return theCallableStatement.getCharacterStream(parameterName);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBlob (String parameterName, Blob x) throws SQLException {
            try {
                theCallableStatement.setBlob (parameterName, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setClob (String parameterName, Clob x) throws SQLException {
            try {
                theCallableStatement.setClob (parameterName, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setAsciiStream(String parameterName, java.io.InputStream x, long length) throws SQLException {
            try {
                theCallableStatement.setAsciiStream(parameterName, x, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBinaryStream(String parameterName, java.io.InputStream x, long length) throws SQLException {
            try {
                theCallableStatement.setBinaryStream(parameterName, x, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setCharacterStream(String parameterName, java.io.Reader reader, long length) throws SQLException {
            try {
                theCallableStatement.setCharacterStream(parameterName, reader, length);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setAsciiStream(String parameterName, java.io.InputStream x) throws SQLException {
            try {
                theCallableStatement.setAsciiStream(parameterName, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBinaryStream(String parameterName, java.io.InputStream x) throws SQLException {
            try {
                theCallableStatement.setBinaryStream(parameterName, x);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setCharacterStream(String parameterName, java.io.Reader reader) throws SQLException {
            try {
                theCallableStatement.setCharacterStream(parameterName, reader);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
            try {
                theCallableStatement.setNCharacterStream(parameterName, value);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setClob(String parameterName, Reader reader) throws SQLException {
            try {
                theCallableStatement.setClob(parameterName, reader);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
            try {
                theCallableStatement.setBlob(parameterName, inputStream);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        public void setNClob(String parameterName, Reader reader) throws SQLException {
            try {
                theCallableStatement.setNClob(parameterName, reader);
            } catch (SQLException sqle) {
                PoolConnection.this.destroyPool();
                throw sqle;
            }
        }
        // end jdk 1.6 compatability
      }
    }
  }

  /**
   * One of the three main functions of this class. This is intended
   * as a drop-in replacement for DriverManager.getConnection(). The
   * connection return may have been previously used.
   */
  public static Connection getConnection(String dbURL,
					 Properties props)
    throws SQLException {
    return getConnection(dbURL, (String) props.get("user"), (String) props.get("password"));
  }

  /**
   * One of the three main functions of this class. This is intended
   * as a drop-in replacement for DriverManager.getConnection(). The
   * connection return may have been previously used.
   */
  public static Connection getConnection(String dbURL,
					 String user,
					 String passwd)
    throws SQLException {
    String key = dbURL + SEP + user;
    DBConnectionPool pool;
    boolean createdPool = false;

    synchronized (dbConnectionPools) {
      pool = (DBConnectionPool) dbConnectionPools.get(key);
      if (pool == null) {
	pool = new DBConnectionPool(key);
	dbConnectionPools.put(key, pool);
        createdPool = true;
      }
    }

    if (createdPool) {
      ensureTimer();
    }

    return pool.findConnection(dbURL, user, passwd);
  }

  private static final Object timer_lock = new Object();
  private static Thread timer;

  private static void ensureTimer() {
    synchronized (timer_lock) {
      if (timer != null) return;
      Runnable r = new Runnable() {
        public void run() {
          timerRun();
        }
      };
      timer = new Thread(r, "DBConnectionPool Timer");
      timer.setDaemon(true);
      timer.start();
    }
  }

  private static void timerRun() {
    while (true) {
      // pause
      try {
        Thread.sleep(TIMEOUT_CHECK_INTERVAL);
      } catch (InterruptedException e) {
      }

      // check timeouts
      try {
        checkAllTimeouts();
      } catch (Throwable t) {
        // Emergency measures to keep thread from dying.
        System.err.println(
            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"+
            "Uncaught Exception or Error in DBConnectionPool:");
        t.printStackTrace();
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      }
    }
  }

  private static void checkAllTimeouts() {
    synchronized (dbConnectionPools) {
      // get pools as array
      int n = dbConnectionPools.size();
      if (n <= 0) return;
      DBConnectionPool[] pools = new DBConnectionPool[n];
      Iterator iter = dbConnectionPools.values().iterator();
      for (int i = 0; i < n; i++) {
        pools[i++] = (DBConnectionPool) iter.next();
      }

      // call "checkTimeout" on each pool
      long now = System.currentTimeMillis();
      for (int i = 0; i < pools.length; i++) {
        try {
          pools[i].checkTimeout(now);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * A List of entries for connections we have opened.
   */
  private ArrayList entries = new ArrayList();

  /**
   * The maximum number of connections we permit in this
   * pool. Obtained from the first connection opened in this pool.
   */
  private int maxConnections = -1;

  /** How many times to retry a getConnection when failed due to a recoverable exception **/
  private int maxRetries = 30;  // retry for a whole minute.
  
  /** How frequently should recoverable getConnections be retried? **/
  private long retryTimeout = 5*1000L; // 5 seconds

  /**
   *
   */
  private synchronized boolean containsConnection(DBConnectionPoolEntry entry) {
    return entries.contains(entry);
  }

  /**
   * Closes all of the currently unused Connections in this pool.  Connections
   * that are currently open must destroy themselves when they become unused.
   */
  private synchronized void destroyPool() {
    while (!entries.isEmpty()) {
      DBConnectionPoolEntry entry = (DBConnectionPoolEntry) entries.get(0);
      entries.remove(entry);
      if (!entry.inUse)
        entry.destroy();
    }
    notifyAll();
  }

  /** Encapsulate logic to decide if a given exception is likely to be 
   * transitory and so the connection attempt worthwhile retrying.
   **/
  private static boolean isRetryable(SQLException e) {
    while (e != null) {
      String m = e.getMessage();
      if (m.startsWith("ORA-00604")) { // recursive error
        e = e.getNextException();
      } else if (m.startsWith("ORA-00020") || // maximum number of processes (100) exceeded  [too many connections]
                 m.startsWith("ORA-00018") || // maximum number of sessions exceeded [?]
                 m.startsWith("ORA-01000")    // maximum open cursors exceeded  [too many open statements]
                 ) {
        return true;
      } else if (m.indexOf("Cannot connect to MySQL") > -1) { // MySQL DB unreachable
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  private synchronized Connection findConnection(String dbURL, String user, String passwd)
    throws SQLException 
  {
    int retries = 0;            // how many retries have we done?
    boolean waitingP = false;
    while (true) {
      for (Iterator e = entries.iterator(); e.hasNext(); ) {
        DBConnectionPoolEntry entry = (DBConnectionPoolEntry) e.next();
        if (!entry.inUse) {
          entry.inUse = true;
          return entry.getPoolConnection();
        }
      }
    
      if (maxConnections < 0 || entries.size() < maxConnections) {
        try {
          Connection conn = DriverManager.getConnection(dbURL, user, passwd);
          if (maxConnections < 0) {
            maxConnections = conn.getMetaData().getMaxConnections();
            if (maxConnections < 1 || maxConnections > MAX_CONNECTIONS) {
              maxConnections = MAX_CONNECTIONS;
            }
          }
          DBConnectionPoolEntry entry = new DBConnectionPoolEntry(conn);
          entries.add(entry);
          if (waitingP) {
            waitingCounter--;
            if (logger.isDebugEnabled()) {
              logger.debug("Finished waiting for "+key+" ("+waitingCounter+")");
            }
          }
        } catch (SQLException sqle) {
          if (logger.isWarnEnabled()) {
            logger.warn("DBConnectionPool "+key+" saw exception", sqle);
          }
          if (retries<maxRetries && isRetryable(sqle)) {
            retries++;
            waitingP = true;
            waitingCounter++;
            if (logger.isDebugEnabled()) {
              logger.debug("Waiting to retry for "+key+" ("+waitingCounter+")");
            }
            try {
              wait(retryTimeout);
            } catch (InterruptedException e) {}
          } else {
            throw sqle;
          }
        }
      } else {
        try {
          if (!waitingP) {
            waitingP = true;
            waitingCounter++;
            if (logger.isDebugEnabled()) {
              logger.debug("Waiting for "+key+
                           " ("+waitingCounter+" of "+entries.size()+"/"+maxConnections+")");
            }
          }
          wait();
        } catch (InterruptedException e) { }
      }
    }
  }

  private synchronized void delete(DBConnectionPoolEntry entry) {
    entries.remove(entry);
    entry.destroy();
    notifyAll();
  }

  private synchronized void release(DBConnectionPoolEntry entry) {
    entry.inUse = false;
    entry.lastUsed = System.currentTimeMillis();
    notifyAll();
  }

  private synchronized void checkTimeout(long now) {
    if (entries.size() > 0) {
      ArrayList entriesToDelete = new ArrayList();
      for (Iterator e = entries.iterator(); e.hasNext(); ) {
        DBConnectionPoolEntry entry = (DBConnectionPoolEntry) e.next();
        if (!entry.inUse) {
          if (entry.lastUsed < (now - TIMEOUT)) {
            entriesToDelete.add(entry);
          }
        }
      }
      int ndrops = entriesToDelete.size();
      if (ndrops > 0) {
        if (logger.isDebugEnabled()) {
          logger.debug("DBConnectionPool "+key+" dropping "+entriesToDelete.size()+" entries");
        }
        for (Iterator e = entriesToDelete.iterator(); e.hasNext(); ) {
          DBConnectionPoolEntry entry = (DBConnectionPoolEntry) e.next();
          delete(entry);
        }
      }
    }
  }

  //
  // driver registration
  //

  /** A Map of drivernames to Driver classes **/
  private static Map drivers = new HashMap(); 

  /** Register a driver carefully **/
  public static void registerDriver(String driverName)
    throws Exception
  {
    synchronized (drivers) {
      if (drivers.get(driverName) != null)
        return;
      
      Driver driver = (Driver)(Class.forName(driverName).newInstance());
      DriverManager.registerDriver(driver);
      drivers.put(driverName, driver);
    }
  }

  static interface PreparedStatementConstructor {
    PreparedStatement create() throws SQLException;
  }
}


