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

package org.cougaar.lib.contract.lang.cache;

import java.util.*;
import java.lang.reflect.*;

import org.cougaar.lib.contract.lang.*;

/** 
 * Class reflection utilities.
 * <p>
 * Add cache here.  
 **/
public final class ClassCache {

  /** classname package prefixes used by <tt>findClass</tt>. **/
  private static String[] packages = new String[0];

  /** Not synchronized with <tt>findClass</tt>!. */
  public static final void setPackages(Collection c) {
    packages = new String[c.size()];
    Iterator iter = c.iterator(); 
    for (int i = 0; iter.hasNext(); i++) {
      Object oi = iter.next();
      if (!(oi instanceof String)) {
        throw new ClassCastException("Expecting \"String\" packages");
      }
      packages[i] = (String)oi;
    }
  }

  /** Not synchronized with <tt>findClass</tt>!. */
  public static final void setPackages(String[] sa) {
    if (sa != null) {
      packages = new String[sa.length];
      for (int i = 0; i < sa.length; i++) {
        String si = sa[i];
        if (si == null) {
          throw new ClassCastException("Expecting \"String\" packages");
        }
        packages[i] = si;
      }
    } else {
      packages = new String[0];
    }
  }

  public static final String toString(final Class c) {
    return toString(c, true);
  }

  /**
   * Format <code>Class</code> to trim known packages.
   */
  public static final String toString(final Class c, final boolean verbose) {
    String s = c.getName();
    if (!verbose) {
      int pkgSep = s.lastIndexOf('.');
      if (pkgSep > 0) {
        ++pkgSep;
        for (int i = 0; i < packages.length; i++) {
          String pi = packages[i];
          if ((pi.length() == pkgSep) &&
              (s.startsWith(pi))) {
            // use short classname within package
            s = s.substring(pkgSep);
            break;
          }
        }
      }
    }
    return s.replace('$', '.');
  }

  /**
   * Find <code>Class</code> "name".
   **/
  public static final Class lookup(final String name) {
    // try the given name
    try {
      return Class.forName(name);
    } catch (Exception e) { }

    // see if it contains a "."
    int dotSep = name.lastIndexOf('.');
    if (dotSep < 0) {
      // no "." -- try prefixing with packages
      for (int i = 0; i < packages.length; i++) {
        String fullname = packages[i]+name;
        try {
          return Class.forName(fullname);
        } catch (Exception e) { }
      }
      // no such class
      return null;
    }
    
    // see if this is some inner class, which requires the
    //   substitution of the "." separator with a "$"
    String currName = name;
    while (true) {
      // replace the last "." with a "$"
      StringBuffer buf = new StringBuffer(currName);
      buf.setCharAt(dotSep, '$');
      currName = buf.toString();

      // try package prefixes
      String fullname = currName;
      int i = -1;
      while (true) {
        try {
          return Class.forName(fullname);
        } catch (Exception e) { }
        if (++i >= packages.length) {
          break;
        }
        fullname = packages[i]+currName;
      }

      // get the next "."
      dotSep = currName.lastIndexOf('.');
      if (dotSep < 0) {
        // no more potential inner classes
        break;
      }
    }

    // no such class
    return null;
  }
}
