/*
 * <copyright>
 * Copyright 2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.util.log;

import java.util.*;
import org.cougaar.util.*;

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
