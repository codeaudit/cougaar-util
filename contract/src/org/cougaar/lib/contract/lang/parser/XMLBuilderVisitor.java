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
