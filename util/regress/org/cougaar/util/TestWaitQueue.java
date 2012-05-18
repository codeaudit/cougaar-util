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
import java.util.ArrayList;

import junit.framework.TestCase;

public class TestWaitQueue extends TestCase {

  public void test_wq() {
    ArrayList keys = new ArrayList();
    final WaitQueue wq = new WaitQueue();

    for (int i = 0; i<10; i++) {
      final Object k = wq.getKey();
      final Object r = new Integer(i);
      new Thread(new Runnable() {
          public void run() {
            try {
              Thread.sleep(500); /*half a second*/
            } catch(Exception e) {
              e.printStackTrace();
            }
            wq.trigger(k, r);
          }
        }
                 ).start();
      keys.add(k);
      
      try {
        Thread.sleep(100);      /*tenth of a second*/
      } catch(Exception e) {
        e.printStackTrace();
      }

    }
    
    for (int i=0;i<10; i++) {
      try {
        Object v = wq.waitFor(keys.get(i));
        assertTrue("result "+i, (new Integer(i)).equals(v));
      } catch (InterruptedException ie) {
        fail();
      }
    }
  }
}
