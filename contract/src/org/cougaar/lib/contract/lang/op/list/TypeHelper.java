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

import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 **/
public final class TypeHelper {

  public static final int EXPECT_ARRAY       = 0;
  public static final int EXPECT_LIST        = 1;
  public static final int EXPECT_COLLECTION  = 2;
  public static final int EXPECT_ITERATOR    = 3;
  public static final int EXPECT_ENUMERATION = 4;
  public static final int EXPECT_UNKNOWN     = 5;

  public static final int getExpectedId(
      final OpParser p, 
      final boolean setToElementType) {
    // get expected list type
    TypeList sharedTypeList = p.getTypeList();
    List knownTypes = sharedTypeList.getKnownTypes();
    int nTypes = knownTypes.size();
    for (int i = 0; ; i++) {
      Type ti;
      if (i < nTypes) {
        // use next known type
        ti = (Type)knownTypes.get(i);
      } else if (i == nTypes) {
        // use assumed type
        ti = sharedTypeList.getWanted();
      } else {
        // no matching type found!
        return EXPECT_UNKNOWN;
      }
      if (!(ti.isNot())) {
        Class ticl = ti.getClazz();
        if (ticl.isArray()) {
          // elements are in Array
          if (setToElementType) {
            p.setTypeList(ticl.getComponentType());
          }
          return EXPECT_ARRAY;
        } else if (List.class.isAssignableFrom(ticl)) {
          // elements are in List
          if (setToElementType) {
            p.setTypeList(Object.class);
          }
          return EXPECT_LIST;
        } else if (Collection.class.isAssignableFrom(ticl)) {
          // elements are in a non-List Collection
          if (setToElementType) {
            p.setTypeList(Object.class);
          }
          return EXPECT_COLLECTION;
        } else if (Iterator.class.isAssignableFrom(ticl)) {
          // elements are in Iterator
          if (setToElementType) {
            p.setTypeList(Object.class);
          }
          return EXPECT_ITERATOR;
        } else if (Enumeration.class.isAssignableFrom(ticl)) {
          // elements are in Enumeration
          if (setToElementType) {
            p.setTypeList(Object.class);
          }
          return EXPECT_ENUMERATION;
        } else {
          // not a list
        }
      }
    }
  }
}
