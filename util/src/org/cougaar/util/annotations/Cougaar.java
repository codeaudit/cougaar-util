/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.util.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.LinkedList;

import org.cougaar.util.GenericStateModelAdapter;

/**
 * Use this class to define COUGAAR-specific annotations. The worker methods for
 * each annotation type should live elsewhere (eg {@link Argument}).
 */
public class Cougaar {
    // Plugin and Component argument annotatons

    /**
     * Can't use 'null' in annotation attributes, so use this instead
     */
    public static final String NULL_VALUE = "###null-value###";

    /**
     * This is used to indicate that the field should be left as is
     */
    public static final String NO_VALUE = "###no-value###";
    
    @SuppressWarnings("unchecked") // unavoidable warnings
    private static <T extends AccessibleObject&Member>
    T[] getMembers(Class<?> targetClass, Class<T> memberClass) {
        if (memberClass.equals(Field.class)) {
          try {
            return (T[]) targetClass.getDeclaredFields();
          } catch (AccessControlException ace) {
            // inside applet? return public fields, TODO remove inherited fields
            return (T[]) targetClass.getFields();
          }
        } else if (memberClass.equals(Method.class)) {
          try {
            return (T[]) targetClass.getDeclaredMethods();
          } catch (AccessControlException ace) {
            // same as above applet case
            return (T[]) targetClass.getMethods();
          }
        } else {
            throw new IllegalArgumentException(memberClass + " is not Field or Method");
        }
    }
    
    private static boolean signaturesMatch(Method method, Method other) {
        if (!method.getName().equals(other.getName())) {
            return false;
        }
        if (method.getReturnType() != other.getReturnType()) {
            return false;
        }
        Class<?>[] params1 = method.getParameterTypes();
        Class<?>[] params2 = other.getParameterTypes();
        if (params1.length != params2.length) {
            return false;
        }
        for (int i = 0; i < params1.length; i++) {
            if (params1[i] != params2[i])
                return false;
        }
        return true;
    }
    
    /**
     * Collect all Members in a given class or any of its super classes 
     * which have any of a given set of annotations. Mark each such Member
     * as accessible.
     * 
     * @param targetClass the first class in which to look for annotated Members
     * 
     * @param endClass the class at which to stop the super walk
     * 
     * @param annotationClasses which annotations to look for
     * 
     * @param members the members so far.  The initial invoker should supply an empty list
     */
    public static <T extends AccessibleObject&Member> 
    void getAnnotatedMembers(Class<?> targetClass, 
                             Class<?> endClass,
                             Collection<Class<? extends Annotation>> annotationClasses,
                             Class<T> memberClass,
                             Collection<T> members) {
        T[] declaredMembers = getMembers(targetClass, memberClass);
       
        for (T member : declaredMembers) {
            int mod = member.getModifiers();
            if ((Modifier.isFinal(mod) && memberClass.equals(Field.class)) 
                    || Modifier.isStatic(mod)) {
                // skip final Fields and static Members
                continue;
            } else {
                for (Class<? extends Annotation> annotationClass : annotationClasses) {
                    if (member.isAnnotationPresent(annotationClass)) {
                        if (memberClass.equals(Method.class)) {
                            // Must check manually to see if an override is already in the list!
                            // TODO: Make this more efficient.
                            boolean isOverridden = false;
                            Method method = (Method) member;
                            for (Member extant : members) {
                                Method extantMethod = (Method) extant;
                                if (signaturesMatch(extantMethod, method)) {
                                    isOverridden = true;
                                    break;
                                }
                            }
                            if (isOverridden) {
                                continue;
                            }
                        }
                        members.add(member);
                        if (!member.isAccessible()) {
                            try {
                                member.setAccessible(true);
                            } catch (SecurityException e) {
                                String msg = member.getName() + " has annotation " 
                                        + annotationClass + " but is not accessible";
                                throw new IllegalStateException(msg);
                            }
                        }
                        break;
                    }
                }
            }
        }
        Class<?> superClass = targetClass.getSuperclass();
        if (superClass != null && superClass != endClass) {
            getAnnotatedMembers(superClass, endClass, annotationClasses, memberClass, members);
        }
    }
    
    /**
     * Collect all fields in a given class or any of its super classes 
     * which have any of a given set of annotations. Fields can have
     * any visibility; each will be tagged as accessible.
     * 
     * @param targetClass the first class in which to look for annotated fields
     * 
     * @param endClass the class at which to stop the super walk
     * 
     * @param annotationClasses which annotations to look for
     * 
     */
    public static <T extends AccessibleObject&Member>
    Collection<T> getAnnotatedMembers(Class<?> targetClass, 
                                      Class<?> endClass,
                                      Collection<Class<? extends Annotation>> annotationClasses,
                                      Class<T> memberClass) {
        Collection<T> members = new LinkedList<T>();
        getAnnotatedMembers(targetClass, endClass, annotationClasses, memberClass, members);
        return members;
    }
    
    /**
     * Collect all fields in a given class or any of its super classes 
     * which have a given annotation. Fields can have
     * any visibility; each will be tagged as accessible.
     * 
     * @param targetClass the first class in which to look for annotated fields
     * 
     * @param endClass the class at which to stop the super walk
     * 
     * @param annotationClass which annotation to look for
     * 
     */
    public static <T extends AccessibleObject&Member>
    Collection<T> getAnnotatedMembers(Class<?> targetClass, 
                                      Class<?> endClass,
                                      Class<? extends Annotation> annotationClass,
                                      Class<T> memberClass) {
        Collection<T> members = new LinkedList<T>();
        Collection<Class<? extends Annotation>> annotationClasses = 
            new LinkedList<Class<? extends Annotation>>();
        annotationClasses.add(annotationClass);
        getAnnotatedMembers(targetClass, endClass, annotationClasses, memberClass, members);
        return members;
    }
    
    public static Collection<Field> getAnnotatedFields(Class<?> targetClass, 
                                                       Class<? extends Annotation> annotationClass) {
        Class<?> endClass;
        // logical end-class is the super of ParameterizedPlugin, but since that's in core we
        // don't have compile-time access here.
        try {
            endClass = Class.forName("org.cougaar.core.plugin.ParameterizedPlugin").getSuperclass();
        } catch (ClassNotFoundException e) {
            endClass = GenericStateModelAdapter.class;
        }
        return getAnnotatedMembers(targetClass, endClass, annotationClass, Field.class);
    }
    
    public static Collection<Method> getAnnotatedMethods(Class<?> targetClass, 
                                                         Class<? extends Annotation> annotationClass) {
        Class<?> endClass;
        // logical end-class is the super of ParameterizedPlugin, but since that's in core we
        // don't have compile-time access here.
        try {
            endClass = Class.forName("org.cougaar.core.plugin.ParameterizedPlugin").getSuperclass();
        } catch (ClassNotFoundException e) {
            endClass = GenericStateModelAdapter.class;
        }
        return getAnnotatedMembers(targetClass, endClass, annotationClass, Method.class);
    }

    /**
     * This annotation should be attached to a public data member to initialize
     * it from an argument element in the society xml.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Arg {
        String name();

        boolean required() default true;

        String defaultValue() default NO_VALUE;

        String description() default "no description";
    }

    /**
     * This annotation should be attached to a public data member if it belongs
     * to a collection of members that need to be set as a group.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ArgGroup {
        String name();

        Argument.GroupRole role() default Argument.GroupRole.MEMBER;

        Argument.GroupIterationPolicy policy() default Argument.GroupIterationPolicy.FIRST_UP;
    }

    /**
     * This annotation should be attached to a public static method of any class
     * that can create an instance given a string. This resolver method will be
     * used by the argument initialization mechanism for data members whose type
     * isn't one of the presupported ones (ie not an atomic type, or a boxed
     * atomic type, or String, or URI, or an Enum).
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Resolver {
    }

   /**
    * This annotation should be attached to public fields whose value should be
    * assigned to the registered service of the field's type.  The field type
    * <em>must</em> implement the interface {@link org.cougaar.core.component.Service}.
    */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ObtainService {
    }

    // Execution annotations

    /**
     * Attaching this kind annotation to a public method will create an
     * IncrementalSubscription through the blackboard service, and cause the
     * annotated method to be invoked for each item in each of the collections
     * of that subscription mentioned in in {@link #on} clause. These
     * invocations will happen in the plugin's execute thread, in a
     * blackboard transaction.
     * 
     * By default, the method will be invoked on objects whose class matches the
     * class of the method's single parameter (it must take only one). In other
     * words, the class of the parameter implicitly filters the subscription to
     * items of that type. For further filtering, a value for the
     * {@link #when} clause can be provided. This value should be name of a
     * predicate method, ie, a public method on the same class that returns a
     * boolean and takes a single parameter whose class matches that of the
     * parameter of the annotated method.
     * 
     * For now, predicate methods can also be defined using the
     * {@link Predicate} annotation. In this case, the {@link #when} clause
     * should match the the {@link Predicate#when}. But Predicate annotations
     * are deprecated and might well be dropped; their use is not
     * recommended.
     * 
     * The return value of methods with an Execute annotation is ignored, so a
     * 'void' return type is almost always the right choice.
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Execute {
        /**
         * @return which subscription modification types are relevant
         */
        Subscribe.ModType[] on();

        /**
         * @return the name of a predicate method, or a name that matches some
         *         {@link Predicate#when} on the same class}
         */
        String when() default NO_VALUE;
    }

    /**
     * Attaching this kind of annotation to a public method causes it to be used
     * as a filter for an {@link Execute} method with a matching {@link Execute#when}.
     * 
     * A predicate method must be a public, must return a boolean and must take
     * exactly one argument. Further, the type of the argument should match that
     * of the one argument of the corresonding {@link Execute}.
     * 
     * This annotation is deprecated.  Instead of annotating a method this way, 
     * the preferred approach is simply to make the name a predicate method 
     * match the value of the {@link Execute#when} clause.
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Deprecated
    public @interface Predicate {
        String when() default NO_VALUE;
    }
}
