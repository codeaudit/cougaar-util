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

package org.cougaar.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class to parse a List of "name=value" Strings into
 * a Map with helper methods, for example "int getInt(name, deflt)".
 * <p>
 * Example use:<pre>
 *   public class MyPlugin .. {
 *     // initialize args to the empty instance
 *     private Arguments args = Arguments.EMPTY_INSTANCE;
 *     // "setParameter" is only called if a plugin has parameters
 *     public void setParameter(Object o) {
 *       args = new Arguments(o);
 *     }
 *     public void load() {
 *       super.load();
 *       int foo = args.getInt("foo", 123);
 *     }
 *   }
 * </pre>
 */
public class Arguments implements Serializable {

  /** A singleton instance for an empty map */
  public static final Arguments EMPTY_INSTANCE = new Arguments(null);

  private final Map m;

  public Arguments(Object o) {
    m = parseMap(o);
  }

  protected Map parseMap(Object o) {
    if (!(o instanceof List)) {
      // throw exception if non-null?
      return Collections.EMPTY_MAP;
    }
    List l = (List) o;
    int n = l.size();
    if (n == 0) {
      return Collections.EMPTY_MAP;
    }
    Map ret = new HashMap(n);
    for (int i = 0; i < n; i++) {
      String s = (String) l.get(i);
      int sepIdx = s.indexOf('=');
      if (sepIdx < 0) {
        // throw exception?
        continue;
      }
      String key = s.substring(0, sepIdx);
      String value = s.substring(sepIdx+1);
      ret.put(key, value);
    }
    ret = Collections.unmodifiableMap(ret);
    return ret;
  }

  public String getString(String key) {
    return getString(key, null);
  }

  public String getString(String key, String deflt) {
    String value = (String) m.get(key);
    return (value == null ? deflt : value);
  }

  public boolean getBoolean(String key, boolean deflt) {
    String value = getString(key);
    return (value == null ? deflt : "true".equals(value));
  }

  public int getInt(String key, int deflt) {
    String value = getString(key);
    return (value == null ? deflt : Integer.parseInt(value));
  }

  public long getLong(String key, long deflt) {
    String value = getString(key);
    return (value == null ? deflt : Long.parseLong(value));
  }

  public double getDouble(String key, double deflt) {
    String value = getString(key);
    return (value == null ? deflt : Double.parseDouble(value));
  }

  public Set getKeys() {
    return m.keySet();
  }

  public String toString() {
    return "args["+m.size()+"]="+m;
  }
}
