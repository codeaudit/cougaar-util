/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.util.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


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
    
    /**
     * This class is used only as the default value for {@link Execute#isa()}.
     * Its presence indicates that 'isa' hasn't been set by the user.
     */
    public static final class NoClass {
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
     * atomic type, or String, or URI).
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Resolver {
    }
    
    
    // Obtaining services, TBD
    public @interface Service {
    }
    
    // Execution annotations
    
    /**
     * Attaching this kind of annotation to a public field will create a
     * TodoSubscription and set that field to that subscription. The elements on
     * the TodoSubscription will be TodoItems and will be run in the plugin's
     * execute context (ie, in blackboard transaction). New items can be added
     * in any context.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Todo {
        String id();
    }

    /**
     * Attaching this kind annotation to a public method will cause that method
     * to be invoked once per item per {@link Subscribe.ModType} collection, for
     * a given IncrementalSubscription. These invocations will happen in the
     * plugin's execute context (ie, in blackboard transaction). These methods
     * must be a public and should take exactly one parameter. The return value
     * is ignored, so a 'void' return type is almost always the right choice.
     * 
     * By default, the method will be invoked on objects whose class matches the
     * class of the method's parameter.
     * 
     * If a {@link #when} is specified, the plugin should also define a method
     * with the same signature that returns a boolean, and that has a
     * {@link Predicate} with a matching {@link Predicate#when}. This predicate
     * method is used to further filter the set of items that will be passed to
     * the execute method.
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Execute {
        /**
         * @return which subscription modification types are relevant
         */
        Subscribe.ModType[] on();

        /**
         * @return the name of a {@link Predicate}
         */
        String when() default NO_VALUE; 
    }

    /**
     * Attaching this kind of annotation to a public method causes it to
     * be used as a filter for an {@link Execute} method with a matching
     * {@link Execute#when}.
     * 
     * A predicate method must be a public, must return a boolean and
     * must take exactly one argument.  Further, the type of the argument
     * should match that of the one argument of the corresonding 
     * {@link Execute}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Predicate {
        String when() default NO_VALUE;
    }
}
