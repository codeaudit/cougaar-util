/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
 * @see <code>org.cougaar.util.StringObjectFactory</code>.
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
   * @see org.cougaar.util.StringObjectFactory
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
