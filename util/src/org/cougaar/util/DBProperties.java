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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;


/**
 * This utility extends <code>java.util.Properties</code> by doing parameter
 * substitutions after loading a .q file. q files are cached, such that they
 * are only parsed once per VM.
 *
 * .q files understood by this class are different from those
 * understood by QueryLDMPlugin and LDMSQLPlugin because the files are
 * not divided into query handler sections. Instead query handlers are
 * specified (when necessary) by the following convention: a property
 * named after the generic query type (e.g. locationQuery) and
 * suffixed with .handler is defined with a comma-separated list of
 * handler class names. Each handler class knows the name of the query
 * property that it uses.
 **/
public abstract class DBProperties extends java.util.Properties {
  protected static final Logger log = Logging.getLogger(DBProperties.class);

  private String default_dbtype;
  private boolean debug = false;
  private String name;

  // Stash away DBProperties by .q file name as we create them
  private static Map dbProps = new HashMap();

  /**
   * Get the cached DBProperties instance for this file, if any
   *
   * @param qFile a <code>String</code> query file to look up
   * @return a <code>DBProperties</code> for that file, possibly null
   */
  private static DBProperties getCachedInstance(String qFile) {
    synchronized (dbProps) {
      if (qFile == null)
	return null;
      return ((DBProperties)dbProps.get(qFile));
    }
  }

  /**
   * Stash away DBProperties as they are created by q file name
   *
   * @param instance a <code>DBProperties</code> instance to reuse
   * @param qFile a <code>String</code> query file name
   */
  private static void addCachedInstance(DBProperties instance, String qFile) {
    synchronized (dbProps) {
      if (qFile != null && instance != null && !dbProps.containsKey(qFile))
	dbProps.put(qFile, instance);
    }
  }

  /**
   * Stash away DBProperties as they are created by q file name, regardless
   * of whether the named .q file was previously stashed away.
   *
   * @param instance a <code>DBProperties</code> instance to reuse
   * @param qFile a <code>String</code> query file name
   */
  private static void setCachedInstance(DBProperties instance, String qFile) {
    synchronized (dbProps) {
      if (qFile != null && instance != null)
	dbProps.put(qFile, instance);
    }
  }

  /**
   * Read and parse a .q file. If the file was previously parsed, do not
   * re-read. If the .q file contains value for
   * "database" (case-sensitive) the default database type is set accordingly. The
   * default database type may be set or changed manually with the
   * setDefaultDatabase method.
   * @param qfile the name of the query file
   **/
  public static DBProperties readQueryFile(String qfile)
    throws IOException
  {
    return DBProperties.readQueryFile(qfile, null);
  }

  /**
   * Read and parse a .q file. If the file was previously parsed, do not
   * re-read. If the .q file contains value for
   * "database" (case-sensitive) the default database type is set accordingly. The
   * default database type may be set or changed manually with the
   * setDefaultDatabase method.
   * @param qfile the name of the query file
   * @param module the name of the module to search first for the query file
   **/
  public static DBProperties readQueryFile(String qfile, String module)
    throws IOException
  {
    // Only create a DBProperties if we dont have one yet for this file
    DBProperties dbp = DBProperties.getCachedInstance(qfile);
    if (dbp == null) {
      dbp = createDBProperties(qfile, ConfigFinder.getInstance(module).open(qfile));
      if (dbp != null)
	DBProperties.addCachedInstance(dbp, qfile);
    }
    return dbp;    
  }

  /**
   * Force Re-Read and parse a .q file. If the .q file contains value for
   * "database" (case-sensitive) the default database type is set accordingly. The
   * default database type may be set or changed manually with the
   * setDefaultDatabase method.
   * @param qfile the name of the query file
   **/
  public static DBProperties reReadQueryFile(String qfile)
    throws IOException
  {
    return DBProperties.reReadQueryFile(qfile, null);
  }

  /**
   * Force Re-Read and parse a .q file. If the .q file contains value for
   * "database" (case-sensitive) the default database type is set accordingly. The
   * default database type may be set or changed manually with the
   * setDefaultDatabase method.
   * @param qfile the name of the query file
   * @param module the name of the module to search first for the query file
   **/
  public static DBProperties reReadQueryFile(String qfile, String module)
    throws IOException
  {
    // Force re-reading the q file, and cache it.
    DBProperties dbp = createDBProperties(qfile, ConfigFinder.getInstance(module).open(qfile));
    if (dbp != null)
      DBProperties.setCachedInstance(dbp, qfile);
    return dbp;
  }

  /**
   * Read and parse a .q file specified as a URL. If the file was 
   * previously read, do not re-read.  If the .q file
   * contains a value for "database" (case-sensitive) the default database type is
   * set accordingly. The default database type may be set or
   * changed manually with the setDefaultDatabase method.
   * @param url the url to be opened to read the query file
   * contents.
   **/
  public static DBProperties readQueryFile(URL url)
    throws IOException
  {
    // Only create a DBProperties if we dont have one yet for this file
    String qfile = url.toString();
    DBProperties dbp = DBProperties.getCachedInstance(qfile);
    if (dbp == null) {
      dbp = createDBProperties(qfile, url.openStream());
      if (dbp != null)
	DBProperties.addCachedInstance(dbp, qfile);
    }
    return dbp;
  }

  /**
   * Force Re-read and parse a .q file specified as a URL. Even if the file was 
   * previously read, re-read.  If the .q file
   * contains a value for "database" (case-sensitive) the default database type is
   * set accordingly. The default database type may be set or
   * changed manually with the setDefaultDatabase method.
   * @param url the url to be opened to read the query file contents.
   **/
  public static DBProperties reReadQueryFile(URL url)
    throws IOException
  {
    // Only create a DBProperties if we dont have one yet for this file
    String qfile = url.toString();
    DBProperties dbp = createDBProperties(qfile, url.openStream());
    if (dbp != null)
      DBProperties.setCachedInstance(dbp, qfile);
    return dbp;
  }

  private static DBProperties createDBProperties(String name, InputStream is)
    throws IOException
  {
    InputStream i = new BufferedInputStream(is);
    try {
      return new Immutable(name, i);
    } catch (RuntimeException re) {
      log.error("CreateDBProperties exception", re);
      throw re;
    } catch (IOException ioe) {
      log.error("CreateDBProperties exception", ioe);
      throw ioe;
    } finally {
      i.close();
    }
  }

  //
  // constructors
  //

  protected DBProperties(DBProperties that) {
    for (Enumeration props = that.propertyNames(); props.hasMoreElements(); ) {
      String key = (String)props.nextElement();
      super.setProperty(key, that.getProperty(key));
    }
    default_dbtype = that.getDefaultDatabase();
    name = that.getName();
  }

  protected DBProperties(String name, InputStream i) throws IOException {
    this.name = name;
    load(i);
    String dbts = getProperty("database");
    if (dbts != null) {
      default_dbtype = getDBType(dbts);
    }
  }


  //
  // accessors
  //

  /**
   * Add the queries from the given query file to this instance's collection.
   *
   * @param qfile a <code>String</code> query file to parse
   * @exception IOException if an error occurs
   */
  public void addQueryFile(String qfile) throws IOException {
    addQueryFile(qfile, null);
  }

  /**
   * Add the queries from the given query file to this instance's collection.
   *
   * @param qfile a <code>String</code> query file to parse
   * @param module the name of the module to search first for the query file
   * @exception IOException if an error occurs
   */
  public void addQueryFile(String qfile, String module) throws IOException {
    InputStream i = new BufferedInputStream(ConfigFinder.getInstance(module).open(qfile));
    try {
      load(i);
    } catch (RuntimeException re) {
      log.error("addQueryFile exception", re);
      throw re;
    } catch (IOException ioe) {
      log.error("addQueryFile exception", ioe);
      throw ioe;
    } finally {
      i.close();
    }
  }


  public String getName() {
    return name;
  }

  /**
   * Change the database specification. The database specification
   * is the name of a database url parameter found using in this
   * DBProperties.
   * @param dburl the jdbc url of the database in which the queries
   * are to be executed. The database type is extracted from the url
   * and used for getting queries tailored for that database.
   **/
  public void setDefaultDatabase(String dburl) {
    default_dbtype = getDBType(dburl);
  }

  public String getDefaultDatabase() {
    return default_dbtype;
  }

  /**
   * Accessor for the default database type string. This is the
   * string that is appended to query names (e.g. oracle) to form
   * the name of a database-specific query (or other value).
   **/
  public String getDBType() {
    return default_dbtype;
  }

  /**
   * Convert a db url into a database type string
   **/
  public String getDBType(String dburl) {
    if (dburl == null) {
      log.error("Missing DB URL!");
      throw new IllegalArgumentException("Missing DB URL!");
    }
    int ix1 = dburl.indexOf("jdbc:") + 5;
    int ix2 = dburl.indexOf(":", ix1);
    if (ix1 < 0 || ix2 < 0) {
      log.error("Malformed DB Url: " + dburl);
      throw new IllegalArgumentException("Malformed DB URL: " + dburl);
    }
    return dburl.substring(ix1, ix2);
  }

  /**
   * Load properties from an InputStream and post-process to perform
   * variable substitutions on the values. Substitution is done
   * using Parameters.replaceParameters.
   * This method should not normally be used and is defined only to
   * override the base class version and interpose parameter
   * replacements from cougaar.rc.
   **/
  public void load(InputStream i) throws IOException {
    super.load(i);
    for (Enumeration e = propertyNames(); e.hasMoreElements(); ) {
      String name = (String) e.nextElement();
      String rawValue = getProperty(name);
      String cookedValue = Parameters.replaceParameters(rawValue, this);
      setProperty(name, cookedValue);
    }
  }

  /**
   * Return a query with a given name. Variable substitution is
   * performed by looking for patterns which are the keys in a Map.
   * By convention, variable names start with a colon and are
   * alphanumeric, but this is not required. However, variable names
   * must not start with the name of another variable. For example,
   * :a and :aye are not allowed. The query is sought under two
   * different names, first by suffixing the given name with a dot
   * (.) and the default database type and, if that query is not
   * found, again without the suffix.
   * @param queryName the name of the query.
   * @param substitutions The substitutions to be performed. The
   * query is examined for occurances of the keys in the Map and
   * replaced with the corresponding value.
   **/
  public String getQuery(String queryName, Map substitutions) {
    return getQueryForDatabase(queryName, substitutions, null);
  }

  /**
   * Same as {@link #getQuery(String,Map) above}, but allows a
   * different database to be specified.
   * @param queryName the name of the query.
   * @param substitutions a map of translations of substitution variables
   * @param dbspec the key under which to find the database url for
   * this query.
   **/
  public String getQueryForDatabase(String queryName, Map substitutions, String dbspec) {
    String dbtype = default_dbtype;;
    if (dbspec != null) {
      String dburl = getProperty(dbspec);
      if (dburl != null) {
	dbtype = getDBType(dburl);
      }
    }
    String result = getQuery1(queryName + "." + dbtype, substitutions);
    if (result == null) result = getQuery1(queryName, substitutions);
    if (result == null)
      throw new IllegalArgumentException("No query named " + queryName);
    return result;
  }

  /**
   * Does the actual work. Called twice for each query.
   **/
  private String getQuery1 (String queryName, Map substitutions) {
    String tmp = getProperty(queryName);
    if(tmp == null) return null;
    
    StringBuffer query = new StringBuffer(tmp);
    if (substitutions != null) {
      for (Iterator entries = substitutions.entrySet().iterator();
           entries.hasNext(); ) {
        Map.Entry entry = (Map.Entry) entries.next();
        String key = (String)entry.getKey();
        int ix = 0;
        while ((ix = query.indexOf(key, ix)) >= 0) {
          String subst = (String) entry.getValue();
          if (subst == null)
            throw new IllegalArgumentException("Null value for " + key);
          else {
            query.replace(ix, ix+key.length(), subst);
            ix = ix + key.length();
          }}}}
    if (debug) {
      System.out.println(this + ": " + queryName + "->" + query);
    }
    return query.substring(0);
  }
  
  
  /**
   * Enable debugging. When debugging is enabled, the queries are
   * printed after substitution has been performed
   **/
  public void setDebug(boolean newDebug) {
    debug = newDebug;
  }

  public String toString() {
    return name;
  }

  /** Return a locked copy of this DBProperties object.
   **/
  public abstract DBProperties lock();

  /** Return an unlocked copy of this DBProperties object.
   **/
  public abstract DBProperties unlock();

  public abstract boolean isLocked();

  //
  // instantiable classes
  //

  /** A mutable DBProperties class **/
  private static class Mutable extends DBProperties {
    public Mutable(DBProperties p) {
      super(p);
    }
    public Object clone() {
      return new Mutable(this);
    }
    public DBProperties lock() {
      return new Immutable(this);
    }
    public DBProperties unlock() {
      return new Mutable(this);
    }
    public boolean isLocked() {
      return false;
    }
  }

  /** An immutable variation of DBProperties **/
  static class Immutable extends DBProperties {
    public Immutable(DBProperties p) {
      super(p);
      lockdown = true;
    }
    public Immutable(String name, InputStream i) throws IOException {
      super(name, i);
      lockdown = true;
    }

    private boolean lockdown = false;

    public void load(InputStream isStream) throws IOException {
      if (lockdown) {
        //log.error("Attempt to modify Immutable instance", new Throwable());
        throw new IllegalArgumentException("Immutable DBProperties instance");
      }
      super.load(isStream);
    }
    public void clear() {
      //log.error("Attempt to modify Immutable instance", new Throwable());
      throw new IllegalArgumentException("Immutable DBProperties instance");
    }
    public void putAll(Map m) {
      //log.error("Attempt to modify Immutable instance", new Throwable());
      throw new IllegalArgumentException("Immutable DBProperties instance");
    }
    public Object remove(Object key) {
      //log.error("Attempt to modify Immutable instance", new Throwable());
      throw new IllegalArgumentException("Immutable DBProperties instance");
    }
    public Object put(Object key, Object value) {
      if (lockdown) {
        //log.error("Attempt to modify Immutable instance", new Throwable());
        throw new IllegalArgumentException("Immutable DBProperties instance");
      }
      return super.put(key,value);
    }      
    public Object clone() {
      return this;
    }
    public void setDefaultDatabase(String dburl) {
      //log.error("Attempt to modify Immutable instance", new Throwable());
      throw new IllegalArgumentException("Immutable DBProperties instance");
    }
    public DBProperties lock() {
      return this;
    }
    public DBProperties unlock() {
      return new Mutable(this);
    }
    public boolean isLocked() {
      return true;
    }
    public void addQueryFile(String qfile, String module) {
      //log.error("Attempt to modify Immutable instance", new Throwable());
      throw new IllegalArgumentException("Immutable DBProperties instance");
    }
  }
}
