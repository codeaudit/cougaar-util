/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

/*
 * Originally from delta/fgi package mil.darpa.log.alpine.delta.plugin;
 * Copyright 1997 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
 */
package org.cougaar.util;

import java.net.URL;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * This utility extends <code>java.util.Properties</code> by doing parameter
 * substitutions after loading a .q file. q files are cached, such that they
 * are only parsed once per VM.
 **/
public class DBProperties extends java.util.Properties {
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
    } finally {
      i.close();
    }
  }

  private static DBProperties createDBProperties(String name, InputStream is)
    throws IOException
  {
    InputStream i = new BufferedInputStream(is);
    try {
      DBProperties result = new DBProperties(name);
      result.load(i);
      // The database property, case sensitive, must exist
      // in the .q file. It's value is usually ${<db name from cougaar.rc>}
      String dburl = result.getProperty("database");
      if (dburl != null) result.setDefaultDatabase(dburl);
      return result;
    } finally {
      i.close();
    }
  }

  private DBProperties(String name) {
    this.name = name;
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
    int ix1 = dburl.indexOf("jdbc:") + 5;
    int ix2 = dburl.indexOf(":", ix1);
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
    for (Enumeration enum = propertyNames(); enum.hasMoreElements(); ) {
      String name = (String) enum.nextElement();
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
}
