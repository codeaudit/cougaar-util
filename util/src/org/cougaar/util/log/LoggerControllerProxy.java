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

package org.cougaar.util.log;

import java.util.Enumeration;

/**
 * This class simply wraps a LoggerController and proxies it.
 *
 * @see LoggerController
 */
public class LoggerControllerProxy implements LoggerController {

  protected LoggerController lc;

  public LoggerControllerProxy(LoggerController lc) {
    this.lc = lc;
  }

  public LoggerController getLoggerController(String name) {
    return lc.getLoggerController(name);
  }
  public Enumeration getAllLoggerNames() {
    return lc.getAllLoggerNames();
  }
  public int getLoggingLevel() {
    return lc.getLoggingLevel();
  }
  public void setLoggingLevel(int level) {
    lc.setLoggingLevel(level);
  }
  public LogTarget[] getLogTargets() {
    return lc.getLogTargets();
  }
  public void addLogTarget(
      int outputType, Object outputDevice) {
    lc.addLogTarget(outputType, outputDevice);
  }
  public void addConsole(){
    lc.addConsole();
  }
  public boolean removeLogTarget(
      int outputType, Object outputDevice) {
    return lc.removeLogTarget(outputType, outputDevice);
  }
  public boolean removeLogTarget(
      int outputType, String deviceString){
    return lc.removeLogTarget(outputType, deviceString);
  }

  @Override
public String toString() {
    return lc.toString();
  }

}
