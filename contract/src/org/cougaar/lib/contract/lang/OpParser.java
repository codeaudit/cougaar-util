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
 * Interface which allows <code>Op</code>s to be parsed, provides their 
 * arguments, and allows type checking.
 */
public interface OpParser {

  /**
   * Take the next <tt>Op</tt> argument, returning <tt>null</tt> if the end 
   * of the arguments has been reached -- essentially a tokenizer for 
   * <tt>Op</tt>s.
   * <p>
   * For example, consider "(a (b) (c (d (e)) f)".  Remove all the "("s 
   * and replace the ")"s with <tt>null</tt> to yield:<br>
   *   "a b <tt>null</tt> c d e <tt>null</tt> <tt>null</tt> f <tt>null</tt>"<br>
   * This would be the result of many calls to <tt>nextOp</tt>, with any
   * further calls returning <tt>null</tt>.
   * <p>
   * Future: Maybe create real AST?  Currently can get away with simple
   * <tt>nextOp</tt> pattern.
   */
  public Op nextOp() throws ParseException;

  /**
   * @see org.cougaar.lib.contract.lang.TypeList
   */
  public void setTypeList(final TypeList tl);
  public void setTypeList(final Class cl);
  public int addType(final Type type);
  public TypeList getTypeList();
  public TypeList cloneTypeList();
}
