/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

public class TestStackMachine extends TestCase {
  private class MySM extends StackMachine {
    private String seq = null;    // we'll accumulate the states here (yuck)
    String getSeq() { return seq; }
    @Override
   protected void _set(State s) {
      if (seq == null) {
        seq = s.getKey();
      } else {
        seq = seq+" "+s.getKey();
      }
      super._set(s);
    }
  }


  public void test_sm1() {
    MySM sm = new MySM();
            
    sm.add(new StackMachine.SState("A") { @Override
   public void invoke() { 
      setVar("i", new Integer(1));
      call("X", getVar("i"), "B");
    }});
    sm.add(new StackMachine.SState("B") { @Override
   public void invoke() { 
      Integer i = (Integer) getVar("i");
      //System.err.println(""+i+"*"+i+" = "+getResult());
      setVar("i", new Integer(1+i.intValue()));
      call("X", getVar("i"), "C");
    }});
    sm.add(new StackMachine.SState("C") { @Override
   public void invoke() { 
      Integer i = (Integer) getVar("i");
      //System.err.println(""+i+"*"+i+" = "+getResult());
      setVar("i", new Integer(1+i.intValue()));
      call("X", getVar("i"), "D");
    }});
    sm.add(new StackMachine.SState("D") { @Override
   public void invoke() { 
      Integer i = (Integer) getVar("i");
      //System.err.println(""+i+"*"+i+" = "+getResult());
      setVar("i", new Integer(1+i.intValue()));
      call("X", getVar("i"), "E");
    }});
    sm.add(new StackMachine.SState("E") { @Override
   public void invoke() { 
      transit("DONE");
    }});

    sm.add(new StackMachine.SState("X") {
        @Override
      public void invoke() {
          int arg = ((Integer)getArgument()).intValue();
          //System.err.println("In X("+arg+")");
          callReturn(new Integer(arg*arg));
        }
      });


    // initialize to A
    sm.reset("A");
    sm.go();

    assertEquals("A X POP B X POP C X POP D X POP E DONE", sm.getSeq());
  }

  public void test_sm2() {
    MySM sm = new MySM();

    sm.add(new StackMachine.SState("T1") { @Override
   public void invoke() {
      setVar("Collection", new ArrayList());
      ArrayList stuff = new ArrayList();
      stuff.add("A"); stuff.add("B"); stuff.add("C"); stuff.add("D"); stuff.add("E");
      iterate(stuff, "Sub", "T2");
    }});
    sm.add(new StackMachine.SState("T2") { @Override
   public void invoke() {
      //System.out.println("I collected: ");
      for(Iterator it = ((Collection) getVar("Collection")).iterator(); it.hasNext(); ) {
        it.next();
        //System.out.println("\t"+it.next());
      }
      transit("DONE");
    }});
    sm.add(new StackMachine.SState("Sub") { @Override
   public void invoke() {
      String s = (String) getArgument();
      s = "["+s+"]";
      // get the "Collection" var from two frames above (iterator counts).
      Collection c = (Collection) getVar("Collection");
      c.add(s);
      callReturn(null);
    }});
    sm.set("T1");
    //System.out.println("Testing iterate");
    sm.go();
    
    assertEquals("T1 ITERATE ITERATE1 "+
                 "Sub POP ITERATE1 "+
                 "Sub POP ITERATE1 "+
                 "Sub POP ITERATE1 "+
                 "Sub POP ITERATE1 "+
                 "Sub POP ITERATE1 "+
                 "POP T2 DONE"
                 , sm.getSeq());
  }
}
