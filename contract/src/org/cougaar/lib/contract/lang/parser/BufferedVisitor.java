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
