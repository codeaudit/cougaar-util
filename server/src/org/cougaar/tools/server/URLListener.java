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
 
package org.cougaar.tools.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Client adapter (server) that listens on a port for URL 
 * connections keeps a map of (path, OutputListener) pairs.
 * <p>
 * Spawns multiple threads to listen on the specified port.
 */
public class URLListener {

  //
  // FIXME this implementation is "okay" for now.. it has 
  // some threading and socket-usage issues which can be 
  // addressed later.
  //

  private final int port;
  private final int timeoutMillis;
  private final String urlBase;

  // map of (id, OutputListener) pairs
  private final Map m = new HashMap();

  private boolean running;

  private ServerSocket serverSock;

  private final Object lock = new Object();

  public URLListener(int port) {
    this(port, -1);
  }

  public URLListener(int port, int timeoutMillis) {
    this.port = port;
    this.timeoutMillis = timeoutMillis;
    if (port <= 0) {
      throw new IllegalArgumentException("Bad port: "+port);
    }
    String localHostName;
    try {
      localHostName = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to get local host address: "+e.getMessage());
    }
    this.urlBase = "http://"+localHostName+":"+port+"/";
  }

  /**
   * Start the server.
   */
  public void start() {
    spawnServer();
  }

  public void stop() {
    synchronized (lock) {
      haltServer();
    }
  }

  /**
   * Equivalent to "addListener(id,ol,false)".
   */
  public URL addListener(
      String id, 
      OutputListener ol) {
    return addListener(id, ol, false);
  }

  /**
   * Add a listener with the given name, returning the URL
   * for RemoteListenable registration.
   *
   * @param override if true then if the name is already taken
   *    the other listener will be removed and the
   *    passed listener will take its place; if false then
   *    a "name already in use" exception is thrown instead.
   *
   * @see #start
   */
  public URL addListener(
      String id, 
      OutputListener ol,
      boolean override) {
    if ((id == null) ||
        (ol == null)) {
      throw new NullPointerException();
    }
    URL ret;
    try {
      ret = new URL(urlBase+id);
    } catch (MalformedURLException mue) {
      throw new IllegalArgumentException(
          "Illegal name format \""+id+"\": "+mue.getMessage());
    }
    synchronized (lock) {
      if (m.get(id) != null) {
        if (override) {
          // FIXME remove the other listener!
          // do we need a map of (id, clientSocket) to close?
        } else {
          throw new IllegalArgumentException(
              "Listener name \""+id+"\" is already in use");
        }
      }
      m.put(id, ol);
    }
    return ret;
  }

  public void removeListener(String id) {
    if (id == null) {
      throw new NullPointerException();
    }
    synchronized (lock) {
      m.remove(id);
      // FIXME race condition for remove/handle
    }
  }

  //
  // the rest is protected / private:
  //

  private OutputListener getOutputListener(String id) {
    synchronized (lock) {
      return (OutputListener) m.get(id);
    }
  }

  protected void scheduleServer(Runnable r) {
    // FIXME add a thread pool (or use nio?)
    Thread t = new Thread(r, r.toString());
    t.start();
  }

  protected void scheduleClient(Runnable r) {
    // FIXME add a thread pool (or use nio?)
    Thread t = new Thread(r, r.toString());
    t.start();
  }

  protected void spawnServer() {
    // create runner
    Runnable r = new Runnable() {
      public void run() {
        serve();
      }
      @Override
      public String toString() {
        return "URL listener for server ("+port+")";
      }
    };
    scheduleServer(r);
  }

  protected void spawnHandler(final Socket clientSock) {
    // create runner
    Runnable r = new Runnable() {
      public void run() {
        handle(clientSock);
      }
      @Override
      public String toString() {
        return "URL socket handler for ("+port+") client: "+clientSock;
      }
    };
    scheduleClient(r);
  }

  protected void haltServer() {
    synchronized (lock) {
      running = false;
      if (serverSock != null) {
        try {
          serverSock.close();
        } catch (Exception e) {
          System.err.println("Unable to cleanly halt server:");
          e.printStackTrace();
        }
      }
      // wait for server?
    }
  }

  protected final void serve() {
    synchronized (lock) {
      if (running) {
        throw new IllegalStateException(
            "Server already running");
      }
      running = true;
    }
    try {
      serverSock = new ServerSocket(port);
      if (timeoutMillis > 0) {
        serverSock.setSoTimeout(timeoutMillis);
      }
      while (running) {
        final Socket clientSock;
        try {
          clientSock = serverSock.accept();
        } catch (SocketTimeoutException ste) {
          continue;
        }
        spawnHandler(clientSock);
      }
    } catch (Exception e) {
      System.err.println("Listener ("+port+") failure: ");
      e.printStackTrace();
      System.err.println("Listener ("+port+") halting");
    } finally {
      haltServer();
    }
  }

  protected final void handle(Socket clientSock) {
    InputStream is = null;
    try {
      if (timeoutMillis > 0) {
        // FIXME do we want this timeout???
        clientSock.setSoTimeout(timeoutMillis);
      }
      is = clientSock.getInputStream();
      handleClient(is);
    } catch (Exception e) {
      System.err.println("Listener ("+port+") failure: ");
      e.printStackTrace();
      System.err.println("Listener ("+port+") halting");
      haltServer();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
          System.err.println(
              "Unable to cleanly close client input stream:");
          e.printStackTrace();
        }
      }
      try {
        clientSock.close();
      } catch (Exception e) {
        System.err.println(
            "Unable to cleanly close client socket:");
        e.printStackTrace();
      }
    }
  }

  private void handleClient(InputStream is) throws Exception {
    // read header
    String path = readPath(is);
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    String id = path;
    // find listener
    //
    // FIXME maybe find listener per bundle, to allow
    // for dynamic (client) substitution of listener?
    OutputListener ol = getOutputListener(id);
    if (ol == null) {
      System.err.println("Unknown listener: "+id);
    }
    // read bundles
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(is);
      while (running) {
        OutputBundle ob = readOutputBundle(ois);
        if (ob == null) {
          break;
        }
        ol.handleOutputBundle(ob);
      }
    } finally {
      if (ois != null) {
        try {
          ois.close();
        } catch (Exception e) {
          System.err.println(
              "Unable to cleanly close client object stream:");
          e.printStackTrace();
        }
      }
    }
  }

  private String readPath(
      InputStream is) throws IOException {
    // read method & ' '
    int i;
    while (true) {
      i = is.read();
      if (i == ' ') {
        // skip whitespace
        do {
          i = is.read();
        } while (i == ' ');
        break;
      }
    }
    // read path
    StringBuffer buf = new StringBuffer(13);
    while (true) {
      buf.append((char) i);
      i = is.read();
      if (i == ' ') {
        break;
      }
    }
    String path = buf.toString();
    // read until end of line
    // read (optional) non-empty lines
    // read empty line
    //
    // FIXME assume only one line
    while (true) {
      i = is.read();
      if (i == '\r') {
        is.read(); // \n
        is.read(); // \r
        is.read(); // \n
        break;
      }
    }
    return path;
  }

  private OutputBundle readOutputBundle(
      ObjectInputStream ois) {
    Object o;
    try {
      o = ois.readObject();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
    if (o == null) {
      return null;
    }
    if (!(o instanceof OutputBundle)) {
      throw new IllegalArgumentException(
          "Not an OutputBundle: "+o.getClass());
    }
    OutputBundle ob = (OutputBundle) o;
    return ob;
  }

  @Override
public String toString() {
    return "URLListener on port "+port;
  }
}
