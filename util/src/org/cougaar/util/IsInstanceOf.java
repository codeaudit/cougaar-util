/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.util;

/**                                                                         
 * General purpose dynamic 'instanceof' predicate.                                   
 */
public class IsInstanceOf<T> implements UnaryPredicate<T> {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final Class<T> match;

    public IsInstanceOf(Class<T> match) {
        this.match = match;
    }

    public boolean execute(Object arg) {
        return match.isAssignableFrom(arg.getClass());
    }
}

