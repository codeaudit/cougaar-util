/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang.parser;

import java.io.*;
import java.util.*;

import org.cougaar.lib.contract.lang.*;

import com.ibm.xml.parser.*;
import org.w3c.dom.*;

public class XMLBuilderVisitor 
    implements TreeVisitor {

  private Document doc;
  private Node currNode;
  private boolean verbose;

  public XMLBuilderVisitor() {
    this(null);
  }

  public XMLBuilderVisitor(Document doc) {
    if (doc == null) {
      doc = new TXDocument();
    }
    this.doc = doc;
    currNode = doc;
  }

  public void initialize() {
    verbose = false;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void visitEndOfTree() {
  }

  public void visitEnd() {
    currNode = currNode.getParentNode();
  }

  public void visitWord(String w) {
    Element wordElem = doc.createElement(w);
    currNode.appendChild(wordElem);
    currNode = wordElem;
  }

  public void visitConstant(String type, String value) {
    Element constElem = doc.createElement("const");
    if (type != null) {
      constElem.setAttribute("type", type);
    }
    constElem.setAttribute("value", value);
    currNode.appendChild(constElem);
  }

  public void visitConstant(String value) {
    visitConstant(null, value);
  }

  public Element getResult() {
    // correct usage only adds a single Element child!
    return (Element)doc.getLastChild();
  }

}
