/***************************************************************
 *                                                              
 *  Copyright 1997-1999 Defense Advanced Research Projects 
 *  Agency and ALPINE (A BBN Technologies (BBN) and Raytheon 
 *  Systems Company (RSC) Consortium). This software to be used 
 *  in accordance with the COUGAAR license agreement.                                   
 *                                                              
 ****************************************************************
 */

package org.cougaar.launcher;

import java.beans.Beans;
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;

/** Prototype of a network-based Node launcher which is capable of
 * retrieving java code over the network (or from the local filesystem)
 * on demand.
 *
 * Arguments are exactly that of Node.
 * The goal is to require only an org.cougaar.install.path system property
 * and a classpath which contains only this class (and its inner classes).
 *
 * Example:
 * java -classpath launcher.jar -Dorg.cougaar.install.path=http://draught/alp org.cougaar.launcher.Launcher admin
 */

public class Launcher {
  public static void main(String args[]) {
    try {
      URL ipath = toURL(System.getProperty("org.cougaar.install.path"));
      System.err.println("Install URL = "+ipath);
      URL[] urls = searchForJars(ipath, "lib");
      URLClassLoader cl = new URLClassLoader(urls);
      Class c = cl.loadClass("org.cougaar.core.society.Node");
      Class[] argts = {String[].class};
      Method m = c.getMethod("main", argts);
      Object[] argl = {args};
      m.invoke(null, argl);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // the following code comes from PluginLoader and should be identical.

  /** convert the string reference to a URL. **/
  private static URL toURL(String s) {
    try {
      return new URL(s);
    } catch (MalformedURLException e) {
      try {
        return new URL("file:"+s);
      } catch (MalformedURLException e1) {
        throw new RuntimeException("Couldn't resolve '"+s+"' into a URL: "+e1);
      }
    }
  }
  // assume url is a directory, add s onto the URL
  private static URL addURL(URL url, String s) throws MalformedURLException {
    try {
      return new URL(s);        // is s a full URL?
    } catch (MalformedURLException e) {
      return new URL(url+"/"+s);
    }
  }


  private static URL[] searchForJars(URL base, String ext) {
    try {
      return searchForJars(addURL(base,ext));
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return new URL[0];
    }
  }

  /** look for jar files in org.cougaar.install.path/subdir **/
  private static URL[] searchForJars(URL sub) {
    //System.err.println("Searching in "+sub);
    URL[] urls = searchURL(sub, new URLFilter() {
        public boolean accept(URL f) {
          String name = f.getFile();
          return (name.endsWith(".jar") ||
                  name.endsWith(".zip") ||
                  name.endsWith(".plugin"));
        }
      });
      
    return urls;
  }

  private static interface URLFilter {
    boolean accept(URL url);
  }

  /** look for jar files in org.cougaar.install.path/PRODUCT/lib **/
  private static URL[] searchForProductJars(URL base) {
    ArrayList urlv = new ArrayList();

    URL[] ds = searchURL(base);
    int l = ds.length;
    for (int i=0; i<l; i++) {
      URL d = ds[i];
      URL[] subs = searchForJars(d, "/lib");
      int sl = subs.length;
      for (int j = 0; j<sl; j++) {
        urlv.add(subs[j]);
      }
    }
      
    int ul = urlv.size();
    URL[] urls = new URL[ul];
    for (int i = 0; i < ul; i++) {
      urls[i] = (URL) urlv.get(i);
    }
    return urls;
  }

  private static URL[] searchURL(URL url) {
    return searchURL(url, null);
  }

  private static URL[] searchURL(URL url, URLFilter filter) {
    ArrayList urlv = new ArrayList();
    
    try {
      URLConnection c = url.openConnection();
      InputStream s = c.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(s));
      for (String line=br.readLine(); line!=null; line=br.readLine()) {
        int i = 0;
        int p;
        //System.err.println("Read line '"+line+"'");
        while ((p = line.indexOf("href=\"", i)) >-1) {
          int q = line.indexOf("\">",p+1);
          if (q>-1) {
            String ref = line.substring(p+6, q);
            //System.err.print("sub='"+ref+"("+(p+6)+","+q+") ");
            URL u = addURL(url,ref);
            //System.err.print("\t"+u);
            if (filter == null || filter.accept(u)) {
              urlv.add(u);
              //System.err.println(" accepted "+i);
            } else {
              //System.err.println(" rejected "+i);
            }
            i=q+1;
          } else {
            break;              // bogon
          } 
        }
      }
      br.close();

      int ul = urlv.size();
      URL[] urls = new URL[ul];
      for (int i = 0; i < ul; i++) {
        urls[i] = (URL) urlv.get(i);
      }
      return urls;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new URL[0];
  }
}  
