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

import java.awt.Color;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

/**
 * ThemeFactory - Convenience class for establishing common look and feel
 * for the UI. Also defines a common set of colors for use in the UI.
 *
 * Can switch between font sizes by changing myCurrentMetalTheme and
 * recompiling. Top level demo UI - applet or main - should call
 * establishMetalTheme()
 *
 * @see javax.swing.plaf.metal.MetalTheme
 */
public class ThemeFactory {
  static private final Color myCougaarRed = new Color(255, 75, 75);
  static private final Color myCougaarYellow = new Color(250, 250, 25);
  static private final Color myCougaarGreen = new Color(50, 205, 50);

  static public final String COUGAAR_RED_KEY = "CougaarRed";
  static public final String COUGAAR_YELLOW_KEY = "CougaarYellow";
  static public final String COUGAAR_GREEN_KEY = "CougaarGreen";

  /**
   * Use for demo
   */
  static final MetalTheme myCurrentMetalTheme = new DemoMetalTheme();

  /**
   * Use for normal size fonts
   */
  //static final MetalTheme myCurrentMetalTheme = new DefaultMetalTheme();

  /**
   * getMetalTheme - returns current MetalTheme (determines default
   * colors and fonts.
   *
   * @return MetalTheme - current MetalTheme
   */
  static public MetalTheme getMetalTheme() {
    return myCurrentMetalTheme;
  }

  
  /**
   * establishMetalTheme - sets the look and feel. Involves setting
   * the current theme for MetalLookAndFeel and then installing 
   * MetalLookAndFeel as the UIManager default.
   *
   * @see javax.swing.plaf.metal.MetalLookAndFeel
   * @see javax.swing.UIManager#setLookAndFeel
   */
  static public void establishMetalTheme() {
    try {
      MetalLookAndFeel.setCurrentTheme(myCurrentMetalTheme);

      UIManager.setLookAndFeel(new MetalLookAndFeel());

      UIDefaults defaults = UIManager.getDefaults();
      defaults.put(COUGAAR_RED_KEY, getCougaarRed());
      defaults.put(COUGAAR_YELLOW_KEY, getCougaarYellow());
      defaults.put(COUGAAR_GREEN_KEY, getCougaarGreen());

      // BOZO for sun - List font is not surfaced in MetalLookAndFeel
      defaults.put("List.font", myCurrentMetalTheme.getUserTextFont());
    } catch (UnsupportedLookAndFeelException ulafe) {
      System.out.println("ThemeFactory.establishMetalTheme() - exception " +
                         ulafe);
      ulafe.printStackTrace();
    }
  }

  /**
   * getCougaarRed - returns the canonical COUGAAR red
   *
   * @return Color - COUGAAR red.
   */
  static public Color getCougaarRed() {
    return myCougaarRed;
  }

  /**
   * getCougaarYellow - returns the canonical COUGAAR yellow
   *
   * @return Color - COUGAAR yellow
   */
  static public Color getCougaarYellow() {
    return myCougaarYellow;
  }

  /**
   * getCougaarGreen - returns the canonical COUGAAR green
   *
   * @return Color - COUGAAR green.
   */
  static public Color getCougaarGreen() {
    return myCougaarGreen;
  }

}






