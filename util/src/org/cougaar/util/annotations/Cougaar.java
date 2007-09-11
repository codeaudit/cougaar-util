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

    // Obtaining services, TBD
    public @interface Service {
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
