/*
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
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
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cougaar.bootstrap.SystemProperties;

/**
 * A "name=value" parser.
 * <p>
 * Note that Arguments instances are unmodifiable, like Strings.
 * <p>
 * Example use:
 * 
 * <pre>
 *   Arguments args = new Arguments("x=y, q=one, q=two, z=99");
 *   String x = args.getString("x");
 *   assert "y".equals(x);
 *   int z = args.getInt("z", 1234);
 *   assert z == 99;
 *   List&lt;String&gt; q = args.getStrings("q");
 *   assert q != null &amp;&amp; q.size() == 2;
 * </pre>
 * 
 * <p>
 * The Cougaar component model includes built-in support to invoke an optional
 * "setArguments" method. Here's an example:
 * 
 * <pre>
 *   package org;
 *   public class MyPlugin ... {
 *     private Arguments args;
 *     // The "setArguments" method is special -- it's an optional method
 *     // that's found by the component model via reflection.  The passed-in
 *     // arguments instance is created via:
 *     //    new Arguments(listOfStrings, classname);
 *     public void setArguments(Arguments args) { this.args = args; }
 *     public void load() {
 *       super.load();
 *       // Get the value of our "foo" argument
 *       //
 *       // First looks for a plugin XML argument named "foo",
 *       // next looks for a "-Dorg.MyPlugin.foo=" system property,
 *       // otherwise the value will be the 1234 default.
 *       int foo = args.getInt("foo", 1234);
 *       System.out.println("foo is "+foo);
 *     }
 *   }
 * </pre>
 * 
 * <p>
 * The {@link #callSetters} method supports "setter" reflection, for example:
 * 
 * <pre>
 *   package org;
 *   public class MyPlugin ... {
 *     private int foo = 1234;
 *     public void setArguments(Arguments args) { args.callSetters(this); }
 *     // This "set<i>NAME</i>(<i>TYPE</i>)" method is found by reflection.
 *     // The args class will only invoke the setters for which it has values.
 *     public void setFoo(int i) { this.foo = i; }
 *     public void load() {
 *       super.load();
 *       System.out.println("foo is "+foo);
 *     }
 *   }
 * </pre>
 */
public final class Arguments extends AbstractMap<String, List<String>>
    implements
        Serializable {

    /** A singleton instance for an empty arguments */
    public static final Arguments EMPTY_INSTANCE = new Arguments(null);

    /** @see toString(String,String) */
    private static final Pattern PATTERN = Pattern.compile("\\$(key|value|vals|veach|vlist)");

    private static final int OPTIMIZE_SIZE = 5;

    private final Map<String, List<String>> m;

    public Arguments(Object o) {
        this(o, null);
    }

    public Arguments(Object o, Object propertyPrefix) {
        this(o, propertyPrefix, null);
    }

    public Arguments(Object o, Object propertyPrefix, Object deflt) {
        this(o, propertyPrefix, deflt, null);
    }

    /**
     * @param o the optional input object, e.g. a List of name=value Strings, or
     *            another Arguments instance.
     * @param propertyPrefix the optional SystemProperties property prefix, e.g.
     *            "org.MyPlugin.", for "-Dorg.MyPlugin.name=value" lookups. If a
     *            class is specified, that class's name+. and its parent's
     *            names+. will be used.
     * @param deflt the optional default values, e.g. a List of name=value
     *            Strings, or another Arguments instance.
     * @param keys the optional filter on which keys are allowed, e.g. only
     *            allow (A, B, C)
     */
    public Arguments(Object o, Object propertyPrefix, Object deflt, Object keys) {
        try {
            Map<String, List<String>> m2 = parseMap(o);
            List<String> prefixes = parsePrefixes(propertyPrefix);
            Map<String, List<String>> def = parseMap(deflt);
            Set<String> ks = parseSet(keys);
            this.m = parse(m2, prefixes, def, ks);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create new Arguments("
                                                       + "\n  o = "
                                                       + o
                                                       + argString(propertyPrefix, deflt, keys)
                                                       + ")",
                                               e);
        }
    }

    private String argString(Object propertyPrefix, Object deflt, Object keys) {
        if (propertyPrefix == null  && deflt == null && keys == null) {
            return "";
        }
        return  ",\n  propertyPrefix = " + propertyPrefix + 
        (deflt == null && keys == null 
                ? ""
                  : (",\n  deflt = " + deflt + 
                          (keys == null ? "" : ",\n  keys = " + keys)));
    }

    /**
     * All the other "get*" methods call this method.
     * <p>
     * Equivalent to:
     * 
     * <pre>
     * List&lt;String&gt; l = get(key);
     * return (l == null ? deflt : l);
     * </pre>
     * 
     * @return the non-empty values or the deflt
     */
    public List<String> getStrings(String key, List<String> deflt) {
        if (m instanceof OptimizedMap) {
            return ((OptimizedMap) m).getStrings(key, deflt);
        }
        List<String> l = m.get(key);
        return (l == null ? deflt : l);
    }

    //
    // Helper methods:
    //

    /** @return the value, or null if not set */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Get the first value, or the specified default if there is no value.
     * <p>
     * Equivalent to:
     * 
     * <pre>
     * List&lt;String&gt; l = get(key);
     * return (l == null ? deflt : l.get(0));
     * </pre>
     * 
     * @return the first value, or the deflt if not set
     */
    public String getString(String key, String deflt) {
        if (m instanceof OptimizedMap) {
            return ((OptimizedMap) m).getString(key, deflt);
        }
        List<String> l = m.get(key);
        return (l == null ? deflt : l.get(0));
    }

    /** @return same as {@link #get(String)} */
    public List<String> getStrings(String key) {
        return getStrings(key, null);
    }

    /** @return the value, or false if not set */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /** @return the first value, or the deflt if not set */
    public boolean getBoolean(String key, boolean deflt) {
        String value = getString(key);
        return (value == null ? deflt : "true".equals(value));
    }

    /** @return the values, or null if not set */
    public List<Boolean> getBooleans(String key) {
        return getBooleans(key, null);
    }

    /** @return the values, or the deflt if not set */
    public List<Boolean> getBooleans(String key, List<Boolean> deflt) {
        List<String> l = getStrings(key, null);
        if (l == null) {
            return deflt;
        }
        int n = l.size();
        if (n == 1) {
            Boolean value = Boolean.valueOf(l.get(0));
            return Collections.singletonList(value);
        }
        List<Boolean> ret = new ArrayList<Boolean>(n);
        for (String s : l) {
            ret.add(Boolean.valueOf(s));
        }
        return Collections.unmodifiableList(ret);
    }

    /** @return the value, or -1 if not set */
    public int getInt(String key) {
        return getInt(key, -1);
    }

    /** @return the first value, or the deflt if not set */
    public int getInt(String key, int deflt) {
        String value = getString(key);
        return (value == null ? deflt : Integer.parseInt(value));
    }

    /** @return the values, or null if not set */
    public List<Integer> getInts(String key) {
        return getInts(key, null);
    }

    /** @return the values, or the deflt if not set */
    public List<Integer> getInts(String key, List<Integer> deflt) {
        List<String> l = getStrings(key, null);
        if (l == null) {
            return deflt;
        }
        int n = l.size();
        if (n == 1) {
            Integer value = Integer.valueOf(l.get(0));
            return Collections.singletonList(value);
        }
        List<Integer> ret = new ArrayList<Integer>(n);
        for (String s : l) {
            ret.add(Integer.valueOf(s));
        }
        return Collections.unmodifiableList(ret);
    }

    /** @return the value, or -1 if not set */
    public long getLong(String key) {
        return getLong(key, -1);
    }

    /** @return the first value, or the deflt if not set */
    public long getLong(String key, long deflt) {
        String value = getString(key);
        return (value == null ? deflt : Long.parseLong(value));
    }

    /** @return the value, or null if not set */
    public List<Long> getLongs(String key) {
        return getLongs(key, null);
    }

    /** @return the values, or the deflt if not set */
    public List<Long> getLongs(String key, List<Long> deflt) {
        List<String> l = getStrings(key, null);
        if (l == null) {
            return deflt;
        }
        int n = l.size();
        if (n == 1) {
            Long value = Long.valueOf(l.get(0));
            return Collections.singletonList(value);
        }
        List<Long> ret = new ArrayList<Long>(n);
        for (String s : l) {
            ret.add(Long.valueOf(s));
        }
        return Collections.unmodifiableList(ret);
    }

    /** @return the value, or Double.NaN if not set */
    public double getDouble(String key) {
        return getDouble(key, Double.NaN);
    }

    /** @return the first value, or the deflt if not set */
    public double getDouble(String key, double deflt) {
        String value = getString(key);
        return (value == null ? deflt : Double.parseDouble(value));
    }

    /** @return the value, or null if not set */
    public List<Double> getDoubles(String key) {
        return getDoubles(key, null);
    }

    /** @return the values, or the deflt if not set */
    public List<Double> getDoubles(String key, List<Double> deflt) {
        List<String> l = getStrings(key, null);
        if (l == null) {
            return deflt;
        }
        int n = l.size();
        if (n == 1) {
            Double value = Double.valueOf(l.get(0));
            return Collections.singletonList(value);
        }
        List<Double> ret = new ArrayList<Double>(n);
        for (String s : l) {
            ret.add(Double.valueOf(s));
        }
        return Collections.unmodifiableList(ret);
    }

    /**
     * Split this arguments instance of String-to-List[N] pairs into a List of
     * "flattened" Arguments with String-to-List[1] pairs.
     * <p>
     * This is useful to group together same-named arguments.
     * 
     * <pre>
     * For example, the constructor input:
     *   "foo=f1, bar=b1, qux=q1,
     *    foo=f2, bar=b2, qux=q2,
     *    foo=f3, bar=b3, qux=q3"
     * will be parsed as:
     *   {foo=[f1,f2,f3], bar=[b1,b2,b3], qux=[q1,q2,q3]}
     * and can be split into:
     *   [{foo=[f1], bar=[b1], qux=[q1]},
     *    {foo=[f2], bar=[b2], qux=[q2]},
     *    {foo=[f3], bar=[b3], qux=[q3]}}
     * This simplifies iteration:
     *   for (Arguments a : args.split()) {
     *     System.out.println("foo is "+a.getString("foo"));
     *   }
     * which will print:
     *   foo is f1
     *   foo is f2
     *   foo is f3
     * </pre>
     * 
     * @return a List of Arguments.
     */
    public List<Arguments> split() {
        int n = 1;
        if (!(m instanceof OptimizedMap)) {
            for (List<String> l : m.values()) {
                n = Math.max(n, l.size());
            }
        }
        if (n == 1) {
            return Collections.singletonList(this);
        }
        List<Map<String, String>> ma = new ArrayList<Map<String, String>>(n);
        for (int i = 0; i < n; i++) {
            ma.add(new LinkedHashMap<String, String>());
        }

        for (Map.Entry<String, List<String>> me : m.entrySet()) {
            String key = me.getKey();
            List<String> l = me.getValue();
            for (int i = 0; i < l.size(); i++) {
                ma.get(i).put(key, l.get(i));
            }
        }

        List<Arguments> ret = new ArrayList<Arguments>(n);
        for (Map<String, String> mi : ma) {
            ret.add(new Arguments(mi));
        }
        return Collections.unmodifiableList(ret);
    }

    //
    // Modifiers
    //

    /**
     * Return an Arguments instance where the values for key1 and key2 are
     * swapped.
     * <p>
     * In other words:
     * 
     * <pre>
     * // given:
     * List&lt;String&gt; v1 = args.getStrings(key1);
     * List&lt;String&gt; v2 = args.getStrings(key2);
     * // do swap:
     * Arguments ret = args.swap(key1, key2);
     * // validate:
     * assert(ret.size() == args.size());
     * List&lt;String&gt; x1 = ret.getStrings(key1);
     * List&lt;String&gt; x2 = ret.getStrings(key2);
     * assert(v1 == null ? x2 == null : v1.equals(x2));
     * assert(v2 == null ? x1 == null : v2.equals(x1));
     * </pre>
     * 
     * @return returns a new Arguments instance
     */
    public Arguments swap(String key1, String key2) {
        // could optimize this...
        List<String> v1 = getStrings(key1);
        List<String> v2 = getStrings(key2);
        Arguments ret = setStrings(key1, v2);
        ret = ret.setStrings(key2, v1);
        return ret;
    }

    /** @see #setStrings */
    public Arguments setString(String key, String value) {
        List<String> l = (value == null ? null : Collections.singletonList(value));
        return setStrings(key, l);
    }

    /**
     * @param key the non-null key
     * @param values the values, which can be null or empty to remove the
     *            specified key's entry
     * @return returns a possibly new Arguments instance (like
     *         {@link String#trim} and similar copy-on-modify classes) where
     *         "getStrings(key)" will be equal to the specified "values"
     */
    public Arguments setStrings(String key, List<String> values) {
        // could optimize this...
        List<String> old = getStrings(key);
        if (values == null || values.isEmpty()) {
            if (old == null) {
                return this;
            }
            if (size() == 1) {
                return EMPTY_INSTANCE;
            }
            Set<String> filter = new HashSet<String>(keySet());
            filter.remove(key);
            return new Arguments(this, null, null, filter);
        } else {
            Map<String, List<String>> add = Collections.singletonMap(key, values);
            return new Arguments(add, null, this);
        }
    }

    //
    // Required base class methods:
    //

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return m.entrySet();
    }

    // avoid linear scan through our "entrySet()":
    public int size() {
        return m.size();
    }

    public boolean containsKey(Object key) {
        return (get(key) != null);
    }

    public List<String> get(String key) {
        return m.get(key);
    }

    //
    // Reflection methods:
    //

    /**
     * Call the given object's setter methods or fields for every name=value
     * pair in the {@link #entrySet}, return the Set of unknown String keys.
     * <p>
     * For example, the name=value pair "x=y" will look for:
     * 
     * <pre>
     *   public void setX(<i>type</i>) {..}
     * </pre>
     * 
     * and field:
     * 
     * <pre>
     *   public <i>type</i> x;
     * </pre>
     * 
     * If neither are found then "x" will be included in the returned Set.
     * 
     * @param o Object that has the setter methods &amp; fields
     * @return Subset of the {@link #keySet} that could not be set
     */
    public Set<String> callSetters(Object o) {
        if (isEmpty()) {
            return Collections.emptySet();
        }
        Class cl = o.getClass();
        if (!Modifier.isPublic(cl.getModifiers())) {
            return keySet();
        }
        Set<String> ret = null;
        Method[] methods = cl.getMethods();
        for (Map.Entry<String, List<String>> me : entrySet()) {
            String key = me.getKey();
            List<String> l = me.getValue();

            boolean found = false;

            // look for setter method(s)
            String setter_name = "set" + Character.toUpperCase(key.charAt(0))
                    + key.substring(1);
            for (int i = 0; i < methods.length; i++) {
                Method mi = methods[i];
                if (!Modifier.isPublic(mi.getModifiers())) {
                    continue;
                }
                if (!setter_name.equals(mi.getName())) {
                    continue;
                }
                Class[] p = mi.getParameterTypes();
                if (p.length != 1) {
                    continue;
                }
                try {
                    Object arg = cast(l, p[0]);
                    mi.invoke(o, new Object[] {arg});
                    found = true;
                } catch (Exception e) {
                    throw new RuntimeException("Unable to set " + key + "=" + l
                            + " on " + mi, e);
                }
            }

            // look for field
            Field field = null;
            try {
                field = cl.getField(key);
                if (!Modifier.isPublic(field.getModifiers())) {
                    field = null;
                }
            } catch (Exception e) {
            }
            if (field != null) {
                try {
                    Object arg = cast(l, field.getType());
                    field.set(o, arg);
                    found = true;
                } catch (Exception e) {
                    throw new RuntimeException("Unable to set " + key + "=" + l
                            + " on " + field, e);
                }
            }

            if (!found) {
                if (ret == null) {
                    ret = new LinkedHashSet<String>();
                }
                ret.add(key);
            }
        }
        if (ret == null) {
            return Collections.emptySet();
        }
        return ret;
    }

    private static Object cast(List<String> l, Class type) {
        if (String.class.isAssignableFrom(type)) {
            return l.get(0);
        }
        if (type.isPrimitive()) {
            String value = l.get(0);
            if (type == Boolean.TYPE) {
                return Boolean.valueOf(value);
            } else if (type == Character.TYPE) {
                return Character.valueOf(value.charAt(0));
            } else if (type == Byte.TYPE) {
                return Byte.valueOf(value);
            } else if (type == Short.TYPE) {
                return Short.valueOf(value);
            } else if (type == Integer.TYPE) {
                return Integer.valueOf(value);
            } else if (type == Long.TYPE) {
                return Long.valueOf(value);
            } else if (type == Float.TYPE) {
                return Float.valueOf(value);
            } else if (type == Double.TYPE) {
                return Double.valueOf(value);
            }
        }
        if (Collection.class.isAssignableFrom(type)) {
            return l;
        }
        // RFE support primitive wrappers, e.g. Integer? Boolean?
        // RFE support arrays, e.g. double[]? String[]?
        throw new UnsupportedOperationException("Unknown type " + type
                + " for " + l);
    }

    //
    // toString methods:
    //

    /**
     * @see #toString(String) Same as "{"+toString(null)+"}"
     */
    public String toString() {
        // return "{" + toString(null, null) + "}";
        return super.toString();
    }

    /** @see #toString(String,String) Same as "toString(format, null)" */
    public String toString(String format) {
        return toString(format, null);
    }

    /**
     * Create a string representation of this map using the given format.
     * 
     * <pre>
     * For example, if our map contains:
     *   A=B
     *   X=V0,V1,V2
     * then:
     *   toString("the_$key is the_$value", " * ");
     * would return:
     *   the_A is the_B * the_X is the_V0
     * and:
     *   toString("($key eq $vals)", " +\n");
     * would return:
     *   (A eq B) +
     *   (X eq [V0, V1, V2])
     * and:
     *   toString("$key=$veach", "&amp;");
     * would return a URL-like string:
     *   A=B&amp;X=V0&amp;X=V1&amp;X=V2
     * and:
     *   "{" + toString("$key=$vlist", ", ") + "}";
     * would return the standard {@link Map#toString} format:
     *   {A=[B], X=[V0, V1, V2]}
     * </pre>
     * 
     * The supported variables are:
     * <ul>
     * <li>"$key" is the string key name (e.g. "X")</li>
     * <li>"$value" is the first value (e.g. "V0"), as defined in
     * {@link #getString(String)}</li>
     * <li>"$vals" is the first value if there is only one value (e.g. "B"),
     * otherwise the list of values prefixed with "[" and "]" if there are
     * multiple values (e.g. "[V0, V1, V2]").</li>
     * <li>"$veach" is current value in the list (e.g. "V1")</li>
     * <li>"$vlist" is the "[]" wrapped list (e.g. "[B]" or "[V0, V1, V2]")</li>
     * </ul>
     * 
     * @param format optional format, defaults to "$key=$vlist"
     * @param separator optional separator, defaults to ", "
     * 
     * @return a string with each entry in the given format, where every "$key"
     *         is replaced with the map key and every "$value" is replaced with
     *         the map value.
     */
    public String toString(String format, String separator) {
        String form = format;
        if (form == null) {
            form = "$key=$vlist";
        }

        String sep = separator;
        if (sep == null) {
            sep = ", ";
        }
        if (sep.length() == 0) {
            sep = null;
        }

        Matcher x = PATTERN.matcher(form);

        boolean firstTime = true;
        StringBuffer buf = new StringBuffer();
        for (Map.Entry<String, List<String>> me : entrySet()) {
            String k = me.getKey();
            List<String> l = me.getValue();

            boolean hasEach = false;
            int eachIndex = 0;
            while (true) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    if (sep != null) {
                        buf.append(sep);
                    }
                }

                x.reset();
                while (x.find()) {
                    String tag = x.group(1);
                    String value;
                    if ("veach".equals(tag)) {
                        hasEach = true;
                        value = l.get(eachIndex);
                    } else {
                        value = entryString(k, l, tag);
                    }
                    x.appendReplacement(buf, value);
                }
                x.appendTail(buf);

                if (!hasEach || ++eachIndex >= l.size())
                    break;
            }
        }
        return buf.toString();
    }

    private String entryString(String k, List<String> l, String tag) {
        if ("key".equals(tag)) {
            return k;
        } else if ("value".equals(tag)) {
            return l.get(0);
        } else if ("vals".equals(tag)) {
            if (l.size() == 1) {
                return l.get(0);
            } else {
                return l.toString();
            }
        } else if ("vlist".equals(tag)) {
            return l.toString();
        } else {
            return "InternalError!";
        }
    }

    //
    // Internal parsing:
    //

    /**
     * @return null or a modifiable, non-empty, ordered map of unmodifiable,
     *         non-empty, lists
     */
    private static final Map<String, List<String>> parseMap(Object object) {
        Object o = object;
        if (o == null) {
            return null;
        }
        if (o instanceof Arguments) {
            return ((Arguments) o).m;
        }
        if (o instanceof Map) {
            Map m2 = (Map) o;
            if (m2.isEmpty()) {
                return null;
            }
            Map<String, List<String>> ret = new LinkedHashMap<String, List<String>>();
            for (Object x : m2.entrySet()) {
                Map.Entry me = (Map.Entry) x;
                String key = parseString(me.getKey(), "Map key");
                List<String> value = parseList(me.getValue());
                if (value == null) {
                    continue;
                }
                ret.put(key, value);
            }
            return ret;
        }
        if (o instanceof String) {
            o = ((String) o).split("\\s*,\\s*");
        }
        if (o instanceof Object[]) {
            o = Arrays.asList((Object[]) o);
        }
        if (!(o instanceof Collection)) {
            throw new IllegalArgumentException("Expecting null, Arguments, Map, Object[], or Collection, not "
                    + (o == null ? "null" : o.getClass().getName()));
        }
        Collection c = (Collection) o;
        int n = c.size();
        if (n == 0) {
            return null;
        }
        Map<String, List<String>> ret = null;
        boolean hasMulti = false;
        Iterator iter = c.iterator();
        for (int i = 0; i < n; i++) {
            Object oi = iter.next();
            if (!(oi instanceof String)) {
                throw new IllegalArgumentException("Expecting a Collection of Strings, not "
                        + (oi == null ? "null"
                                     : (oi.getClass().getName() + " " + oi)));
            }
            String s = (String) oi;
            int sep = s.indexOf('=');
            if (sep < 0) {
                throw new IllegalArgumentException("Missing a \"=\" separator for \""
                        + s + "\"");
            }
            String key = s.substring(0, sep).trim();
            String value = s.substring(sep + 1).trim();
            if (key.length() <= 0) {
                throw new IllegalArgumentException("Key length is zero for \""
                        + s + "\"");
            }
            if (ret == null) {
                ret = new LinkedHashMap<String, List<String>>();
            }
            List<String> prev = ret.get(key);
            if (prev == null) {
                ret.put(key, Collections.singletonList(value));
                continue;
            }
            hasMulti = true;
            if (prev.size() == 1) {
                String s0 = prev.get(0);
                prev = new ArrayList<String>(2);
                prev.add(s0);
                ret.put(key, prev);
            }
            prev.add(value);
        }
        if (hasMulti) {
            // make values unmodifiable
            for (Map.Entry<String, List<String>> me : ret.entrySet()) {
                List<String> prev = me.getValue();
                if (prev.size() == 1) {
                    continue;
                }
                prev = Collections.unmodifiableList(prev);
                me.setValue(prev);
            }
        }
        // don't need to make ret unmodifiable
        return ret;
    }

    private static final Set<String> parseSet(Object object) {
        Object o = object;
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            o = ((String) o).split("\\s*,\\s*");
        }
        if (o instanceof Object[]) {
            o = Arrays.asList((Object[]) o);
        }
        if (!(o instanceof Collection)) {
            throw new IllegalArgumentException("Expecting null, String, Object[], or Collection, not "
                    + (o == null ? "null" : o.getClass().getName()));
        }
        Collection c = (Collection) o;
        int n = c.size();
        if (n == 0) {
            return null;
        }
        Set<String> ret = new HashSet<String>(c.size());
        for (Object oi : c) {
            ret.add(parseString(oi, "Set filter value"));
        }
        return ret;
    }

    private static final List<String> parseList(Object object) {
        Object o = object;
        if (o instanceof String) {
            return Collections.singletonList((String) o);
        }
        if (o instanceof Object[]) {
            o = Arrays.asList((Object[]) o);
        }
        if (!(o instanceof Collection)) {
            throw new IllegalArgumentException("Expecting a String, Object[], or Collection value, not "
                    + (o == null ? "null" : (o.getClass().getName()) + " " + o));
        }
        Collection c = (Collection) o;
        int n = c.size();
        if (n == 0) {
            return null;
        }
        Iterator iter = c.iterator();
        if (n == 1) {
            String s = parseString(iter.next(), "Collection value");
            return Collections.singletonList(s);
        }
        List<String> ret = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            String s = parseString(iter.next(), "Collection value");
            ret.add(s);
        }
        return Collections.unmodifiableList(ret);
    }

    private static final String parseString(Object o, String desc) {
        if (!(o instanceof String)) {
            throw new IllegalArgumentException("Expecting a " + desc
                    + " String, not "
                    + (o == null ? "null" : (o.getClass().getName()) + " " + o));
        }
        return (String) o;
    }

    private static final List<String> parsePrefixes(Object o) {
        if (o == null) {
            return Collections.emptyList();
        }
        if (o instanceof String) {
            return Collections.singletonList((String) o);
        }
        if (!(o instanceof Class)) {
            throw new IllegalArgumentException("Expecting null, a String, or a Class, not "
                    + (o == null ? "null" : (o.getClass().getName()) + " " + o));
        }
        List<String> ret = new ArrayList<String>();
        for (Class cl = (Class) o; cl != null; cl = cl.getSuperclass()) {
            ret.add(cl.getName() + ".");
        }
        return ret;
    }

    /**
     * @param m a map created by "parseMap()"
     * @param prefixes a list created by "parsePrefixes()"
     * @param deflt a map created by "parseMap()"
     * @param keys a set created by "parseSet()"
     * @return a non-null, unmodifiable, ordered map
     */
    private static final Map<String, List<String>> parse(Map<String, List<String>> m,
                                                         List<String> prefixes,
                                                         Map<String, List<String>> deflt,
                                                         Set<String> keys) {
        Map<String, List<String>> ret = new LinkedHashMap<String, List<String>>();
        if (m != null && !m.isEmpty()) {
            if (keys == null) {
                ret.putAll(m);
            } else {
                for (String key : keys) {
                    List<String> l = m.get(key);
                    if (l == null) {
                        continue;
                    }
                    ret.put(key, l);
                }
            }
        }
        if ((prefixes != null && !prefixes.isEmpty())
                && (keys == null || (ret.size() < keys.size()))) {
            for (String s : prefixes) {
                Properties props = SystemProperties.getSystemPropertiesWithPrefix(s);
                if (props == null || props.isEmpty()) {
                    continue;
                }
                for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
                    String name = (String) en.nextElement();
                    if (!name.startsWith(s)) {
                        continue;
                    }
                    String key = name.substring(s.length());
                    if (key.length() <= 0) {
                        continue;
                    }
                    if (ret.containsKey(key)) {
                        continue;
                    }
                    if (keys != null && !keys.contains(key)) {
                        continue;
                    }
                    String value = props.getProperty(name);
                    // RFE split by commas, but not if quoted?
                    List<String> l = Collections.singletonList(value);
                    ret.put(key, l);
                }
            }
        }
        if ((deflt != null && !deflt.isEmpty())
                && (keys == null || (ret.size() < keys.size()))) {
            for (Map.Entry<String, List<String>> me : deflt.entrySet()) {
                String key = me.getKey();
                if (ret.containsKey(key)) {
                    continue;
                }
                if (keys != null && !keys.contains(key)) {
                    continue;
                }
                ret.put(key, me.getValue());
            }
        }
        int n = ret.size();
        if (n == 0) {
            return Collections.emptyMap();
        }
        if (n == 1) {
            Map.Entry<String, List<String>> me = ret.entrySet()
                                                    .iterator()
                                                    .next();
            return Collections.singletonMap(me.getKey(), me.getValue());
        }
        if (n <= OPTIMIZE_SIZE) {
            boolean hasMulti = false;
            for (List<String> l : ret.values()) {
                if (l.size() > 1) {
                    hasMulti = true;
                    break;
                }
            }
            if (!hasMulti) {
                return new OptimizedMapImpl(ret);
            }
        }
        return Collections.unmodifiableMap(ret);
    }

    interface OptimizedMap extends Map<String, List<String>> {
        List<String> getStrings(String key, List<String> deflt);

        String getString(String key, String deflt);
    }

    /**
     * Nearly all of our expected uses will have only a handleful of entries,
     * where every value is a single-element List&lt;String&gt;, so we optimize
     * this case.
     * <p>
     * We keep two String arrays; one for the keys, and one for the values.
     * <p>
     * Contrast this with the general case, where we keep an unmodifiableMap
     * wrapper around a LinkedHashMap of String-to-List entries, where the value
     * Lists would be a mix of<br>
     * &nbsp; (1) singletonList wrappers around strings, and<br>
     * &nbsp; (2) unmodifiableList wrappers around ArrayLists of strings.<br>
     */
    private static final class OptimizedMapImpl extends
        AbstractMap<String, List<String>> implements OptimizedMap, Serializable {
        private final String[] keys;
        private final String[] values;

        public OptimizedMapImpl(Map<String, List<String>> m) {
            keys = new String[m.size()];
            values = new String[m.size()];
            int i = 0;
            for (Map.Entry<String, List<String>> me : m.entrySet()) {
                keys[i] = me.getKey();
                values[i] = me.getValue().get(0);
                i++;
            }
        }

        public List<String> getStrings(String key, List<String> deflt) {
            String s = getString(key, null);
            return (s == null ? deflt : Collections.singletonList(s));
        }

        public String getString(String key, String deflt) {
            for (int i = 0; i < keys.length; i++) {
                if (key.equals(keys[i])) {
                    return values[i];
                }
            }
            return deflt;
        }

        // required by our AbstractMap base class:
        public Set<Map.Entry<String, List<String>>> entrySet() {
            return new AbstractSet<Map.Entry<String, List<String>>>() {
                public int size() {
                    return keys.length;
                }

                public Iterator<Map.Entry<String, List<String>>> iterator() {
                    return new Iterator<Map.Entry<String, List<String>>>() {
                        private int i = 0;

                        public boolean hasNext() {
                            return i < keys.length;
                        }

                        public Map.Entry<String, List<String>> next() {
                            if (i >= keys.length) {
                                throw new ArrayIndexOutOfBoundsException(i);
                            }
                            final int j = i++;
                            return new Map.Entry<String, List<String>>() {
                                public String getKey() {
                                    return keys[j];
                                }

                                public List<String> getValue() {
                                    return Collections.singletonList(values[j]);
                                }

                                public List<String> setValue(List<String> value) {
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }

        public boolean containsKey(Object key) {
            for (int i = 0; i < keys.length; i++) {
                if (key.equals(keys[i])) {
                    return true;
                }
            }
            return false;
        }

        public List<String> get(String key) {
            for (int i = 0; i < keys.length; i++) {
                if (key.equals(keys[i])) {
                    return Collections.singletonList(values[i]);
                }
            }
            return null;
        }
    }

    // Support for argument annotation metadata

    // Can't use 'null' in annotation attributes, so use this instead
    public static final String NULL_VALUE = "####null-value";
    
    // This is used to indicate that the field should be left as is
    public static final String NO_VALUE = "###no-value###";

    public static class ParseException extends Exception {
        public ParseException(Field field, String value, Throwable cause) {
            super("Couldn't parse " +value+ " for field " +
        	    field.getName() + ": " +cause.getMessage());
        }
    }

    public static enum BaseDataType {
        FIXED {
            Object parse(Field field, String rawValue) throws ParseException {
                try {
                    return Integer.parseInt(rawValue);
                } catch (NumberFormatException e) {
                    throw new ParseException(field, rawValue, e);
                }
            }
        },
        REAL {
            Object parse(Field field, String rawValue) throws ParseException {
                try {
                    return Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    throw new ParseException(field, rawValue, e);
                }
            }
        },
        STRING {
            Object parse(Field field, String rawValue) throws ParseException {
                return rawValue;
            }
        },
        BOOLEAN {
            Object parse(Field field, String rawValue) {
                return Boolean.parseBoolean(rawValue);
            }
        },
        URI {
            Object parse(Field field, String rawValue) throws ParseException {
                try {
                    return new URI(rawValue);
                } catch (URISyntaxException e) {
                    throw new ParseException(field, rawValue, e);
                }
            }
        };

        abstract Object parse(Field field, String rawValue) throws ParseException;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Spec {

        String name();

        BaseDataType valueType() default BaseDataType.STRING;

        boolean sequence() default false;

        boolean required() default true;

        String defaultValue() default NO_VALUE;

        String description() default "no description";
    }

    public static enum GroupRole {
        MEMBER, OWNER
    }

    public static enum GroupIterationPolicy {
        ROUND_ROBIN, FIRST_UP, CLOSEST, RANDOM;
        
        // Default is to restrict the arguments to the 
        // given members, and then split it.
        // 
        // TODO: Specialize this per policy
        List<Arguments> split(Arguments arguments, Set<String> members) {
            return new Arguments(arguments, null, null, members).split();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Group {
        String name();

        GroupRole role() default GroupRole.MEMBER;

        GroupIterationPolicy policy() default GroupIterationPolicy.FIRST_UP;
    }
    
    // For now, group annotations can be specified in two ways.
    // One is the defined {@link Group} above.  The other is
    // any Annotation class whose name ends in "ArgGroup" and
    // that has the right getter methods.  
    //
    // The purpose of the second form is to improve
    // compile-time validation of group names, since
    // the name string doesn't have to be repeated
    // in every annotation.

    private Set<String> getGroups(Field field) {
        Set<String> groups = null;
        for (Annotation anno : field.getAnnotations()) {
            Class annoClass = anno.annotationType();
            if (anno instanceof Group) {
                Group group = (Group) anno;
                if (group.role() == GroupRole.MEMBER) {
                    if (groups == null) {
                        groups = new HashSet<String>();
                    }
                    groups.add(group.name());
                }
            } else if (annoClass.getName().endsWith("ArgGroup")) {
                try {
                    Class[] parameterTypes = {};
                    Method roleGetter = annoClass.getDeclaredMethod("role",
                                                                    parameterTypes);
                    Method nameGetter = annoClass.getDeclaredMethod("name",
                                                                    parameterTypes);
                    Object[] args = {};
                    GroupRole role = (GroupRole) roleGetter.invoke(anno, args);
                    String name = (String) nameGetter.invoke(anno, args);
                    if (role == GroupRole.MEMBER) {
                        if (groups == null) {
                            groups = new HashSet<String>();
                        }
                        groups.add(name);
                    }
                } catch (Exception e) {
                }
            }
        }
        return groups;
    }

    private Annotation getOwnedGroup(Field field) {
        for (Annotation anno : field.getAnnotations()) {
            Class annoClass = anno.annotationType();
            if (anno instanceof Group) {
                Group group = (Group) anno;
                if (group.role() == GroupRole.OWNER) {
                    return group;
                }
            } else if (annoClass.getName().endsWith("ArgGroup")) {
                try {
                    Class[] parameterTypes = {};
                    Method roleGetter = annoClass.getDeclaredMethod("role", parameterTypes);
                    Object[] args = {};
                    GroupRole role = (GroupRole) roleGetter.invoke(anno, args);
                    if (role == GroupRole.OWNER) {
                        return anno;
                    }
                } catch (Exception e) {
                    // these can safely be ignored
                }
            }
        }
        return null;
    }

    private void setSequenceFieldFromSpec(Field field, Object object, Spec spec) 
            throws ParseException, IllegalAccessException, IllegalStateException {
        String defaultValue = spec.defaultValue();
        String key = spec.name();
        BaseDataType type = spec.valueType();
        boolean isRequired = spec.required();
        List<String> rawValues = null;
        if (containsKey(key)) {
            rawValues = getStrings(key);
        } else if (isRequired) {
            throw new IllegalStateException("Required argument " + key
                    + " was not provided");
        } else if (defaultValue.equals(NULL_VALUE)) {
            field.set(object, null);
            return;
        } else if (defaultValue.equals(NO_VALUE)) {
            return;
        } else {
            // Should be in the form [x,y,z]
            // TODO: Use the existing Arguments code for this, if I can ever
            // find it
            String[] valueArray;
            int end = defaultValue.length() - 1;
            if (defaultValue.charAt(0) == '['
                    && defaultValue.charAt(end) == ']') {
                valueArray = defaultValue.substring(1, end).split(",");
            } else {
                valueArray = defaultValue.split(",");
            }
            rawValues = Arrays.asList(valueArray);
        }
        List<Object> values = new ArrayList<Object>(rawValues.size());
        for (String rawValue : rawValues) {
            values.add(type.parse(field, rawValue));
        }
        field.set(object, Collections.unmodifiableList(values));
    }

    private void setSimpleFieldFromSpec(Field field, Object object, Spec spec) 
            throws ParseException, IllegalAccessException, IllegalStateException {
        String defaultValue = spec.defaultValue();
        String key = spec.name();
        BaseDataType type = spec.valueType();
        boolean isRequired = spec.required();
        String rawValue;
        if (containsKey(key)) {
            List<String> values = getStrings(key);
            rawValue = values.get(0);
        } else if (isRequired) {
            throw new IllegalStateException("Required argument " + key
                    + " was not provided");
        } else {
            rawValue = defaultValue;
        }
        if (rawValue.equals(NO_VALUE)) {
            return;
        }
        Object parsedValue = 
            rawValue.equals(NULL_VALUE) ? null : type.parse(field, rawValue);
	field.set(object, parsedValue);
    }

    private void setFieldFromSpec(Field field, Spec spec, Object object) 
            throws ParseException, IllegalAccessException, IllegalStateException {
        try {
	    if (spec.sequence()) {
	        setSequenceFieldFromSpec(field, object, spec);
	    } else {
	        setSimpleFieldFromSpec(field, object, spec);
	    }
	} catch (IllegalAccessException e) {
	    String exceptionMsg = e.getMessage();
	    String msg = "Couldn't set field " +field.getName()+ 
	    " from argument " +spec.name();
	    if (exceptionMsg != null) {
		msg += ": " +exceptionMsg;
	    }
	    throw new IllegalAccessException(msg);
	} catch (IllegalArgumentException e) {
	    String exceptionMsg = e.getMessage();
	    String msg = "Couldn't set field " +field.getName()+ 
	    " from argument " +spec.name();
	    if (exceptionMsg != null) {
		msg += ": " +exceptionMsg;
	    }
	    throw new IllegalArgumentException(msg);
	}
    }

    private void setGroupOwnerField(Field field,
                                    Object object,
                                    GroupIterationPolicy policy,
                                    Set<String> members) 
            throws ParseException, IllegalAccessException, IllegalStateException {
        List<Arguments> split = policy.split(this, members);
        field.set(object, split);
    }

    /**
     * Set whatever {@link Spec}-annotated fields we have values for.
     */
    public void setFields(Object object) 
            throws ParseException, IllegalAccessException, IllegalStateException {
        for (Field field : object.getClass().getFields()) {
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                // skip finals and statics
                continue;
            } else if (field.isAnnotationPresent(Spec.class)) {
                Spec spec = field.getAnnotation(Spec.class);
                String argName = spec.name();
                if (containsKey(argName)) {
                    setFieldFromSpec(field, spec, object);
                }
            }
        }
    }

    /**
     * Set values of every field that has either a {@link Spec} annotation, or a
     * Group annotation with role OWNER.
     * 
     */
    public void setAllFields(Object object) 
            throws ParseException, IllegalAccessException, IllegalStateException {
        Map<String, Set<String>> groupMembers = new HashMap<String, Set<String>>();
        Map<Annotation, Field> groupFields = new HashMap<Annotation, Field>();
        for (Field field : object.getClass().getFields()) {
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                // skip finals and statics
                continue;
            } else if (field.isAnnotationPresent(Spec.class)) {
                Spec spec = field.getAnnotation(Spec.class);
                setFieldFromSpec(field, spec, object);
                Set<String> groups = getGroups(field);
                if (groups != null) {
                    for (String group : groups) {
                        Set<String> members = groupMembers.get(group);
                        if (members == null) {
                            members = new LinkedHashSet<String>();
                            groupMembers.put(group, members);
                        }
                        members.add(spec.name());
                    }
                }
            } else {
                // Check for group owners
                Annotation anno = getOwnedGroup(field);
                if (anno != null) {
                    groupFields.put(anno, field);
                }
            }
        }
        // Now set up group owners
        for (Map.Entry<Annotation, Field> entry : groupFields.entrySet()) {
            Annotation anno = entry.getKey();
            Field field = entry.getValue();
            Set<String> members = null;
            GroupIterationPolicy policy = null;
            Class annoClass = anno.annotationType();
            if (anno instanceof Group) {
                Group group = (Group) anno;
                members = groupMembers.get(group.name());
                policy = group.policy();
            } else if (annoClass.getName().endsWith("ArgGroup")) {
                try {
                    Class[] parameterTypes = {};
                    Method policyGetter = annoClass.getDeclaredMethod("policy",
                                                                      parameterTypes);
                    Method nameGetter = annoClass.getDeclaredMethod("name",
                                                                    parameterTypes);
                    Object[] args = {};
                    policy = (GroupIterationPolicy) policyGetter.invoke(anno,
                                                                        args);
                    String name = (String) nameGetter.invoke(anno, args);
                    members = groupMembers.get(name);
                } catch (Exception e) {
                }
            }
            if (policy != null && members != null && !members.isEmpty()) {
                setGroupOwnerField(field, object, policy, members);
            }
        }
    }
}
