/* 
 * <copyright>
 *  Copyright 2012 BBN Technologies
 * </copyright>
 */
package org.cougaar.core.component;

/**
 * Hook to start subscriptions as a pseudo life-cycle event
 */
public interface SubscriptionLifeCycle {
   public void startSubscriptions();
}
