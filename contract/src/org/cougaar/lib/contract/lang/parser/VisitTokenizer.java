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

import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * A generic tree tokenizer, used as a buffer from a <code>TreeVisitor</code>.
 */
public interface VisitTokenizer {

  /** 
   * Tokens.
   */
  public static final int TT_END = 0;
  public static final int TT_END_OF_TREE = 1;
  public static final int TT_WORD = 2;
  public static final int TT_CONSTANT = 3;

  /**
   * Move to the beginning.
   */
  public void rewind();

  /** 
   * Set the marker at the current point. 
   * @see #reset
   */
  public void mark();

  /** 
   * Move to the marker. 
   * @see #mark
   */
  public void reset();
  
  /**
   * @return a TT_* constant.
   */
  public int nextToken();

  /**
   * @return a TT_* constant.
   */
  public int previousToken();

  /**
   * Should only be called if the current token is <tt>TT_WORD</tt>.
   */
  public String getWord();

  /**
   * Should only be called if the current token is <tt>TT_CONSTANT</tt>.
   */
  public String getConstantType();

  /**
   * Should only be called if the current token is <tt>TT_CONSTANT</tt>.
   */
  public String getConstantValue();
}
