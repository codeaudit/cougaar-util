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

import org.cougaar.util.log.*;

import java.util.Map;
import java.util.HashMap;

/** A little state machine framework to allow proper sequencing of a set
 * of logical tasks/states.
 **/

public class StateMachine {
  private static final Logger log = Logging.getLogger(StateMachine.class);

  /** Error state.  The Machine will require a reset in order to progress **/
  public final static State ERROR = new ConstantState("ERROR");
  /** Default state from which you cannot transit anywhere. **/
  public final static State UNINITIALIZED = new ConstantState("UNINITIALIZED");
  /** Machine is fulfilled - will not progress from this state **/
  public final static State DONE = new ConstantState("DONE");

  private State current = UNINITIALIZED;

  private final Map table = new HashMap(11);
  
  public StateMachine() {
    table.put("ERROR", ERROR);
    table.put("UNINITIALIZED", UNINITIALIZED);
    table.put("DONE", DONE);
  }

  /** Invoke a single step to see if we should transit out of the current state.
   * If the machine is "DONE" then no state will be invoked: perpetual no-op unless the machine 
   * is rest.
   * If the machine is in UNINITIALIZED or ERROR state, then an IllegalStateException will be thrown.
   * @return true IFF the current state invoked a transit method.
   */
  public synchronized boolean step() {
    assert current != null;
    if (current == DONE) return false; // cannot return from DONE via step
    if (current == ERROR) throw new IllegalStateException("StateMachine is stuck in Error state");
    if (current == UNINITIALIZED) throw new IllegalStateException("StateMachine is Uninitialized");

    if (progressed) {
      progressed = false;
      try {
        current.invoke();
      } catch (RuntimeException e) {
        throw new RuntimeException("Caught exception in "+this+" "+current, e);
      }
    } else {
      // this is true when a kick is scheduled as pending by the same thread which completes 
      // the transision.  As such, even though it appears disturbing, it isn't actually an
      // error any longer
      if (log.isInfoEnabled()) {
        log.warn(this.toString()+" stalled in "+current);
      }
    }
    return progressed;          // side-effected by set()
  }

  private boolean progressed;

  /** Step until getState() returns DONE **/
  public synchronized void stepUntilDone() {
    while (getState() != DONE) {
      step();
    }
  }

  /** call step repeatedly until the machine doesn't transit **/
  public synchronized void go() {
    while (step());
  }
  
  /** Return the current state **/
  public synchronized final State getState() {
    return current;
  }

  public synchronized final boolean isDone() {
    return current == DONE;
  }

  /** Add a State to the machine.  A state with the same key as an old state will
   * be replaced and the old one returned.
   **/
  public synchronized State add(State s) {
    if ("ERROR".equals(s.key) ||
        "UNINITIALIZED".equals(s.key) ||
        "DONE".equals(s.key)) {
      throw new IllegalStateException("Cannot override constant States "+s);
    }

    s.setMachine(this);
    return (State) table.put(s.key, s);
  }

  /** Add a direct link between two tags **/
  public void addLink(String startTag, final String nextTag) {
    add(new State(startTag) {
        public void invoke() {
          transit(nextTag);
        }
      });
  }

  /** Sets the State of the machine **/
  public synchronized void set(State s) {
    if (s == null) throw new IllegalStateException("Null state");
    if (s == ERROR) throw new IllegalStateException("ERROR state entered");
    if (progressed) {
      /*
      synchronized(System.err) {
        System.err.println("Ack!  Already progressed!");
        (new Throwable()).printStackTrace();
      }
      */
      throw new IllegalStateException("Already progressed");
    }
    current = s;
    progressed = true;
  }

  protected synchronized final void transit(State s) {
    transit(getState(), s);
  }
  protected synchronized final void transit(String k) {
    transit(getState(), decodeKey(k));
  }

  /** Transit from oldState to newState.  Default implementation
   * merely calls set(newState)
   **/
  protected synchronized void transit(State oldState, State newState) {
    set(newState);
  }

  public final synchronized void set(String k) {
    set(decodeKey(k));
  }
   
  private synchronized State decodeKey(String k) {
    State s = (State)table.get(k);
    if (s == null)
      throw new IllegalStateException("Key \""+k+"\" doesn't name a State in StateMachine "+this);
    return s;
  }

  /**
   * Each state in a machine extends this abstract class 
   **/
  public static abstract class State {
    private StateMachine machine = null;
    public final String key;
    protected State(String key) { this.key = key; }

    private synchronized void setMachine(StateMachine m) {
      if (machine != null)
        throw new IllegalStateException("State "+this+" already attached to a StateMachine "+machine);
      machine = m;
    }

    public final String getKey() { return key; }

    // protected so that StackMachine (for instance) can cast the machine
    protected final synchronized StateMachine getMachine() {
      return machine;
    }
    protected final void transit(String key) {
      synchronized (getMachine()) {
        getMachine().transit(key);
      }
    }
    protected final void transit(State state) {
      synchronized (getMachine()) {
        getMachine().transit(state);
      }
    }

    /** Whenever the StateMachine is invoked, so is the current State **/
    public abstract void invoke();

    public String toString() {
      return "State "+key;
    }
  }

  /** 
   * A State for StateMachine constants like ERROR
   */
  private static class ConstantState extends State {
    private ConstantState(String key) { super(key); }
    public void invoke() {
      throw new IllegalStateException("ConstantState \""+key+"\" must never be invoked."); 
    }
  }

  /**
   * Utility Exception for use by the StateMachine
   **/
  public static class IllegalStateException extends RuntimeException {
    public IllegalStateException(String s) { super(s); }
  }

  //
  // example
  //
  
  public static void main(String[] args) {
    StateMachine sm = new StateMachine() {
        public void transit(State s0, State s1) {
          System.err.println("transiting from "+s0+" to "+s1);
          super.transit(s0, s1);
        }
      };

            
    sm.add(new State("A") { public void invoke() { transit("B"); }});
    sm.add(new State("B") { public void invoke() { transit("C"); }});
    sm.add(new State("C") { public void invoke() { transit("D"); }});
    sm.add(new State("D") { public void invoke() { transit("DONE"); }});

    sm.set("A");
   
    while (sm.getState() != DONE) {
      System.out.println("next");
      sm.step();
    }

    sm.set("A");
    System.out.println("stepUntilDone():");
    sm.stepUntilDone();

    sm.set("A");
    System.out.println("go():");
    // replace B with a state which requires several invokes
    sm.add(new State("B") {
        private int count = 0;
        public void invoke() { 
          count++;
          if (count >= 3) transit("C"); 
        }});

    while (sm.getState() != DONE) {
      System.out.println("next");
      sm.go();
    }
    
  }

}
