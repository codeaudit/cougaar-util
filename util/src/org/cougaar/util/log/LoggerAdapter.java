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

/** Standard implementation of all the busy-work of Logger
 **/
public abstract class LoggerAdapter
  implements Logger
{

  public abstract boolean isEnabledFor(int level);
  public abstract void log(int level, String message, Throwable t);


  public final void log(int level, String message) { log(level,message,null); }

  public final boolean isDetailEnabled() { return isEnabledFor(DETAIL); }
  public final boolean isDebugEnabled()  { return isEnabledFor(DEBUG); }
  public final boolean isInfoEnabled()   { return isEnabledFor(INFO); }
  public final boolean isWarnEnabled()   { return isEnabledFor(WARN); }
  public final boolean isErrorEnabled()  { return isEnabledFor(ERROR); }
  public final boolean isShoutEnabled()  { return isEnabledFor(SHOUT); }
  public final boolean isFatalEnabled()  { return isEnabledFor(FATAL); }

  public final void detail(String message) { log(DETAIL, message, null); }
  public final void detail(String message, Throwable t) { log(DETAIL, message, t); }
  public final void debug(String message) { log(DEBUG, message, null); }
  public final void debug(String message, Throwable t) { log(DEBUG, message, t); }
  public final void info(String message) { log(INFO, message, null); }
  public final void info(String message, Throwable t) { log(INFO, message, t); }
  public final void warn(String message) { log(WARN, message, null); }
  public final void warn(String message, Throwable t) { log(WARN, message, t); }
  public final void error(String message) { log(ERROR, message, null); }
  public final void error(String message, Throwable t) { log(ERROR, message, t); }
  public final void shout(String message) { log(SHOUT, message, null); }
  public final void shout(String message, Throwable t) { log(SHOUT, message, t); }
  public final void fatal(String message) { log(FATAL, message, null); }
  public final void fatal(String message, Throwable t) { log(FATAL, message, t); }
}
