/*
 * <copyright>
 *  
 *  Copyright 1997-2007 BBNT Solutions, LLC
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import junit.framework.TestCase;
import org.cougaar.bootstrap.SystemProperties;

public class TestArguments extends TestCase {

  // test the typical plugin usage
  public void test_parse0() {
    Collection c = Arrays.asList(new String[] {
      "foo=bar", "x=y", "count=1234"});
    String clname = "org.MyPlugin";

    Arguments args = new Arguments(c, clname+".");

    assertEquals(args.getString("foo"), "bar");
    assertEquals(args.getString("x"), "y");
    assertEquals(args.getInt("count"), 1234);

    String propertyPrefix = clname+".";
    Properties props = 
      SystemProperties.getSystemPropertiesWithPrefix(
          propertyPrefix);
    if (props != null && !props.isEmpty()) {
      for (Enumeration en = props.propertyNames();
          en.hasMoreElements();
          ) {
        String name = (String) en.nextElement();
        if (!name.startsWith(propertyPrefix)) continue;
        String key = name.substring(propertyPrefix.length());
        String value = props.getProperty(name);

        List<String> l = args.getStrings(key);

        assertTrue(
            "Missing -D"+name+"="+value+" in "+args,
            l != null && l.contains(value));
      }
    }
  }

  // test non-string input types
  public void test_parse1() {
    Arguments a1 = new Arguments(
        new String[] {"alpha=beta", "foo=bar", "x=y"});
    assertEquals("{alpha=[beta], foo=[bar], x=[y]}", a1.toString());
    Map m = new LinkedHashMap();
    m.put("alpha", "zeta");
    m.put("x", "z");
    Arguments a2 = new Arguments(m, null, a1);
    assertEquals("{alpha=[zeta], x=[z], foo=[bar]}", a2.toString());
  }

  // test defaults and non-string types
  public void test_parse2() {
    Arguments args = new Arguments(
        "one=1, filterMe=blah, two=2",
        "org.MyPlugin.",
        "def=ault",
        "one, two, def, fromProp");

    String fromProp = SystemProperties.getProperty("org.MyPlugin.fromProp");
    boolean hasProp = (fromProp != null);

    assertTrue(args.size() == 3 + (hasProp ? 1 : 0));

    assertEquals(
        "{one=[1], two=[2], "+
        (hasProp ? "fromProp=["+fromProp+"]" : "")+
        ", def=[ault]}",
        args.toString());

    assertEquals("1", args.getString("one"));
    assertEquals("2", args.getString("two"));
    assertEquals(Collections.singletonList("2"), args.getStrings("two"));
    assertEquals("ault", args.getString("def"));
    assertEquals(null, args.getString("bar"));
    assertEquals(
        (hasProp ? Integer.parseInt(fromProp) : 1234),
        args.getInt("fromProp", 1234));
  }

  // test "swap" method
  public void test_swap0() {
    Arguments args = new Arguments("a=1, b=2, c=3");

    Arguments a2 = args.swap("a", "c");
    assertEquals("{c=[1], a=[3], b=[2]}", a2.toString());

    a2 = args.swap("a", "x");
    assertEquals("{x=[1], b=[2], c=[3]}", a2.toString());

    a2 = args.swap("x", "y");
    assertEquals("{a=[1], b=[2], c=[3]}", a2.toString());
  }

  // test "set" methods
  public void test_set0() {
    Arguments args = new Arguments("a=1, b=2, c=3");

    Arguments a2 = args.setString("a", "foo");
    assertEquals("{a=[foo], b=[2], c=[3]}", a2.toString());

    a2 = args.setString("a", null);
    assertEquals("{b=[2], c=[3]}", a2.toString());

    a2 = args.setStrings("a", Arrays.asList(new String[] { "foo", "bar"}));
    assertEquals("{a=[foo, bar], b=[2], c=[3]}", a2.toString());
  }

  // test "callSetters" reflection
  public void test_reflection0() {
    String s =
        "l=1, l=2, s=x, i=123, dub=4.5, lng=6789, "+
        "col=q, col=r, col=s, str=bar, integ=99, d=-3.1";
    Arguments args = new Arguments(
        "unk=alpha, "+
        s +
        ", junk=beta, s=duplicate, i=42");
    Foo f = new Foo();
    Set<String> unset = args.callSetters(f);
    assertEquals(
        "{"+s+", ignored=1234}",
        f.toString());
    assertEquals(
        "[unk, junk]",
        unset.toString());
  }
  public static final class Foo {
    public List l;
    public String s = "bad";
    public int i = -123;
    public double dub = -4.56;
    public long lng = -798;

    public int ignored = 1234;

    private Collection col;
    private String str;
    private int integ;
    private double d;

    public void setCol(Collection c) { col = c; }
    public void setStr(String s) { str = s; }
    public void setInteg(int i) { integ = i; }
    public void setD(double d) { this.d = d; }
    public void setLng(long l) { this.lng = l; }

    public String toString() {
      String ret = "{";
      if (l != null) {
        for (Object o : l) {
          ret += "l="+o+", ";
        }
      }
      ret += "s="+s+", ";
      ret += "i="+i+", ";
      ret += "dub="+dub+", ";
      ret += "lng="+lng+", ";
      if (col != null) {
        for (Object o : col) {
          ret += "col="+o+", ";
        }
      }
      ret += "str="+str+", ";
      ret += "integ="+integ+", ";
      ret += "d="+d+", ";
      ret += "ignored="+ignored;
      ret += "}";
      return ret;
    }
  }

  // test "toString" formatting
  public void test_format0() {
    Arguments args = new Arguments("A=B, X=V0, X=V1, X=V2");

    assertEquals(
        "{A=[B], X=[V0, V1, V2]}",
        args.toString());
    assertEquals(
        "the_A is the_B * the_X is the_V0",
        args.toString("the_$key is the_$value", " * "));
    assertEquals(
        "(A eq B) +\n(X eq [V0, V1, V2])",
        args.toString("($key eq $vals)", " +\n"));
    assertEquals(
        "A=B&amp;X=V0&amp;X=V1&amp;X=V2",
        args.toString("$key=$veach", "&amp;"));
    assertEquals(
        ("<argument name=\"A\" value=\"B\"/>\n"+
         "<argument name=\"X\" value=\"V0\"/>\n"+
         "<argument name=\"X\" value=\"V1\"/>\n"+
         "<argument name=\"X\" value=\"V2\"/>"),
        args.toString("<argument name=\"$key\" value=\"$veach\"/>", "\n"));
    assertEquals(
        "A=[B], X=[V0, V1, V2]",
        args.toString("$key=$vlist", ", "));
  }

  // test read-back from toString
  public void test_format1() {
    Arguments a1 = new Arguments("a=1, b=2a, c=3, b=2b");
    String s = a1.toString("$key=$veach");
    Arguments a2 = new Arguments(s);
    assertEquals(a1, a2);
    assertEquals(a1.toString(), a2.toString());
  }

}
