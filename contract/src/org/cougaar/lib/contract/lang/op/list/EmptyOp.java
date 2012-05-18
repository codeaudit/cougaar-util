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

package org.cougaar.lib.contract.lang.op.list;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "empty" <code>Op</code> -- returns true if List is null or
 * has size of zero.
 **/
public final class EmptyOp
    extends OpImpl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;

public static final Op singleInstance = new EmptyOp();

  public int expected_id;

  private EmptyOp() { }

  @Override
public final int getID() {
    return OpCodes.EMPTY_ID;
  }

  @Override
public final Op parse(final OpParser p) throws ParseException {
    // get expected list type
    expected_id = TypeHelper.getExpectedId(p, false);
    if (expected_id == TypeHelper.EXPECT_UNKNOWN) {
      throw new ParseException(
        "\""+OpCodes.EMPTY_NAME+"\" unable to determine Array/Collection "+
        "type from "+p.getTypeList());
    }

    Op u1 = p.nextOp();
    if (u1 != null) {
      throw new ParseException(
        "\""+OpCodes.EMPTY_NAME+"\" expecting zero arguments, but given "+
        u1.getClass().toString());
    }

    // (empty)
    return this;
  }

  @Override
public final boolean execute(final Object o) {
    if (o == null) {
      return true;
    } else {
      switch (expected_id) {
        case TypeHelper.EXPECT_ARRAY: 
          return (Array.getLength(o) <= 0);
        case TypeHelper.EXPECT_LIST:
          return (((List)o).size() <= 0);
        case TypeHelper.EXPECT_COLLECTION:
          return (((Collection)o).size() <= 0);
        case TypeHelper.EXPECT_ITERATOR:
          return (!(((Iterator)o).hasNext()));
        case TypeHelper.EXPECT_ENUMERATION:
          return (!(((Enumeration)o).hasMoreElements()));
        default:
          throw new InternalError(
            "\""+OpCodes.EMPTY_NAME+"\" invalid expected_id: "+expected_id);
      }
    }
  }
 
  @Override
public final void accept(TreeVisitor visitor) {
    // (empty)
    visitor.visitWord(OpCodes.EMPTY_NAME);
    visitor.visitEnd();
  }
}
