/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
