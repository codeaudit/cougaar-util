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
import org.cougaar.lib.contract.OperatorFactory;
import org.cougaar.lib.contract.lang.cache.ClassCache;
import org.cougaar.lib.contract.lang.parser.BufferedVisitor;
import org.cougaar.lib.contract.lang.parser.OpParserImpl;
import org.cougaar.lib.contract.lang.parser.ParenParser;
import org.cougaar.lib.contract.lang.parser.XMLParser;

/**
 * Implementation of <code>org.cougaar.lib.contract.OperatorFactory</code> -- used to hide
 * package dependency and some internals.
 * <p>
 * @see org.cougaar.lib.contract.OperatorFactory
 */
public class OperatorFactoryImpl extends OperatorFactory {

  public OperatorFactoryImpl() {
    // specify standard COUGAAR packages.
    //
    // This should probably be moved from a static configuration to a 
    // field of this factory!
    String[] packages = new String[7];
    packages[0] = "java.lang.";
    packages[1] = "java.util.";
    packages[2] = "org.cougaar.planning.ldm.plan.";
    packages[3] = "org.cougaar.planning.ldm.asset.";
    packages[4] = "org.cougaar.planning.ldm.measure.";
    packages[5] = "org.cougaar.glm.";
    packages[6] = "org.cougaar.glm.ldm.asset.";
    ClassCache.setPackages(packages);
  }

  @Override
public Operator create(
      final int style, 
      final Object inObj) throws Exception {
    try {
      // build tree tokenizer
      BufferedVisitor bufVis = new BufferedVisitor();
      if ((style & Operator.PAREN_FLAG) != 0) {
        ParenParser.parse(bufVis, inObj);
      } else if ((style & Operator.XML_FLAG) != 0) {
        XMLParser.parse(bufVis, inObj);
      } else {
        throw new IllegalArgumentException(
          "Unknown OpParser.parse style: "+style);
      }
      OpParserImpl opi = new OpParserImpl();
      opi.parse(bufVis.getVisitTokenizer());
      return opi.nextOp();
    } catch (ParseException pe) {
      // clear Java-stack and re-throw
      throw (ParseException)pe.fillInStackTrace();
    } catch (Exception e) {
      // create trace
      ParseException npe = new ParseException(e.getMessage());
      npe.addTrace("OperatorFactory.create()");
      throw npe;
    }
  }
}
