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

import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * Uses a <code>VisitTokenizer</code> which controls a
 * <code>TreeVisitor</code> to replay the visits.
 */
public class VisitReplayer {

  TreeVisitor toVis;
  VisitTokenizer fromTok;

  public VisitReplayer(TreeVisitor toVis, VisitTokenizer fromTok) {
    this.toVis = toVis;
    this.fromTok = fromTok;
  }

  public void replay() {
    replay(toVis, fromTok);
  }

  public static void replay(
      TreeVisitor toVisitor, 
      VisitTokenizer fromTokenizer) {
    toVisitor.initialize();
    fromTokenizer.mark();
    fromTokenizer.rewind();
    while (true) {
      switch (fromTokenizer.nextToken()) {
        case VisitTokenizer.TT_END:
          toVisitor.visitEnd();
          break;
        case VisitTokenizer.TT_END_OF_TREE:
          toVisitor.visitEndOfTree();
          fromTokenizer.reset();
          return;
        case VisitTokenizer.TT_WORD:
          toVisitor.visitWord(
            fromTokenizer.getWord());
          break;
        case VisitTokenizer.TT_CONSTANT:
          toVisitor.visitConstant(
              fromTokenizer.getConstantType(),
              fromTokenizer.getConstantValue());
          break;
      }
    }
  }

}
