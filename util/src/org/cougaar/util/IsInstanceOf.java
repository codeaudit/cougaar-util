/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.util;

/**                                                                         
 * General purpose dynamic 'instanceof' predicate.                                   
 */
public class IsInstanceOf implements UnaryPredicate {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final Class<?> match;

    public IsInstanceOf(Class<?> match) {
        this.match = match;
    }

    public boolean execute(Object arg) {
        return match.isAssignableFrom(arg.getClass());
    }
}

