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

import org.cougaar.lib.contract.Operator;

import org.cougaar.lib.contract.lang.compare.*;

import org.cougaar.lib.contract.lang.parser.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Base (abstract) implementation of <code>Op</code>.
 * <p>
 * @see Op
 */
public abstract class OpImpl implements Op {

  public OpImpl() {}

  /**
   * Parser additions to <code>org.cougaar.lib.contract.Operator</code>
   */
  public abstract int getID();

  public abstract Op parse(final OpParser p) throws ParseException;

  /**
   * Most <code>Op</code>s return boolean.
   */
  public boolean isReturnBoolean() {
    return true;
  }

  /**
   * Most <code>Op</code>s return boolean.
   */
  public Class getReturnClass() {
    return Boolean.TYPE;
  }

  /** 
   * Subclass should redefine either <tt>operate</tt> and/or
   * <tt>execute</tt>!
   */
  public Object operate(final Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * Subclass should redefine either <tt>operate</tt> and/or
   * <tt>execute</tt>!
   */
  public boolean execute(final Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets a constant value in the <code>Op</code>, which is accessed
   * using "(get key)".
   * <p>
   * As an artifact of the tree structure, <code>Op</code> implementations
   * need to pass this method down to sub-<code>Op</code>s:
   * <code>
   *   for all Op fields f0..fn, f.setConst(key, val);
   * </code>
   * to react the GetOps, which actually uses the values.
   */
  public void setConst(final String key, final Object val) {
  }

  /**
   * <tt>allows</tt>(<code>Operator</code>) is computed by 
   * <code>org.cougaar.lib.contract.lang.compare.Allow</code>.
   */
  public boolean allows(final Operator oper) {
    //return Allow.compute(this, oper);
    return (Imply.compute(this, oper) ||
            Imply.compute(oper, this));
  }

  /**
   * <tt>implies</tt>(<code>Operator</code>) is computed by 
   * <code>org.cougaar.lib.contract.lang.compare.Imply</code>.
   */
  public boolean implies(final Operator oper) {
    return Imply.compute(this, oper);
  }

  /**
   * Equivalent to
   *   <tt>oper.implies(this)</tt>.
   */
  public boolean impliedBy(final Operator oper) {
    return Imply.compute(oper, this);
  }

  /**
   * <tt>equals</tt>(<code>Operator</code>) is computed by 
   * <code>org.cougaar.lib.contract.lang.compare.Equal</code>.
   */
  public boolean equals(final Operator oper) {
    return Equal.compute(this, oper);
  }

  public final boolean equals(final Object o) {
    return 
      ((o instanceof Operator) ?
       Equal.compute(this, (Operator)o) :
       false);
  }

  public final String toString() {
    return toString(DEFAULT_STYLE);
  }

  public final String toString(int style) {
    StringVisitor strVis;
    if ((style & PAREN_FLAG) != 0) {
      strVis = ParenParser.getStringVisitor();
    } else if ((style & XML_FLAG) != 0) {
      strVis = XMLParser.getStringVisitor();
    } else {
      throw new IllegalArgumentException(
        "Unknown Operator.toString style: "+style);
    }
    strVis.initialize();
    strVis.setVerbose((style & VERBOSE_FLAG) != 0);
    strVis.setPrettyPrint((style & PRETTY_FLAG) != 0);
    accept(strVis);
    strVis.visitEndOfTree();
    return strVis.toString();
  }

  public final Element getXML(
      Document doc,
      final int style) {
    XMLBuilderVisitor xmlVis = XMLParser.getXMLVisitor(doc);
    xmlVis.initialize();
    xmlVis.setVerbose((style & VERBOSE_FLAG) != 0);
    accept(xmlVis);
    xmlVis.visitEndOfTree();
    return xmlVis.getResult();
  }

  public final Element getXML(Document doc) {
    return getXML(doc, DEFAULT_STYLE);
  }

  public abstract void accept(TreeVisitor visitor);
}
