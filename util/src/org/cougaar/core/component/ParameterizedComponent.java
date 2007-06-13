/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
package org.cougaar.core.component;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cougaar.util.Arguments;
import org.cougaar.util.GenericStateModelAdapter;

/**
 * Simple Component base class that provides named paramater support.
 * Parameters should take the form <var>=<value>.
 */
abstract public class ParameterizedComponent
        extends GenericStateModelAdapter
        implements Component {
    
    public static class ParseException extends Exception {
	public ParseException(Throwable cause) {
	    super(cause);
	}
    }
    
    public static enum BaseDataType {
	FIXED {
	    Object parse(String rawValue) throws ParseException {
		try {
		    return Integer.parseInt(rawValue);
		} catch (NumberFormatException e) {
		    throw new ParseException(e);
		}
	    }
	},
	REAL {
	    Object parse(String rawValue) throws ParseException {
		try {
		    return Double.parseDouble(rawValue);
		} catch (NumberFormatException e) {
		    throw new ParseException(e);
		}
	    }
	},
	STRING {
	    Object parse(String rawValue) throws ParseException {
		return rawValue;
	    }
	},
	BOOLEAN {
	    Object parse(String rawValue) {
		return Boolean.parseBoolean(rawValue);
	    }
	},
	URI {
	    Object parse(String rawValue) throws ParseException {
		try {
		    return new URI(rawValue);
		} catch (URISyntaxException e) {
		    throw new ParseException(e);
		}
	    }
	};
	
	abstract Object parse(String rawValue) throws ParseException;
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ArgSpec {
        String name();
        BaseDataType valueType();
	boolean sequence() default false;
	boolean required() default true;
        String defaultValue() default "";
        String description();
    }
    
    
    public static enum ArgGroupRole {
        MEMBER,
        OWNER
    }
    
    public static enum ArgGroupIterationPolicy {
        ROUND_ROBIN,
        FIRST_UP,
        CLOSEST,
        RANDOM
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ArgGroup {
        String name();
        ArgGroupRole role() default ArgGroupRole.MEMBER;
        ArgGroupIterationPolicy policy() default ArgGroupIterationPolicy.FIRST_UP;
    }
   
    
    protected Arguments args;
    
    private boolean isGroupMember(Field field, String groupName) {
	for (Annotation anno : field.getAnnotations()) {
	    Class annoClass = anno.annotationType();
	    if (anno instanceof ArgGroup) {
		ArgGroup group = (ArgGroup) anno;
		if (group.role() == ArgGroupRole.MEMBER && group.name().equals(groupName)) {
		    return true;
		}
	    } else if (annoClass.getName().endsWith("ArgGroup")) {
		Class[] parameterTypes = {};
		Object[] args = {};
		try {
		    Method roleGetter = annoClass.getDeclaredMethod("role", parameterTypes);
		    Method nameGetter = annoClass.getDeclaredMethod("name", parameterTypes);
		    ArgGroupRole role = (ArgGroupRole) roleGetter.invoke(anno, args);
		    String name = (String) nameGetter.invoke(anno, args);
		    if (role == ArgGroupRole.MEMBER && name.equals(groupName)) {
			return true;
		    }
		} catch (Exception e) {
		    return false;
		}		
	    }
	}
	return false;
    }
    
    private void setFieldFromSpec(Field field, Arguments arguments) {
	ArgSpec spec = field.getAnnotation(ArgSpec.class);
	String defaultValue = spec.defaultValue();
	String key = spec.name();
	BaseDataType type = spec.valueType();
	boolean isSequence = spec.sequence();
	boolean isRequired = spec.required();
	String rawValue;
	if (arguments.containsKey(key)) {
	    rawValue = arguments.getString(key);
	} else if (isRequired) {
	    System.err.println("Required argument " +key+ " was not provided");
	    return;
	} else {
	    rawValue = defaultValue;
	}
	Object value;
	try {
	    if (isSequence) {
		StringTokenizer tk = new StringTokenizer(rawValue, ",");
		List<Object> values = new ArrayList<Object>(tk.countTokens());
		while (tk.hasMoreTokens()) {
		    values.add(type.parse(tk.nextToken()));
		}
		value = values;
	    } else {
		value = type.parse(rawValue);
	    }
	    field.set(this, value);
	} catch (ParseException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}
    }

    
    /**
     * Set values of all fields that have ArgSpecs.
     *
     */
    public void setAllFields() {
	for (Field field : getClass().getFields()) {
            if (field.isAnnotationPresent(ArgSpec.class)) {
        	setFieldFromSpec(field, args);
            }
        }
    }
    
    /**
     * Set values of all fields in the given group,
     * using the 'split' values in the given
     * arguments.
     *
     */
    public void setGroupFields(String groupName, Arguments arguments) {
	for (Field field : getClass().getFields()) {
            if (field.isAnnotationPresent(ArgSpec.class) && isGroupMember(field, groupName)) {
        	setFieldFromSpec(field, arguments);
            }
        }
    }
    
    public void setArguments(Arguments args) {
      this.args = args;
    }

    /** @see Arguments#getString(String) */
    protected String getParameter(String key) {
	return args.getString(key);
    }
    /** @see Arguments#getString(String,String) */
    protected String getParameter(String key, String defaultValue) {
        return args.getString(key, defaultValue);
    }
    /** @see Arguments#getLong(String,long) */
    public long getParameter(String key, long defaultValue) {
        return args.getLong(key, defaultValue);
    }
    /** @see Arguments#getDouble(String,double) */
    public double getParameter(String key, double defaultValue) {
        return args.getDouble(key, defaultValue);
    }
    /** @see Arguments#getStrings(String) */
    public List getParameterValues(String key) {
        return args.getStrings(key);
    }
    /** @see Arguments */
    public Map getParameterMap() {
        return args;
    }
}
