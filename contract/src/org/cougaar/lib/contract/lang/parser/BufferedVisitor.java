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

/**
 * A <code>TreeVisitor</code> that buffers the visits to an array and
 * creates a <code>VisitTokenizer</code>.
 */
public class BufferedVisitor implements TreeVisitor {

  protected static final int DEFAULT_SIZE = 11;

  protected boolean verbose = DEFAULT_VERBOSE;

  protected int ntokens;
  protected int[] tokens;
  protected int nwords;
  protected String[] words;

  public BufferedVisitor() { }

  public final boolean isVerbose() {
    return verbose;
  }

  public final void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public final void initialize() {
    initialize(DEFAULT_SIZE);
  }

  protected final void initialize(int size) {
    // create tokens array
    if ((tokens == null) || (tokens.length < size)) {
      tokens = new int[size];
    } else {
      // keep array, ignore tail tokens
    }
    // create words array
    if ((words == null) || (words.length < size)) {
      words = new String[size];
    } else {
      // keep array, GC Strings
      for (int i = (VisitTokenizer.TT_CONSTANT + 1); i < nwords; i++) {
        // memset!
        words[i] = null;
      }
    }
    ntokens = 0;
    nwords = VisitTokenizer.TT_CONSTANT + 1;
  }

  private final void ensureTokenSlot() {
    // based on ArrayList
    int oldCapacity = tokens.length;
    if ((ntokens+1) >= oldCapacity) {
      int[] oldData = tokens;
      int newCapacity = (oldCapacity * 3)/2 + 1;
      tokens = new int[newCapacity];
      System.arraycopy(oldData, 0, tokens, 0, ntokens);
    }
  }

  private final void addToken(int tok) {
    ensureTokenSlot();
    tokens[ntokens] = tok;
    ntokens++;
  }

  private void ensureWordSlot() {
    // based on ArrayList
    int oldCapacity = words.length;
    if ((nwords+1) >= oldCapacity) {
      String[] oldData = words;
      int newCapacity = (oldCapacity * 3)/2 + 1;
      words = new String[newCapacity];
      System.arraycopy(oldData, 0, words, 0, nwords);
    }
  }

  /** add END token <tt>VisitTokenizer.TT_END</tt> */
  public final void visitEnd() {
    addToken(VisitTokenizer.TT_END);
  }

  public final void visitWord(String word) {
    ensureWordSlot();
    words[nwords] = word;
    addToken(nwords);
    nwords++;
  }

  public final void visitConstant(String type, String value) {
    ensureWordSlot();
    words[nwords] = type;
    addToken(-(nwords));
    nwords++;
    ensureWordSlot();
    words[nwords] = value;
    nwords++;
  }

  /** Short for <tt>visitConstant("java.lang.String", value)</tt>. */
  public final void visitConstant(String value) {
    visitConstant(null, value);
  }

  public final void visitEndOfTree() {
    addToken(VisitTokenizer.TT_END_OF_TREE);
  }


  public VisitTokenizer getVisitTokenizer() {
    return new VisitTokenizer() {

      protected int cursor;
      protected String word; // also used as consttype
      protected String constval;

      protected int markCursor;

      public void rewind() {
        cursor = 0;
      }

      public void mark() {
        markCursor = cursor;
      }

      public void reset() {
        cursor = markCursor;
      }

      public int nextToken() {
        int tok = getToken();
        if (tok != VisitTokenizer.TT_END_OF_TREE) {
          cursor++;
        }
        return tok;
      }

      public int previousToken() {
        int tok = getToken();
        cursor--;
        return tok;
      }

      public String getWord() {
        return word;
      }

      public String getConstantType() {
        return word; // reused as consttype
      }

      public String getConstantValue() {
        return constval;
      }

      private int getToken() {
        int tok = tokens[cursor];
        switch (tok) {
          case VisitTokenizer.TT_END: 
            // END
            return VisitTokenizer.TT_END;
          case VisitTokenizer.TT_END_OF_TREE: 
            // END_OF_TREE
            return VisitTokenizer.TT_END_OF_TREE;
          default: 
            if (tok > VisitTokenizer.TT_CONSTANT) {
              word = words[tok];
              return VisitTokenizer.TT_WORD;
            } else if (tok < 0) {
              int i = -tok;
              word = words[i]; // reused as consttype
              constval = words[i+1];
              return VisitTokenizer.TT_CONSTANT;
            } else {
              // not possible?
              throw new RuntimeException("Illegal Parse State!: "+tok);
            }
        }
      }
    };
  }
}
