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

import java.awt.Font;

import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**
 * Extends DefaultMetalTheme to specify fonts appropriate for the demo
 * UI displays.
 *
 * @see javax.swing.plaf.metal.DefaultMetalTheme
 */
public class DemoMetalTheme extends DefaultMetalTheme {
  
  private final FontUIResource demoControlFont = 
    new FontUIResource("Dialog", Font.BOLD, 18); 

  private final FontUIResource demoSystemFont =  
    new FontUIResource("Dialog", Font.PLAIN, 18); 

  private final FontUIResource demoUserFont =  
    new FontUIResource("Dialog", Font.PLAIN, 18); 

  private final FontUIResource demoSmallFont = 
    new FontUIResource("Dialog", Font.PLAIN, 14); 

  public FontUIResource getControlTextFont() { return demoControlFont;} 
  public FontUIResource getSystemTextFont() { return demoSystemFont;} 
  public FontUIResource getUserTextFont() { return demoUserFont;} 
  public FontUIResource getMenuTextFont() { return demoControlFont;} 
  public FontUIResource getWindowTitleFont() { return demoControlFont;} 
  public FontUIResource getSubTextFont() { return demoSmallFont;} 
} 

