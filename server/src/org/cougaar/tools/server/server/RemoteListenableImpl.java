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

package org.cougaar.tools.server.server;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.cougaar.tools.server.OutputBundle;
import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteListenableConfig;

/**
 * Server-side buffer for all output to the client.
 * <p>
 * Consider enhancing with new non-blocking io (nio).
 */
class RemoteListenableImpl implements RemoteListenable {

  private static final int MIN_SEND_PERIOD = 5000;

  private final String name;
  private final BufferWatcher bw;

  /**
   *
   */
  private final Timer timer = new Timer();

  private final TimerTask sendTask = new TimerTask() {
    @Override
   public void run() {
      if (!(flushBuffer(false))) {
        cancel();
      }
    }
  };

  //
  // these are locked by the "obLock"
  //
  private final Object obLock = new Object();
  private OutputBundle ob;
  private OutputStream stdOut;
  private OutputStream stdErr;

  //
  // these are locked by the "watcherLock"
  //
  private final Object watcherLock = new Object();
  private OutputWatcher stdOutWatcher;
  private OutputWatcher stdErrWatcher;
  private Thread stdOutWatcherThread;
  private Thread stdErrWatcherThread;

  //
  // the rest is locked by the "sendLock"
  //
  private final Object sendLock = new Object();

  // a map of (id, ol) pairs
  private final Map listeners = new HashMap(5);

  // one policy for all listeners?
  private OutputPolicy op;

  public RemoteListenableImpl(
      String name,
      BufferWatcher bw,
      RemoteListenableConfig rlc) {

    this.name = name;
    this.bw = bw;
    if (bw == null) {
      throw new IllegalArgumentException("Null buffer watcher");
    }

    // get initial listeners
    String id = rlc.getId();
    OutputListener ol = rlc.getOutputListener();
    if (ol != null) {
      listeners.put(id, ol);
    }
    URL url = rlc.getURL();
    if (url != null) {
      listeners.put(id, new URLOutputListenerAdapter(url));
    }

    // get output policy
    OutputPolicy op = rlc.getOutputPolicy();
    this.op = op;

    // create the output buffer
    ob = new OutputBundle();
    ob.setName(name);
    ob.setCreated(true);
    ob.setTimeStamp(System.currentTimeMillis());

    // schedule the buffer flush
    int sendPeriod = MIN_SEND_PERIOD;
    timer.schedule(sendTask, 0, sendPeriod);
  }

  public void setStreams(InputStream newIn, InputStream newErr) {

    synchronized (obLock) {
      if (ob == null) {
        throw new RuntimeException("Output has been closed");
      }

      stdOut = ob.getDualStreamBuffer().getOutputStream(true);
      stdErr = ob.getDualStreamBuffer().getOutputStream(false);
    }

    synchronized (watcherLock) {
      stdOutWatcher = 
        new OutputWatcher(newIn, true);

      stdErrWatcher = 
        new OutputWatcher(newErr, false);

      stdOutWatcherThread = 
        new Thread(stdOutWatcher, name+"-stdOut");
      stdErrWatcherThread = 
        new Thread(stdErrWatcher, name+"-stdErr");

      stdOutWatcherThread.start();
      stdErrWatcherThread.start();
    }
  }

  public List list() {
    synchronized (sendLock) {
      return
        (listeners.isEmpty() ?
         Collections.EMPTY_LIST :
         (new ArrayList(listeners.keySet())));
    }
  }

  public void addListener(URL url) {
    addListener(new URLOutputListenerAdapter(url), url.toString());
  }

  public void removeListener(URL url) {
    removeListener(url.toString());
  }

  public void addListener(OutputListener ol, String id) {
    if (ol == null) {
      throw new IllegalArgumentException(
          "Client OutputListener can not be null");
    }
    synchronized (sendLock) {
      OutputListener x = (OutputListener) listeners.put(id, ol);
      if (x != null) {
        listeners.put(id, x);
        throw new RuntimeException(
            "A listener with id \""+id+
            "\" is already registered");
      }
    }
  }

  public void removeListener(String id) {
    OutputListener x;
    synchronized (sendLock) {
      x = (OutputListener) listeners.remove(id);
    }
    if (x instanceof URLOutputListenerAdapter) {
      try {
        ((URLOutputListenerAdapter) x).close();
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  public OutputPolicy getOutputPolicy() {
    return op;
  }

  //
  // lots of "sync {..}" here ... will this block the process?
  //

  public void setOutputPolicy(
      OutputPolicy op) throws Exception {
    if (op == null) {
      throw new IllegalArgumentException(
          "Client OutputPolicy can not be null");
    }
    // set buffering policy
    // FIXME!
    this.op = op;
  }

  public void flushOutput() throws Exception {
    flushBuffer(false);
  }

  //
  // for RemoteProcessImpl use only:
  //

  public boolean appendIdleUpdate(double percent, long time) {
    String msg = percent+":"+time;
    synchronized (obLock) {
      if (ob == null) {
        return false;
      }
      ob.getIdleUpdates().add(msg);
    }
    return true;
  }

  public void close() throws Exception {

    flushBuffer(true);

    // remove all listeners
    List urlL = null;
    synchronized (sendLock) {
      for (
          Iterator iter = listeners.values().iterator();
          iter.hasNext();
          ) {
        OutputListener ol = (OutputListener) iter.next();
        if (ol instanceof URLOutputListenerAdapter) {
          if (urlL == null) {
            urlL = new ArrayList(3);
          }
          urlL.add(ol);
        }
      }
      listeners.clear();
    }

    // close any url listeners
    if (urlL != null) {
      for (Iterator iter = urlL.iterator(); iter.hasNext(); ) {
        URLOutputListenerAdapter uol = 
          (URLOutputListenerAdapter) iter.next();
        try {
          uol.close();
        } catch (Exception e) {
          System.err.println(
              "Unable to close URL listener "+uol+": "+e);
        }
      }
    }

    // wait for the streams to end
    //
    // must carefully handle possible simultaneous "close()"
    // called by the output threads themselves, e.g. due to
    // I/O errors.
    synchronized (watcherLock) {
      Thread thisThread = Thread.currentThread();
      if (stdOutWatcherThread != null) {
        if (stdOutWatcherThread != thisThread) {
          try {
            stdOutWatcherThread.join();
          } catch (Exception e) {
            System.err.println(
                "Unable to wait for std-out completion");
          }
        }
        stdOutWatcherThread = null;
      }
      if (stdErrWatcherThread != null) {
        if (stdErrWatcherThread != thisThread) {
          try {
            stdErrWatcherThread.join();
          } catch (Exception e) {
            System.err.println(
                "Unable to wait for std-err completion");
          }
        }
        stdErrWatcherThread = null;
      }
    }
  }

  //
  // the rest is private
  //

  private boolean flushBuffer(boolean isClosing) {
    // switch buffers
    OutputBundle t;
    synchronized (obLock) {
      if (ob == null) {
        return false;
      }
      t = ob;
      if (isClosing) {
        t.setDestroyed(true);
        ob = null;
      } else {
        ob = new OutputBundle();
        ob.setName(name);
        ob.setTimeStamp(System.currentTimeMillis());
        stdOut = ob.getDualStreamBuffer().getOutputStream(true);
        stdErr = ob.getDualStreamBuffer().getOutputStream(false);
      }
    }

    // send the buffered output
    if (!(sendOutputBundle(t, isClosing))) {
      // stop the timer
      return false;
    }

    // okay
    return true;
  }

  private boolean sendOutputBundle(
      OutputBundle t, boolean isClosing) {
    // FIXME:
    // keep multiple threaded queues, one per listener,
    // so a slow listener won't block all the other listeners
    synchronized (sendLock) {

      for (
          Iterator iter = listeners.entrySet().iterator();
          iter.hasNext();
          ) {
        Map.Entry me = (Map.Entry) iter.next();
        String id = (String) me.getKey();
        OutputListener ol = (OutputListener) me.getValue();

        // send the output
        Exception e;
        try {
          ol.handleOutputBundle(t);
          continue;
        } catch (Exception xe) {
          e = xe;
        }

        if (isClosing) {
          // we're closing, so we don't care if there's a 
          // problem
          continue;
        }

        // notify the failure listener
        int i;
        try {
          i = bw.handleOutputFailure(id, e);
        } catch (Exception e2) {
          i = BufferWatcher.KILL_ALL_LISTENERS;
        }
        switch (i) {
          case BufferWatcher.KEEP_RUNNING:
            break;
          case BufferWatcher.KILL_CURRENT_LISTENER:
            removeListener(id);
          default:
          case BufferWatcher.KILL_ALL_LISTENERS:
            try {
              close();
            } catch (Exception e3) {
              System.err.println("Close listeners exception: "+e3);
            }
            return false;
        }
      }
    }
    return true;
  }

  private class URLOutputListenerAdapter implements OutputListener {
    // stream for URL objects
    private URL url;
    private Socket urlSocket;
    private ObjectOutputStream urlStream;

    public URLOutputListenerAdapter(URL url) {
      this.url = url;
    }

    public void handleOutputBundle(OutputBundle t) throws Exception {
      if (url != null) {
        ensureURLStream();
        urlStream.writeObject(t);
      }
    }

    private void ensureURLStream() throws Exception {
      // within sync(sendLock)
      if (urlStream == null) {
        openURLStream();
        // catch exception and have retry timer?
      }
    }

    private void openURLStream() throws Exception {
      // within sync(sendLock)
      Socket socket = new Socket(url.getHost(), url.getPort());
      OutputStream os = socket.getOutputStream();
      String header = "PUT "+url.getPath()+" HTTP/1.0\r\n\r\n";
      os.write(header.getBytes());
      ObjectOutputStream oos = new ObjectOutputStream(os);
      // save
      urlSocket = socket;
      urlStream = oos;
    }

    private void close() throws Exception {
      if (url != null) {
        url = null;
        closeURLStream();
      }
    }

    private void closeURLStream() throws Exception {
      // within sync(sendLock)
      if (urlStream != null) {
        try {
          urlStream.writeObject(null);
          urlStream.close();
          urlSocket.close();
        } finally {
          urlStream = null;
          urlSocket = null;
        }
      }
    }

    @Override
   public String toString() {
      return url.toString();
    }
  }

  /**
   * Input-to-buffer pipe.
   */
  private class OutputWatcher implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private InputStream in;
    private final boolean isStdOut;

    public OutputWatcher(
        InputStream in, 
        boolean isStdOut) {
      this.in = in;
      this.isStdOut = isStdOut;
    }

    public void run() {
      // we'll stream into the outer buffer
      byte[] buf = new byte[BUFFER_SIZE];
      while (true) {

        // read input
        int len;
        try {
          len = in.read(buf);
        } catch (Exception e) {
          // notify the failure listener
          int i;
          try {
            i = bw.handleInputFailure(e);
          } catch (Exception e2) {
            i = BufferWatcher.KILL_ALL_LISTENERS;
          }
          switch (i) {
            case BufferWatcher.KEEP_RUNNING:
              continue;
            default:
            case BufferWatcher.KILL_ALL_LISTENERS:
              try {
                close();
              } catch (Exception e3) {
                System.err.println("Close listeners exception: "+e3);
              }
              len = -1;
          }
        }

        // check for end-of-input
        if (len < 0) {
          return;
        }

        // write to buffer
        try {
          synchronized (obLock) {
            if (ob == null) {
              return;
            }
            if (isStdOut) {
              stdOut.write(buf, 0, len);
            } else {
              stdErr.write(buf, 0, len);
            }
          }
        } catch (Exception e) {
          // shouldn't happen
          System.err.println(e);
        }
      }
    }
  }

}
