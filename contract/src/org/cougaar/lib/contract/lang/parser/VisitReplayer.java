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
