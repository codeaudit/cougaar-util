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

package org.cougaar.lib.contract.lang.parser;

import java.io.*;
import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * Implementation of <code>TreeVisitor</code> which will create a
 * "paren" semi-lisp style <tt>toString</tt>
 */
public class ParenStringVisitor implements StringVisitor {

  private StringBuffer sb;
  private boolean pretty;
  private int indent;
  private boolean verbose;

  public void initialize() {
    sb = new StringBuffer();
    this.pretty = false;
    indent = 0;
    verbose = DEFAULT_VERBOSE;
  }

  public final boolean isVerbose() {
    return verbose;
  }

  public final void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public final boolean isPrettyPrint() {
    return pretty;
  }

  public final void setPrettyPrint(boolean pretty) {
    this.pretty = pretty;
    this.indent = 0;
  }

  public final void visitEnd() {
    if (pretty) {
      --indent;
    }
    sb.append(")");
  }

  public final void visitWord(String word) {
    if (pretty) {
      sb.append(IndentFactory.createIndent(indent++));
    }
    sb.append("(").append(word);
  }

  public final void visitConstant(String type, String value) {
    if (pretty) {
      sb.append(IndentFactory.createIndent(indent));
    }
    if (type == null) {
      sb.append("\"").append(value).append("\"");
    } else {
      sb.append("(const \"").append(type);
      sb.append("\" \"").append(value).append("\")");
    }
  }

  /** Short for <tt>visitConstant("java.lang.String", value)</tt>. */
  public final void visitConstant(String value) {
    visitConstant(null, value);
  }

  public final void visitEndOfTree() {
  }

  public String toString() {
    return sb.toString();
  }
}
