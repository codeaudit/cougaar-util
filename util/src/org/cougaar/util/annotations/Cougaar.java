/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.util.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Set;

import org.cougaar.util.Arguments;

/**
 * Use this class to define COUGAAR-specific annotations.
 * The worker methods for each annotation type should
 * live elsewhere (eg {@link ParameterAnnotations}).
 */
public class Cougaar {
    // Plugin and Component parameter annotatons
    
    /**
     * Can't use 'null' in annotation attributes, so use this instead
     */ 
    public static final String NULL_VALUE = "###null-value###";
    
    /** 
     * This is used to indicate that the field should be left as is
     */
    public static final String NO_VALUE = "###no-value###";
    
    public static enum ParamGroupRole {
        MEMBER, OWNER
    }

    public static enum ParamGroupIterationPolicy {
        ROUND_ROBIN, FIRST_UP, CLOSEST, RANDOM;
    
        // Default is to restrict the arguments to the
        // given members, and then split it.
        // 
        // TODO: Specialize this per policy
        List<Arguments> split(Arguments arguments, Set<String> members) {
            return new Arguments(arguments, null, null, members).split();
        }
    }
    
    /**
     * This annotation should be attached to a public data member
     * to initialize it from an argument element in the society xml.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Param {
        String name();
    
        boolean required() default true;
    
        String defaultValue() default NO_VALUE;
    
        String description() default "no description";
    }
    
    /**
     * This annotation should be attached to a public data member
     * if it belongs to a collection of members that need to be set
     * as a group.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParamGroup {
        String name();
    
        Cougaar.ParamGroupRole role() default Cougaar.ParamGroupRole.MEMBER;
    
        Cougaar.ParamGroupIterationPolicy policy() default Cougaar.ParamGroupIterationPolicy.FIRST_UP;
    }

    /**
     * This annotation should be attached to a public static
     * method of any class that can create an instance given
     * a string.  This resolver method will be used by the
     * argument initialization mechanism for data members whose
     * type isn't one of the presupported ones (ie not an atomic
     * type, or a boxed atomic type, or String, or URI).
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Resolver {
    }
    
    
    // Execution annotations
    
    public static enum BlackboardOp {
        ADD,
        REMOVE,
        CHANGE
    }
    
    private static final class NoClass {
        
    }
    
    public static boolean isNoClass(Class<?> candidate) {
        return candidate == NoClass.class;
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Execute {                                                     
        BlackboardOp[] on();                                 
        String todo() default "";  // name of a specific TODO queue
        String when() default "";  // The name of a predicate method            
        Class<?> isa() default NoClass.class;   // simple 'instanceof' predicate                                
    }                                                                           

}
