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
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Param {
        String name();
    
        boolean required() default true;
    
        String defaultValue() default NO_VALUE;
    
        String description() default "no description";
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParamGroup {
        String name();
    
        Cougaar.ParamGroupRole role() default Cougaar.ParamGroupRole.MEMBER;
    
        Cougaar.ParamGroupIterationPolicy policy() default Cougaar.ParamGroupIterationPolicy.FIRST_UP;
    }

    
    // Execution annotations
    
    public static enum BlackboardOp {
        ADD,
        REMOVE,
        MODIFY
    }
    
    public static enum BlackboardEvt {
        BLACKBOARD,
        ALARM,
        TODO
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Execute {                                                     
        BlackboardEvt from() default BlackboardEvt.BLACKBOARD;        
        BlackboardOp[] on();                                 
        String when() default "";  //The name of a predicate method             
        Class<?> isa() default Object.class;   // simple 'instanceof' predicate                                
    }                                                                           

}
