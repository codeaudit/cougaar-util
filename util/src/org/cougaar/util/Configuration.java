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
package org.cougaar.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.cougaar.bootstrap.SystemProperties;

/**
 * Configuration is a holder of a collection of static configuration utility methods,
 * mostly for use by ConfigFinder et al.
 **/
public final class Configuration {

  public static final char SEP_CHAR = ';';
  public static final String SEP = ""+SEP_CHAR;

  public static final String RUNTIME_PATH_PROP = "org.cougaar.runtime.path";
  public static final String SOCIETY_PATH_PROP = "org.cougaar.society.path";
  public static final String INSTALL_PATH_PROP = "org.cougaar.install.path";
  public static final String CONFIG_PATH_PROP = "org.cougaar.config.path";
  public static final String WORKSPACE_PROP = "org.cougaar.workspace";
  public static final String CONFIG_PROP = "org.cougaar.config";
  public static final String USER_HOME_PROP = "user.home";
  public static final String USER_DIR_PROP = "user.dir";

  public static final String DEFAULT_CONFIG_PATH =
    "$CWD"+                     SEP+
    "$RUNTIME/configs/$CONFIG"+ SEP+
    "$RUNTIME/configs/common"+  SEP+
    "$SOCIETY/configs/$CONFIG"+ SEP+
    "$SOCIETY/configs/common"+  SEP+
    "$INSTALL/configs/$CONFIG"+ SEP+
    "$INSTALL/configs/common";

  // these are initialized at the end
  private static Map defaultProperties;
  private static URL installUrl;
  private static URL configUrl;
  private static URL workspaceUrl;
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

  /** @return the workspace location **/
  public static URL getWorkspaceURL() { return workspaceUrl; }

  /** @return the (static) default properties **/
  public static Map getDefaultProperties() { return defaultProperties; }

  /** return the index of the first non-alphanumeric, non-underbar character 
   * at or after i.
   **/
  private static int indexOfNonAlpha(String s, int i) {
    int l = s.length();
    for (int j = i; j<l; j++) {
      char c = s.charAt(j);
      if (!Character.isLetterOrDigit(c) && c!='_') return j;
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

  public static final URL urlify(String s) throws MalformedURLException {
//    MalformedURLException savedx = null;
    s = s.replace('\\', '/').replace('\\', '/'); // These should be URL-like
    if (s.startsWith("resource://")) {
      s = "file:/IN_COUGAAR_JARS/"+s.substring("resource://".length());
    }
    if (!s.endsWith("/")) s += "/";
    if (s.indexOf(":/") >= 0) {
      if (s.charAt(0) == '/' && s.matches("^/\\w{3,}:/.*$")) {
        // remove the leading (erroneous) "/" character.
        //
        // The File constructor does this automatically:
        //   "//foo/"  -->  "/foo/"
        // so we want similar support for URLs:
        //   "/file:/foo/"  -->  "file:/foo/"
        // Without this fix, we'd get:
        //   "/file:/foo/"  -->  "file:/file:/foo/"
        s = s.substring(1);
      }
      try {
        return new URL(s);
      } catch (MalformedURLException mue) {
//        savedx = mue;
      }
    }

    try {
      return filenameToURL(s);
    } catch (MalformedURLException mue) {
      // would be nice to use savedx, too
      throw new MalformedURLException("Could not convert \""+s+"\" to a URL");
    }
  }
    
  private static final String getCanonicalPath(String s) {
    File f = new File(s);
    try { 
      f = f.getCanonicalFile();
    } catch (IOException ioe) {
      // okay
    } catch (AccessControlException ace) {
      String msg = ace.getMessage();
      if (msg != null && 
          msg.equals("access denied (java.util.PropertyPermission user.dir read)")) {
        // okay, must be in sandbox
        //
        // Usually we'd return f, but for some reason the file wrapper
        // turns "http://x" into "http:/x", which is broken.
        return s;
      } else {
        throw new RuntimeException("Security exception", ace);
      }
    }
    return f.toString();
  }

  private static final URL filenameToURL(String s) throws MalformedURLException {
    try {
      File f = new File(s);
      return f.getCanonicalFile().toURL();
    } catch (Exception e) {
      throw new MalformedURLException("Cannot convert string to file URL "+s);
    }
  }    

  /** Utility method for resolving filename or url-like path elements to URLs.
   * These are to be interpreted as filenames, never directories.
   * additionally interprets the url as relative to COUGAAR_INSTALL_PATH if it resolves 
   * as a relative URL.
   * @note Since this method is static, only the static defaultProperties are used.
   **/
  public final static URL canonicalizeElement(String el) throws MalformedURLException {
    String rs = substituteProperties(el,defaultProperties);
    try {
      return new URL(installUrl, rs);
    } catch (MalformedURLException mue) {}
    return filenameToURL(rs);
  }

  /** resolve Configuration variables (default ones only) in the argument
   * string. For example, convert "$INSTALL/foo.txt" to "/opt/cougaar/030330/foo.txt"
   **/
  public final static String resolveValue(String el) {
    return substituteProperties(el, defaultProperties);
  }

  static {
    Map m = new HashMap();

    String runtime_path = SystemProperties.getProperty(RUNTIME_PATH_PROP);
    if (runtime_path != null && runtime_path.length() > 0) {
      runtime_path = getCanonicalPath(runtime_path);
      m.put("RUNTIME", runtime_path);
      m.put("CRP", runtime_path);        // alias for RUNTIME
      m.put("COUGAAR_RUNTIME_PATH", runtime_path); // for completeness
    }

    String society_path = SystemProperties.getProperty(SOCIETY_PATH_PROP);
    if (society_path != null && society_path.length() > 0) {
      society_path = getCanonicalPath(society_path);
      m.put("SOCIETY", society_path);
      m.put("CSP", society_path);        // alias for SOCIETY
      m.put("COUGAAR_SOCIETY_PATH", society_path); // for completeness
    }

    String install_path = SystemProperties.getProperty(INSTALL_PATH_PROP);
    if (install_path != null && install_path.length() > 0) {
      install_path = getCanonicalPath(install_path);
      m.put("INSTALL", install_path);
      m.put("CIP", install_path);        // alias for INSTALL
      m.put("COUGAAR_INSTALL_PATH", install_path); // for completeness
      try {
        installUrl = urlify(install_path);
      } catch (MalformedURLException e) { e.printStackTrace(); }
    }

    String workspace = SystemProperties.getProperty(WORKSPACE_PROP);
    if (workspace == null || workspace.length() <= 0) {
      for (int i = 0; i < 3; i++) {
        String key = (i == 0 ? "RUNTIME" : i == 1 ? "SOCIETY" : "INSTALL");
        String base = (String) m.get(key);
        if (base != null) {
          workspace = base+"/workspace";
          break;
        }
      }
    }
    if (workspace != null) {
      m.put("WORKSPACE", workspace);
      try {
        workspaceUrl = urlify(workspace);
      } catch (MalformedURLException e) { e.printStackTrace(); }
    }

    m.put("HOME", SystemProperties.getProperty(USER_HOME_PROP));
    m.put("CWD", SystemProperties.getProperty(USER_DIR_PROP));

    String configs_path = null;
    for (int i = 0; i < 3; i++) {
      String key = (i == 0 ? "RUNTIME" : i == 1 ? "SOCIETY" : "INSTALL");
      String base = (String) m.get(key);
      if (base != null) {
        configs_path = base+"/configs";
        break;
      }
    }
    if (configs_path != null) {
      configs_path = getCanonicalPath(configs_path);
      m.put("CONFIGS", configs_path);
      try {
        configUrl = urlify(configs_path);
      } catch (MalformedURLException e) { e.printStackTrace(); }
    }

    m.put("CONFIG", SystemProperties.getProperty(CONFIG_PROP, "common"));

    defaultProperties = Collections.unmodifiableMap(m);

    String config_path = SystemProperties.getProperty(CONFIG_PATH_PROP);
    if (config_path != null && 
        config_path.length() > 0 &&
	config_path.charAt(0) == '"' &&
	config_path.charAt(config_path.length()-1) == '"') {
      config_path = config_path.substring(1, config_path.length()-1);
    }
    boolean append_default = false;
    if (config_path == null) {
      config_path = "";
      append_default = true;
    } else if (config_path.length() > 0) {
      config_path = config_path.replace('\\', '/'); // Make sure its a URL and not a file path
      append_default = config_path.endsWith(SEP);
    }
    if (append_default) {
      // append default path, but only path elements that contain known keys.
      //
      // For example, ignore "$RUNTIME/configs/common" if "$RUNTIME" is not set.
      String[] sa = DEFAULT_CONFIG_PATH.split("\\s*"+SEP+"\\s*");
      boolean needs_sep = false;
loop:
      for (int i = 0; i < sa.length; i++) {
        String path = sa[i].trim();
        if (path.length() <= 0) {
          continue;
        }
        String[] sai = path.split("/");
        for (int j = 0; j < sai.length; j++) {
          String sj = sai[j];
          if (sj.length() > 0 && sj.charAt(0) == '$') {
            String key = sj.substring(1);
            if (!defaultProperties.containsKey(key)) {
              continue loop;
            }
          }
        }
        if (needs_sep) {
          config_path += SEP;
        } else {
          needs_sep = true;
        }
        config_path += path;
      }
    }

    configPath = config_path;
  }
}
