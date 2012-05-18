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

package org.cougaar.lib.contract.lang;

/**
 * An <code>org.cougaar.lib.contract.Operator</code> parse failure.
 */
public class ParseException extends Exception {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
private StringBuffer trace;
  private String cacheTrace;

  private ParseException() { }

  public ParseException(String s) {
    if (s != null) {
      trace = new StringBuffer(s.trim());
      trace.append("\nTrace:\n");
    } else {
      trace = new StringBuffer("Unknown Parse Exception\nTrace:\n");
    }
  }

  public ParseException(Exception e) {
    this(e.toString());
    //e.printStackTrace();
  }

  public void addTrace(String line) {
    trace.append("       \"at ");
    if (line != null) {
      trace.append(line).append("\"\n");
    } else {
      trace.append("<Unknown line>\"\n");
    }
  }

  /**
   * Override <code>java.lang.Exception</code>'s <tt>getMessage</tt> with
   * our custom trace message.
   */
  @Override
public String getMessage() {
    if (cacheTrace == null) {
      cacheTrace = trace.toString();
    }
    return cacheTrace;
  }
}
