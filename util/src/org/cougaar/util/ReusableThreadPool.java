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
 * Provide a pool of reusable threads to reduce the overhead of 
 * construction/destruction of large numbers of standard thread,
 * particularly on VM implementation which use OS-level thread 
 * implementations. 
 *
 * The general contract is that wherever you might have used
 * "new Thread()", you use "ReusableThread.newThread()" instead.
 * Most of the Thread constructors are provided as factory methods -
 * missing are all constructors using ThreadGroup arguments.
 * @property org.cougaar.ReusableThread.initialPoolSize Sets the inital pool size for 
 * the thread pool utility (defaults to 32).
 * @property org.cougaar.ReusableThread.maximumPoolSize Sets the maximum pool size for
 * the thread pool utility (defaults to 64).
 *
 **/

public class ReusableThreadPool {
  public static ReusableThreadPool defaultPool = null;
  
  /** initial number of ReusableThreads in the pool **/
  private static int defaultInitialPoolSize;
  /** maximum number of unused ReusableThreads to keep in the pool **/
  private static int defaultMaximumPoolSize;

  /** initialize initialPoolSize and maximumPoolSize from system,
   * properties and create the default ThreadPool from these
   * values.
   */
  static {
    defaultInitialPoolSize = PropertyParser.getInt("org.cougaar.ReusableThread.initialPoolSize", 32);
    defaultMaximumPoolSize = PropertyParser.getInt("org.cougaar.ReusableThread.maximumPoolSize", 64);

  }

  /** The ThreadGroup of the pool - all threads in the pool must be
   * members of the same threadgroup.
   **/
  private ThreadGroup group;
  /** The maximum number of unused threads to keep around in the pool.
   * anything beyond this may be destroyed or GCed.
   **/
  private int maximumSize;
  /** the number of unused threads currently in the pool. **/
  private int poolSize;
  /** the actual pool **/
  private ReusableThread pool[];

  public ThreadGroup getThreadGroup() { return group; }
  public int getMaximumSize() { return maximumSize; }
  public int size() { return poolSize; }

  private int totalThreads = 0;
  /** Return the total number of threads created.  This can be larger than
   * the maximum pool size because the pool is usually 
   * allowed to create additional threads which will not be reclaimed.
   **/
  public int getAllocatedThreads() { return totalThreads; }

  public ReusableThreadPool(ThreadGroup group, int initial, int maximum) {
    this.group = group;

    if (initial > maximum) 
      initial = maximum;

    maximumSize = maximum;

    pool = new ReusableThread[maximum];
      
    for (int i = 0 ; i < initial; i++) {
      pool[i] = constructReusableThread();
    }
    poolSize = initial;
  }

  public ReusableThreadPool(int initial, int maximum) {
    this(Thread.currentThread().getThreadGroup(), initial, maximum);
  }


  public ReusableThread getThread() {
    return getThread(null, "ReusableThread");
  }
  public ReusableThread getThread(String name) {
    return getThread(null, name);
  }
  public ReusableThread getThread(Runnable runnable) {
    return getThread(runnable, "ReusableThread");    
  }
  
  public ReusableThread getThread(Runnable runnable, String name) {
    ReusableThread t = null;

    synchronized (this) {
      if (poolSize>0) {
        poolSize--;
        t = pool[poolSize];
        pool[poolSize] = null;  // clear for gc
      }
    }
    if (t == null) {
      t = constructReusableThread();
    }

    t.setRunnable(runnable);
    t.setName(name);

    //System.err.println("getThread pool="+poolSize+" = "+t);
    return t;
  }
  
  /** actually construct a new ReusableThread **/
  protected ReusableThread constructReusableThread() {
    totalThreads++;
    return new ReusableThread(this);
  }

  /** return a reusableThread to our pool.  package protected
   * so we don't get the wrong thread in our pool.
   **/
  void reclaimReusableThread(ReusableThread t) {
    synchronized (this) {
      if (poolSize < maximumSize) {
        // the pool has space - reuse it
        pool[poolSize] = t;
        poolSize++;
        //System.err.println("reclaimThread pool="+poolSize+" = "+t);
      } else {
        // the pool is already full - drop it.
        // t.destroy();
        //System.err.println("reclaimThread dropped "+t);
      }
    }
  }

  
  public static ReusableThreadPool getDefaultThreadPool() {
    if (defaultPool == null) {
      defaultPool = new ReusableThreadPool(defaultInitialPoolSize, defaultMaximumPoolSize);
    }
    return defaultPool;
  }

  //
  // regression test
  //
  
  public static class Counter {
    private int value = 0;
    public void incr() { value++; }
    public String toString() { return new Integer(value).toString(); }
    public int getValue() { return value; }
    public Counter() { }
    public Counter(int v) { value = v; }
  }

  public static class TestThread extends ReusableThread {
    static final Counter count = new Counter();

    Counter mycount;
    int myinvoke = 0;
    public TestThread(ReusableThreadPool p) {
      super(p);
      count.incr();
      mycount = new Counter(count.getValue());
      System.err.println("Created "+this);
    }
    public Runnable getRunnable() {
      myinvoke++;
      System.err.println("Invoking "+this);
      return super.getRunnable();
    }
    protected void reclaim() {
      System.err.println("Reclaiming "+this);
      super.reclaim();
    }
    public String toString() { 
      return "<"+getName()+" "+mycount+"("+myinvoke+")>";
    }

    public static int getCount() { return count.getValue(); }
  }

  public static class TestPool extends ReusableThreadPool {
    public TestPool(int init, int max) {
      super(init,max);
    }

    protected ReusableThread constructReusableThread() {
      ReusableThread t = new TestThread(this);
      return t;
    }
  }
}
