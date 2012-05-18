/*
 * <copyright>
 *  
 *  Copyright 2003-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.util;

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
        @Override
      public void invoke() {
          Frame f = popFrame();
          setVar("RESULT", f.getRetval());
          transit(f.getReturnTag());
        }});

    add(new SState("ITERATE") {
        @Override
      public void invoke() {
          Object[] args = (Object[]) getArgument();
          Collection values = (Collection)args[0];
          String subTag = (String)args[1];
          setVar("it", values.iterator());
          setVar("subTag", subTag);
          transit("ITERATE1");
        }});
    add(new SState("ITERATE1") {
        @Override
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
}

