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
 * An generic tree visitor, customized for the contract language.
 */
 //
 // Note: 
 //  Might want to change the <tt>visitEnd()</tt> to something like
 //  <tt>visitEndWord(String)</tt>, since this would make String XML 
 //  generation easier.   The only downside is the extra memory in 
 //  saving the duplicate <code>String</code>s when tokenizing.
 //
public interface TreeVisitor {

  public static final boolean DEFAULT_VERBOSE = false;

  public void initialize();

  public boolean isVerbose();

  public void setVerbose(boolean verbose);

  public void visitEnd();

  public void visitWord(String word);

  /**
   * @param type if null, then "java.lang.String" is assumed
   */
  public void visitConstant(String type, String value);

  /** 
   * Short for <tt>visitConstant(null, value)</tt>. 
   */
  public void visitConstant(String value);

  public void visitEndOfTree();

}
