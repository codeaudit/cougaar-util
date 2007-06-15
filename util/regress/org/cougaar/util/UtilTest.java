/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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
import junit.framework.*;

public class UtilTest extends TestCase {
  public void test1() {
    assertEquals(1, 1);
  }

  public static Test suite() {
    TestSuite suite= new TestSuite("Cougaar Utility Class Tests");
    suite.addTest(new TestSuite(TestArguments.class));
    suite.addTest(new TestSuite(TestAnnotations.class));
    suite.addTest(new TestSuite(TestCircularQueue.class));
    suite.addTest(new TestSuite(TestDBProperties.class));
    suite.addTest(new TestSuite(TestMappings.class));
    suite.addTest(new TestSuite(TestNonOverlappingTimeSpanSet.class));
    suite.addTest(new TestSuite(TestPropertyTree.class));
    suite.addTest(new TestSuite(TestRarelyModifiedList.class));
    suite.addTest(new TestSuite(TestShortDateFormat.class));
    suite.addTest(new TestSuite(TestStackMachine.class));
    suite.addTest(new TestSuite(TestStateMachine.class));
    suite.addTest(new TestSuite(TestStringUtility.class));
    suite.addTest(new TestSuite(TestTimeSpanSet.class));
    suite.addTest(new TestSuite(TestWaitQueue.class));
    return suite;
  }
}
