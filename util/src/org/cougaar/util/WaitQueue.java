/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.util;

import java.util.HashMap;

/** Implement an asynchronous wait/notify map with value pass-through, selected by an opaque key.
 **/
class WaitQueue {
  private long counter = 0L;
  private HashMap selects = new HashMap(11); // assume not too many at a time

  /** Construct a key to be used in this wait queue **/
  Object getKey() {
    Object key;
    synchronized (selects) {
      key = new Long(counter++);
      selects.put(key, new Waiter());
    }
    return key;
  }

  /** Activate any thread(s) which are waiting for a response from the key **/
  void trigger(Object key, Object value) {
    Waiter waiter = null;
    synchronized (selects) {
      waiter = (Waiter) selects.get(key);
      if (waiter == null) return; // bail if no waiter
    }
    waiter.trigger(value);
  }

  /** Block the current thread until there is a triggerKey has been called
   * on the referenced key.
   * Returns immediately if the key is unknown or has previously been triggered.
   * @throw InterruptedException if the wait is interrupted.  In this case,
   * the trigger is <em>not</em> cleared.
   * @param key The key as returned by #getKey()
   * @param timeout How long to wait or 0 (forever)
   **/
  Object waitFor(Object key, long timeout) throws InterruptedException {
    Waiter waiter = null;
    synchronized (selects) {
      waiter = (Waiter) selects.get(key);
      if (waiter == null) return null; // bail if no waiter
    }

    // non-null waiter: pass control to it
    Object value = waiter.waitFor(timeout);
    
    // when done, clear it
    synchronized (selects) {
      selects.remove(key);
    }

    return value;
  }

  /** alias for waitFor(key,0L);
   **/
  Object waitFor(Object key) throws InterruptedException {
    return waitFor(key,0L);
  }

  /** Return true IFF waitFor(key) call would have blocked for some amount of time **/
  boolean wouldBlock(Object key) {
    Waiter waiter = null;
    synchronized (selects) {
      waiter = (Waiter) selects.get(key);
      if (waiter == null) return false;
    }

    // non-null waiter: pass control to it
    return waiter.wouldBlock();
  }

  private static class Waiter {
    private boolean trigger = false;
    private Object value = null;

    synchronized Object waitFor(long timeout) throws InterruptedException{
      if (trigger) return value;
      this.wait(timeout);
      return value;
    }

    synchronized void trigger(Object value) {
      trigger = true;
      this.value = value;
      this.notify();
    }

    synchronized boolean wouldBlock() {
      return (!trigger);
    }
  }

  /*
  public static void main(String [] arg) {
    ArrayList keys = new ArrayList();
    final WaitQueue wq = new WaitQueue();

    for (int i = 0; i<10; i++) {
      final Object k = wq.getKey();
      final Object r = new Integer(i);
      new Thread(new Runnable() {
          public void run() {
            try {
              Thread.sleep(30*1000);
            } catch(Exception e) {
              e.printStackTrace();
            }
            wq.trigger(k, r);
          }
        }
                 ).start();
      keys.add(k);
      
      try {
        Thread.sleep(500);
      } catch(Exception e) {
        e.printStackTrace();
      }

    }
    
    for (int i=0;i<10; i++) {
      try {
        Object v = wq.waitFor(keys.get(i));
        System.out.println("result "+i+" = "+v);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  */
}
