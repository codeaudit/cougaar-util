/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.lib.contract.lang.parser;

import java.io.*;
import java.util.*;

import org.cougaar.lib.contract.lang.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.apache.xerces.dom.DocumentImpl;

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
      doc = new DocumentImpl();
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
