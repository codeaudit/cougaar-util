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

package org.cougaar.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/** Utility for reading MSWindows-style .ini files.
 **/

public class INIParser {

  private boolean isVerbose = false;
  public boolean isVerbose() { return isVerbose; }
  public void setVerbose(boolean v) { isVerbose = v; }

  private char commentChar = '#';
  private String filename = "<stream>";
  public char getCommentChar() { return commentChar; }
  public void setCommentChar(char c) { commentChar = c; }

  public static class SlotHolder {
    /** List<Slot> **/
    private List slots = new ArrayList(5);
    public List getSlots() { return slots; }
    public void addSlot(Slot s) { slots.add(s); }
    public Slot getSlot(String name) {
      int ix = slots.indexOf(name);
      if (ix < 0)
	return null;
      return (Slot) slots.get(ix);
    }
  }

  public static class Group extends SlotHolder {
    private List sections = new ArrayList(5);

    public List getSections() { return sections; }
    public void addSection(Section s) { sections.add(s); }
  }

  public static class Section extends SlotHolder {
    /** name of Section **/
    private String name = null;
    public String getName() { return name; }
    
    /** List<String> **/
    private List parameters = new ArrayList();
    public List getParameters() { return parameters; }
    public void addParameter(String p) { parameters.add(p); }

    public Section(String name) { this.name = name; }

    public String toString() { return "Section "+name; }
  }

  public static class Slot {
    private String name;
    public String getName() { return name; }
    
    private List values = new ArrayList(1);
    public List getValues() { return values; }
    public String getValue() {
      if (values.size() < 1) return null;
      return (String) values.get(0);
    }
    public void setValues(List v) { values = v; }
    public void addValue(String v) { values.add(v); }

    public Slot(String name) { this.name = name; }
    public String toString() { return "Slot "+name; }
    public boolean equals(Object obj) {
      if (obj instanceof String) {
	return name.equals(obj);
      }
      if (obj instanceof Slot) {
	return name.equals(((Slot) obj).name);
      }
      return false;
    }
  }

  public Group parse(String filename) throws IOException {
    this.filename = filename;
    InputStream stream = null;
    
    if (filename.equals("-")) {
      if (isVerbose)
        System.err.println("Reading from standard input.");
      stream = new java.io.DataInputStream(System.in);
    } else {
      if (isVerbose)
        System.err.println("Reading \""+filename+"\".");
      stream = new FileInputStream(filename);
    }

    Group g = parse(stream);
    stream.close();
    return g;    
  }

  public Section parse(String filename, String section) throws IOException {
    this.filename = filename;
    InputStream stream = null;
    
    if (filename.equals("-")) {
      if (isVerbose)
        System.err.println("Reading from standard input.");
      stream = new java.io.DataInputStream(System.in);
    } else {
      if (isVerbose)
        System.err.println("Reading \""+filename+"\".");
      stream = new FileInputStream(filename);
    }

    Section s = parse(stream, section);
    stream.close();
    return s;    
  }


  public Section parse(InputStream in, String section) throws IOException {
    InputStreamReader isr = new InputStreamReader(in);
    BufferedReader br = new BufferedReader(isr);
    
    Group g = runParser(br);
    if (g != null) {
      for (Iterator it = g.getSections().iterator(); it.hasNext();) {
        Section s = (Section) it.next();
        if (section.equals(s.getName())) {
          return s;
        }
      }
    }

    return null;
  }

  public Group parse(InputStream in) throws IOException {
    InputStreamReader isr = new InputStreamReader(in);
    BufferedReader br = new BufferedReader(isr);
    return runParser(br);
  }

  private Group runParser(BufferedReader br) throws IOException {
    Group g = new Group();
    Section section = null;

    String line;
    int ln = 0;
    for (line = br.readLine(); line != null; line=br.readLine()) {
      int i, j;

      ln++;

        // ignore comments
      if ((i = line.indexOf(commentChar)) >= 0) 
        line = line.substring(0,i);

        // zap extra whitespace
      line = line.trim();

      // ignore empty lines
      if (line.length() <= 0)
        continue;

      int l;
      while (line.charAt((l=line.length())-1) == '\\') {
        line = line.substring(0,l-1) +
          br.readLine().trim();
        ln++;
      }

      if (line.charAt(0) == '[') {
        // classd line
        j = line.indexOf(']');
        String stuff = line.substring(1, j).trim();

        String[] v = stuff.split("\\s*");
          
        if (v.length < 1) {
          throw new RuntimeException("Empty Section at line "+ln);
        }

        section = new Section(v[0]);
        for (i=1; i<v.length; i++) {
          section.addParameter(v[i]);
        }

        g.addSection(section);
      } else if ( (j = line.indexOf('=')) >= 0) {
        // a param line

        String name = line.substring(0, j).trim();
        List v = CSVUtility.parseToList(line.substring(j+1));
          
	Slot slot = new Slot(name);
	slot.setValues(v);
        if (section == null) {
          g.addSlot(slot);
        } else {
	  section.addSlot(slot);
	}
      } else {
        System.err.println("Bad line " + ln + " in " + filename);
      }
    }
    return g;
  }
}
