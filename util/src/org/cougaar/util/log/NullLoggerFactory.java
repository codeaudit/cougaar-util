/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.util.log;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/** 
 * No-op LoggerFactory to connect to NullLogger instances.
 */
public class NullLoggerFactory 
  extends LoggerFactory 
{
  private static final LoggerController LC = new NullLoggerController();

  public void configure(Properties props) {}
  public void configure(Map m) {}
  public Logger createLogger(Object requestor) {
    return NullLogger.getLogger();
  }

  public LoggerController createLoggerController(String requestor) {
    return LC;
  }

  private static class NullLoggerController implements LoggerController {
    public LoggerController getLoggerController(String name) { return this; }
    public Enumeration getAllLoggerNames() { return new org.cougaar.util.EmptyEnumeration(); }
    public int getLoggingLevel() { return Logger.DEBUG; }
    public void setLoggingLevel(int level) {}
    public LogTarget[] getLogTargets() { return new LogTarget[0]; }
    public void addLogTarget(int outputType, Object outputDevice) {}
    public void addConsole() {}
    public boolean removeLogTarget(int outputType, Object outputDevice) {return false; }
    public boolean removeLogTarget( int outputType, String deviceString) {return false; }
  }
}
