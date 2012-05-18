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

/**
 * A Thread class which can be restarted to rerun it's (settable)
 * runnable object.
 **/

public class ReusableThread extends Thread {
  /** reference to our thread pool so we can return when we die **/
  private ReusableThreadPool pool;
  
  /** our runnable object, or null if we haven't been assigned one **/
  private Runnable runnable = null;
  
  /** Has this thread already be actually started yet?
   * access needs to be guarded by runLock.
   **/
  private boolean isStarted = false;

  /** are we actively running the runnable? **/
  private boolean isRunning = false;

  /** guards isRunning, synced while actually executing and waits when
   * suspended.
   **/
  private Object runLock = new Object();

  public void setRunnable(Runnable r) {
    runnable = r;
  }
  protected Runnable getRunnable() {
    return runnable;
  }

  /** The only constructor. **/
  public ReusableThread(ReusableThreadPool p) {
    super(p.getThreadGroup(), null, "ReusableThread");
    setDaemon(true);
    pool = p;
  }

    // Hook for subclasses
    protected void claim() {
    }

  @Override
public final void run() {
    while (true) {
      synchronized (runLock) {
	  claim();
        Runnable r = getRunnable();
        if (r != null)
          r.run();
        isRunning = false;

        reclaim();

        try {
          runLock.wait();       // suspend
        } catch (InterruptedException ie) {}
      }
    }
  }

  @Override
public void start() throws IllegalThreadStateException {
    synchronized (runLock) {
      if (isRunning) 
        throw new IllegalThreadStateException("ReusableThread already started: "+
                                              this);
      isRunning = true;

      if (!isStarted) {
        isStarted=true;
        super.start();
      } else {
        runLock.notify();     // resume
      }
    }
  }

  protected synchronized void reclaim() {
    pool.reclaimReusableThread(this);
    notifyAll();
  }
}
