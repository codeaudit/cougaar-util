/*
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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

package org.cougaar.bootstrap;

import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import java.security.*;
import java.security.cert.*;
import java.util.regex.*;

/**
 * A Bootstrapper which performs a variety of tests on the
 * resources found in the hopes of catching problems
 * 
 * @property org.cougaar.bootstrap.class=org.cougaar.bootstrap.CheckingBootstrapper Enables
 * the CheckingBootstrapper to check jarfile versions at startup time for mismatches.
 **/

public class CheckingBootstrapper extends Bootstrapper
{
  private static Pattern modPattern = Pattern.compile(".*/([^/]*)\\.\\w*$");
  private static Pattern manPattern = Pattern.compile("^Manifest/(.*)\\.version$");
  private static Pattern kvPattern = Pattern.compile("^(\\w*)\\s*=\\s*(.*)$");
  
  protected List filterURLs(List l) {
    List o = super.filterURLs(l);
    scanAll();
    return o;
  }

  protected boolean checkURL(URL url) {
    boolean ok = super.checkURL(url);
    if (ok) {
      scanURL(url);
    }
    return ok;
  }

  private Map checkedJars = new HashMap(29);

  /** parse out the module name from the url if possible **/
  private String getModName(URL url) {
    String s = url.getFile();   // e.g. /tmp/foo/lib/core.jar
    Matcher m = modPattern.matcher(s);
    if (m.matches()) {
      String mod = m.group(1);
      return mod;
    } else {
      return null;
    }
  }

  // look for "/Manifest/<something>.version"
  private String getManName(String path) {
    Matcher m = manPattern.matcher(path);
    if (m.matches()) {
      String man = m.group(1);
      return man;
    } else {
      return null;
    }
  }

  private void scanURL(URL url) {
    String mod = getModName(url);
    try {
      InputStream is = url.openStream();
      
      ZipInputStream zis = new ZipInputStream(is);
        
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        String path = ze.getName();
        String man = getManName(path);
        if (man != null) {
          if (!(man.equals(mod))) {
            System.err.println("Warning: Classpath entry "+url+" should be module "+mod+" but appears to be "+man);
          }
          BufferedReader br = new BufferedReader(new InputStreamReader(zis));
          String l;
          HashMap map = new HashMap(11);
          while ((l = br.readLine()) != null) {
            Matcher m = kvPattern.matcher(l);
            if (m.matches()) {
              String key = m.group(1);
              String value = m.group(2);
              map.put(key, value);
            }
          }
          map.put("URL", url.toString());
          map.put("Manifest", path);

          String k = (String) map.get("NAME");
          if (k == null) k = man;
          if (checkedJars.get(k) != null) {
            Map ack = (Map) checkedJars.get(k);
            System.err.println("Warning: Module "+k+" found in two locations:");
            System.err.println("\t"+ack.get("URL"));
            System.err.println("\t"+map.get("URL")+" (ignored)");
          } else {
            checkedJars.put(k, map);
          }
        }
        zis.closeEntry();
      }
      zis.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void scanAll() {
    String iname = null;        // index module name... should really be core or somesuch
    String rtime = null;
    String rtag = null;
    String url = null;

    for (Iterator kit = checkedJars.keySet().iterator(); kit.hasNext(); ) {
      String key = (String) kit.next();
      Map map = (Map) checkedJars.get(key);
      
      String mname = (String) map.get("NAME");
      String mrtime = (String) map.get("REPOSITORY_TIME");
      String mrtag = (String) map.get("REPOSITORY_TAG");
      String mcomment = (String) map.get("COMMENT");
      String murl = (String) map.get("URL");

      if (mname == null || mrtime == null || mrtag == null || murl == null) {
        System.err.println("Warning: Jarfile \""+key+"\" has incomplete manifest.");
        continue;
      }

      if (iname == null) {
        iname = mname;
        rtime = mrtime;
        rtag = mrtag;
        url = murl;
        continue;
      } 

      if (! mrtag.equals(rtag)) {
        System.err.println("Warning: Jarfile Repository tag mismatch:");
        System.err.println("\t"+iname+"("+url+") = "+rtag);
        System.err.println("\t"+mname+"("+murl+") = "+mrtag);
      }

      if (! mrtime.equals(rtime)) {
        System.err.println("Warning: Jarfile Repository time mismatch:");
        System.err.println("\t"+iname+"("+url+") = "+rtime);
        System.err.println("\t"+mname+"("+murl+") = "+mrtime);
      }
    }
  }


}
