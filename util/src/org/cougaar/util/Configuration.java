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
package org.cougaar.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

//import org.apache.log4j.*;
import org.cougaar.util.log.*;

/**
 * Configuration is a holder of a collection of static configuration utility methods,
 * mostly for use by ConfigFinder et al.
 **/
public final class Configuration {
  public static final String defaultConfigPath = 
    "$CWD;$HOME/.alp;$INSTALL/configs/$CONFIG;$INSTALL/configs/common";

  // these are initialized at the end
  private static Map defaultProperties;
  private static URL installUrl;
  private static URL configUrl;
  private static String configPath;

  /** Configuration is uninstantiable **/
  private Configuration() {}

  /** get the config path as an unmodifiable List of URL instances
   * which describes, in order, the set of base locations searched by
   * this instance of the ConfigFinder.
   **/
  public static String getConfigPath() { return configPath; }

  /** @return the current Cougaar Install Path **/
  public static URL getInstallURL() { return installUrl; }
  /** @return the current config directory (or common, if undefined) **/
  public static URL getConfigURL() { return configUrl; }

  /** @return the (static) default properties **/
  public static Map getDefaultProperties() { return defaultProperties; }


  /** return the index of the first non-alphanumeric character 
   * at or after i.
   **/
  private static int indexOfNonAlpha(String s, int i) {
    int l = s.length();
    for (int j = i; j<l; j++) {
      char c = s.charAt(j);
      if (!Character.isLetterOrDigit(c)) return j;
    }
    return -1;
  }

  static String substituteProperties(String s, Map props) {
    int i = s.indexOf('$');
    if (i >= 0) {
      int j = indexOfNonAlpha(s,i+1);
      String s0 = s.substring(0,i);
      String s2 = (j<0)?"":s.substring(j);
      String k = s.substring(i+1,(j<0)?s.length():j);
      Object o = props.get(k);
      if (o == null) {
        throw new IllegalArgumentException("No such path property \""+k+"\"");
      }
      return substituteProperties(s0+o.toString()+s2, props);
    }
    return s;
  }

  public static final URL urlify(String s) {
    s = s.replace('\\', '/').replace('\\', '/'); // These should be URL-like
    try {
      if (!s.endsWith("/")) s += "/";
      return new URL(s);
    } catch (MalformedURLException mue) {}

    URL u = filenameToURL(s);
    return u;
  }
    
  private static final URL filenameToURL(String s) {
    try {
      File f = new File(s);
      return (new File(s)).getCanonicalFile().toURL();
    } catch (Exception e) {
      return null;
    }
  }    

  /** Utility method for resolving filename or url-like path elements to URLs.
   * These are to be interpreted as filenames, never directories.
   * additionally interprets the url as relative to COUGAAR_INSTALL_PATH if it resolves 
   * as a relative URL.
   * @note Since this method is static, only the static defaultProperties are used.
   **/
  public final static URL canonicalizeElement(String el) {
    String rs = substituteProperties(el,defaultProperties);
    try {
      return new URL(installUrl, rs);
    } catch (MalformedURLException mue) {}
    return filenameToURL(rs);
  }

  static {
    Map m = new HashMap();

    File ipf = new File(System.getProperty("org.cougaar.install.path", "."));
    try { ipf = ipf.getCanonicalFile(); } catch (IOException ioe) {}
    String ipath = ipf.toString();
    m.put("INSTALL", ipath);
    installUrl = urlify(ipath);

    m.put("HOME", System.getProperty("user.home"));
    m.put("CWD", System.getProperty("user.dir"));

    File csf = new File(ipath, "configs");
    try { csf = csf.getCanonicalFile(); } catch (IOException ioe) {}
    String cspath = csf.toString();
    m.put("CONFIGS", cspath);
    configUrl = urlify(cspath);

    String cs = System.getProperty("org.cougaar.config", "common");
    if (cs != null)
      m.put("CONFIG", cs);

    defaultProperties = Collections.unmodifiableMap(m);

    String config_path = System.getProperty("org.cougaar.config.path");
    if (config_path != null && 
	config_path.charAt(0) == '"' &&
	config_path.charAt(config_path.length()-1) == '"') {
      config_path = config_path.substring(1, config_path.length()-1);
    }
    if (config_path == null) {
      config_path = defaultConfigPath;
    } else {
      config_path = config_path.replace('\\', '/'); // Make sure its a URL and not a file path
      if (config_path.endsWith(";")) config_path += defaultConfigPath;
    }
    configPath=config_path;
  }
}