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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;


/** An extension of StackMachine which supports subroutines/functions
 *
 **/

public class StackMachine extends StateMachine {
  public StackMachine() {
    super();

    // psuedo-state for stack support
    add(new SState("POP") {
        public void invoke() {
          Frame f = popFrame();
          setVar("RESULT", f.getRetval());
          transit(f.getReturnTag());
        }});

    add(new SState("ITERATE") {
        public void invoke() {
          Object[] args = (Object[]) getArgument();
          Collection values = (Collection)args[0];
          String subTag = (String)args[1];
          setVar("it", values.iterator());
          setVar("subTag", subTag);
          transit("ITERATE1");
        }});
    add(new SState("ITERATE1") {
        public void invoke() {
          Iterator it = (Iterator) getVar("it");
          if (it.hasNext()) {
            Object o = it.next();
            String subTag = (String) getVar("subTag");
            call(subTag, o, "ITERATE1");
          } else {
            callReturn(null);
          }
        }});

    // initialize the stack (no argument, transit to ERROR if returned)
    pushFrame(new Frame("ERROR", null));
  }
    

  public static class Frame {
    // next tag to go
    private final String returnTag;
    public String getReturnTag() {return returnTag;}

    // argument object
    private final Object argument;
    public Object getArgument() { return argument; }

    private Object retval = null;
    public Object getRetval() { return retval; }
    public void setRetval(Object o) { retval = o; }

    // frame variables
    private final Map vars = new HashMap(5);
    public Object getVar(Object var) { return vars.get(var); }
    public void setVar(Object var, Object val) { vars.put(var, val); }
    public void unsetVar(Object var) { vars.remove(var); }
    public boolean varBound(Object var) { return vars.containsKey(var); }

    public Frame(String rtag, Object arg) { returnTag = rtag; argument=arg; }
  }

  private final Stack stack = new Stack();

  protected synchronized int stackSize() { return stack.size(); }

  protected synchronized Frame popFrame() {
    return (Frame) stack.pop();
  }

  protected synchronized void pushFrame(Frame f) {
    stack.push(f);
  }

  /** get the current Frame object **/
  protected synchronized Frame getFrame() {
    Frame f = (Frame) stack.peek();
    if (f == null) {
      throw new IllegalStateException("No Stack Frame in current State");
    }
    return f;
  }
  /** get the frame which is N elements above the current one.
   * getFrame(0) == getFrame();
   **/
  protected synchronized Frame getFrame(int n) {
    return (Frame) stack.elementAt((stack.size()-1)-n);
  }
    
  /** dynamic binding variable search **/
  public synchronized Object searchStack(Object var) {
    for (int l=stack.size()-1, i=l; i>=0; i--) {
      Frame f = (Frame) stack.elementAt(i);
      if (f.varBound(var)) {
        return f.getVar(var);
      }
    }
    return null;
  }

  /** State object which adds accessors to Stack manipulation 
   * methods.
   **/

  public static abstract class SState extends State {
    protected SState(String s) { super(s); }

    protected StackMachine getSMachine() {
      return (StackMachine) getMachine();
    }

    protected Frame getFrame() {
      return getSMachine().getFrame();
    }
    protected Frame getFrame(int n) {
      return getSMachine().getFrame(n);
    }

    /** get the argument of the current Frame **/
    protected Object getArgument() {
      return getFrame().getArgument();
    }

    /** Set the return value of the current Frame - this will
     * become the value returned by getReturned() once
     * the stack has popped.
     * @note this is used on the subroutine side.
     **/
    protected void setResult(Object r) {
      getFrame().setRetval(r);
    }

    /** alias for getVar("RESULT"); 
     * @note this is used on the caller side.
     **/
    protected Object getResult() { 
      return getVar("RESULT");
    }

    /** Set the value of the variable in the current 
     * frame.
     * alias for getFrame().setVar **/
    protected void setVar(Object var, Object val) {
      getFrame().setVar(var, val);
    }

    /** get the value of the variable by searching through the stack
     * until a value is found.
     * If you want to avoid the search, use getFrame().getVar() instead.
     */
    protected Object getVar(Object var) {
      synchronized (getSMachine()) {
        return getSMachine().searchStack(var);
      }
    }

    /** copy the value in var1 into var2 **/
    protected void dupVar(Object var1, Object var2) {
      Frame f = getFrame();
      f.setVar(var2, getVar(var1));
    }

    /** call into a subroutine.  The subroutine should end with a transit to
     * the state "POP".
     **/
    protected void call(String tag, Object arg, String ret) {
      synchronized (getSMachine()) {
        getSMachine().pushFrame(new Frame(ret, arg));
        transit(tag);
      }
    }

    /** Usual case of returning from a subroutine.
     * equivalent to setResult() and transit("POP");
     **/
    protected void callReturn(Object result) {
      synchronized(getSMachine()) {
        setResult(result);
        transit("POP");
      }
    }

    protected void iterate(Collection values, String subTag, String nextTag) {
      synchronized (getSMachine()) {
        call("ITERATE", new Object[] {values, subTag}, nextTag);
      }
    }

  }


  //
  // example
  //
  
  public static void main(String[] args) {
    StackMachine sm = new StackMachine() {
        public void transit(State s0, State s1) {
          System.err.println("transiting from "+s0+" to "+s1);
          super.transit(s0, s1);
        }
      };

            
    sm.add(new SState("A") { public void invoke() { 
      setVar("i", new Integer(1));
      call("X", getVar("i"), "B");
    }});
    sm.add(new SState("B") { public void invoke() { 
      Integer i = (Integer) getVar("i");
      System.err.println(""+i+"*"+i+" = "+getResult());
      setVar("i", new Integer(1+i.intValue()));
      call("X", getVar("i"), "C");
    }});
    sm.add(new SState("C") { public void invoke() { 
      Integer i = (Integer) getVar("i");
      System.err.println(""+i+"*"+i+" = "+getResult());
      setVar("i", new Integer(1+i.intValue()));
      call("X", getVar("i"), "D");
    }});
    sm.add(new SState("D") { public void invoke() { 
      Integer i = (Integer) getVar("i");
      System.err.println(""+i+"*"+i+" = "+getResult());
      setVar("i", new Integer(1+i.intValue()));
      call("X", getVar("i"), "E");
    }});
    sm.add(new SState("E") { public void invoke() { 
      Integer i = (Integer) getVar("i");
      System.err.println(""+i+"*"+i+" = "+getResult());
      transit("DONE");
    }});

    sm.add(new SState("X") {
        public void invoke() {
          int arg = ((Integer)getArgument()).intValue();
          System.err.println("In X("+arg+")");
          callReturn(new Integer(arg*arg));
        }
      });


    // initialize to A
    sm.set("A");
   
    System.out.println("go():");
    sm.go();

    StackMachine sm1 = new StackMachine() {
        public void transit(State s0, State s1) {
          System.err.println("transiting from "+s0+" to "+s1);
          super.transit(s0, s1);
        }
      };
    sm1.add(new SState("T1") { public void invoke() {
      setVar("Collection", new ArrayList());
      ArrayList stuff = new ArrayList();
      stuff.add("A"); stuff.add("B"); stuff.add("C"); stuff.add("D"); stuff.add("E");
      iterate(stuff, "Sub", "T2");
    }});
    sm1.add(new SState("T2") { public void invoke() {
      System.out.println("I collected: ");
      for(Iterator it = ((Collection) getVar("Collection")).iterator(); it.hasNext(); ) {
        System.out.println("\t"+it.next());
      }
      transit("DONE");
    }});
    sm1.add(new SState("Sub") { public void invoke() {
      String s = (String) getArgument();
      s = "["+s+"]";
      // get the "Collection" var from two frames above (iterator counts).
      Collection c = (Collection) getVar("Collection");
      c.add(s);
      callReturn(null);
    }});
    sm1.set("T1");
    System.out.println("Testing iterate");
    sm1.go();
  }
}

