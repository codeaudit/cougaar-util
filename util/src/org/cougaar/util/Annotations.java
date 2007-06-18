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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides support for field metatdata
 * in plugins and components via annotations.
 * 
 */
public class Annotations {
    private static final String ARG_GROUP_SUFFIX = "ArgGroup";

    // Can't use 'null' in annotation attributes, so use this instead
    public static final String NULL_VALUE = "###null-value###";

    // This is used to indicate that the field should be left as is
    public static final String NO_VALUE = "###no-value###";
    
    private final Arguments args;
    
    public Annotations(Arguments args) {
        this.args = args;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Spec {
        String name();
        boolean required() default true;
        String defaultValue() default NO_VALUE;
        String description() default "no description";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Group {
        String name();
        GroupRole role() default GroupRole.MEMBER;
        GroupIterationPolicy policy() default GroupIterationPolicy.FIRST_UP;
    }
    
    private String getArgGroupName(Class annotationClass) {
	// remove 'ArgGroup' from the end of the simplename
	String name = annotationClass.getSimpleName();
	return name.substring(0, name.length()-ARG_GROUP_SUFFIX.length());
    }

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
            } else if (annoClass.getName().endsWith(ARG_GROUP_SUFFIX)) {
                try {
                    Class[] parameterTypes = {};
                    Method roleGetter = annoClass.getDeclaredMethod("role",
                                                                    parameterTypes);
                    Object[] args = {};
                    GroupRole role = (GroupRole) roleGetter.invoke(anno, args);
                    String name = getArgGroupName(annoClass);
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
            } else if (annoClass.getName().endsWith(ARG_GROUP_SUFFIX)) {
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

    private void setSequenceFieldFromSpec(Field field, 
                                          Object object, 
                                          Spec spec) 
    throws ParseException, IllegalAccessException, IllegalStateException {
        String defaultValue = spec.defaultValue();
        String key = spec.name();
        DataType type = DataType.fromField(field);
        boolean isRequired = spec.required();
        List<String> rawValues = null;
        if (args.containsKey(key)) {
            rawValues = args.getStrings(key);
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

    private void setSimpleFieldFromSpec(Field field,
                                        Object object,
                                        Spec spec) 
    throws ParseException, IllegalAccessException, IllegalStateException {
        String defaultValue = spec.defaultValue();
        String key = spec.name();
        DataType type = DataType.fromField(field);
        boolean isRequired = spec.required();
        String rawValue;
        if (args.containsKey(key)) {
            List<String> values = args.getStrings(key);
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

    private void setFieldFromSpec(Field field, 
                                  Spec spec, 
                                  Object object) 
    throws ParseException, IllegalAccessException, IllegalStateException {
        try {
            Class valueType = field.getType();
            boolean isSequence = List.class.isAssignableFrom(valueType);
            if (isSequence) {
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
        List<Arguments> split = policy.split(args, members);
        field.set(object, split);
    }

    /**
     * Set whatever {@link Spec}-annotated fields we have values
     * for.
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
                if (args.containsKey(argName)) {
                    setFieldFromSpec(field, spec, object);
                }
            }
        }
    }

    /**
     * Set values of every field that has either a {@link Spec}
     * annotation, or a Group annotation with role OWNER.
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
            } else if (annoClass.getName().endsWith(ARG_GROUP_SUFFIX)) {
                try {
                    Class[] parameterTypes = {};
                    Method policyGetter = annoClass.getDeclaredMethod("policy",
                                                                      parameterTypes);
                    Object[] methodArgs = {};
                    policy = (GroupIterationPolicy) 
                        policyGetter.invoke(anno, methodArgs);
                    String name = getArgGroupName(annoClass);
                    members = groupMembers.get(name);
                } catch (Exception e) {
                }
            }
            if (policy != null && members != null && !members.isEmpty()) {
                setGroupOwnerField(field, object, policy, members);
            }
        }
    }

	public static class ParseException extends Exception {
	    public ParseException(Field field, String value, Throwable cause) {
		super("Couldn't parse " +value+ " for field " +
			field.getName() + ": " +cause.getMessage());
	    }
	}

	public static enum DataType {
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
	    
	    static Class elementType(Field field, Class valueType) {
		Type gtype = field.getGenericType();
		if (gtype == null) {
		    throw new IllegalStateException("Can't handle unqualified List types");
		}
		if (!(gtype instanceof ParameterizedType)) {
		    throw new IllegalStateException("Can't handle fields of type "
			    + valueType);
		}
		Type etype = ((ParameterizedType) gtype).getActualTypeArguments()[0];
		if (!(etype instanceof Class)) {
		    throw new IllegalStateException("Can't handle Lists of type " 
			    + etype);
		}
		return (Class) etype;
	    }
	    
	    static DataType fromField(Field field) {
		Class valueType = field.getType();
		if (List.class.isAssignableFrom(valueType)) {
		    valueType = elementType(field, valueType);
		}
		if (valueType == int.class || valueType == Integer.class) {
		    return FIXED;
		} else if (valueType == float.class || valueType == double.class ||
			valueType == Float.class || valueType == Double.class) {
		    return REAL;
		} else if (valueType == boolean.class || valueType == Boolean.class) {
		    return BOOLEAN;
		} else if (valueType == String.class) {
		    return STRING;
		} else if (URI.class.isAssignableFrom(valueType)) {
		    return URI;
		} else {
		    throw new IllegalStateException("Can't handle fields of type " 
			    + valueType);
		}
	    }
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
    }
