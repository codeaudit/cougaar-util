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
import junit.framework.TestCase;

public class TestStateMachine extends TestCase {
  class TestSM extends StateMachine {
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

  public TestSM createSM() {
    TestSM sm = new TestSM();
    sm.add(new StateMachine.State("A") { @Override
   public void invoke() { transit("B"); }});
    sm.add(new StateMachine.State("B") { @Override
   public void invoke() { transit("C"); }});
    sm.add(new StateMachine.State("C") { @Override
   public void invoke() { transit("D"); }});
    sm.add(new StateMachine.State("D") { @Override
   public void invoke() { transit("DONE"); }});

    sm.reset("A");
    return sm;
  }    

  public void test_step() {
    TestSM sm = createSM();
    while (sm.getState() != StateMachine.DONE) {
      sm.step();
    }
    assertEquals(sm.getSeq(), "A B C D DONE");
  }    

  public void test_stepUntilDone() {
    TestSM sm = createSM();
    sm.stepUntilDone();
    assertEquals(sm.getSeq(), "A B C D DONE");
  }
  
  public void test_go() {
    TestSM sm = createSM();
    // replace B with a state which requires several invokes
    sm.add(new StateMachine.State("B") {
        private int count = 0;
        @Override
      public void invoke() { 
          count++;
          if (count >= 3) transit("C"); 
          else transit("B");
        }});

    while (sm.getState() != StateMachine.DONE) {
      sm.go();
    }
    assertEquals(sm.getSeq(), "A B B B C D DONE");
  }
  
  public void test_reset() {
    TestSM sm = createSM();
    sm.stepUntilDone();
    sm.reset("A");
    sm.stepUntilDone();
    assertEquals(sm.getSeq(), "A B C D DONE A B C D DONE");
  } 
}

