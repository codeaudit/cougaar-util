/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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

package org.cougaar.bootstrap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
  
  @Override
protected List filterURLs(List l) {
    List o = super.filterURLs(l);
    scanAll();
    return o;
  }

  @Override
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
      //      String mcomment = (String) map.get("COMMENT");
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
