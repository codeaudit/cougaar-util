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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * This fixes bugs in JOptionPane.showOptionDialog and
 * JOptionPane.showInputDialog that sizes the dialog too small when
 * using larger demo fonts. It fixes the workaround for bug#4135218 by
 * dynamically returning a preferred size based on the current
 * preferred size instead of statically computing a preferred size.
 * The static technique doesn't track UI changes.
 **/

public class OptionPane extends JOptionPane {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public OptionPane(Object message, int messageType, int optionType,
                      Icon icon, Object[] options, Object initialValue)
    {
        super(message, messageType, optionType, icon, options, initialValue);
    }

    private Dimension prevSize;
    private Dimension prevResult;

    @Override
   public Dimension getPreferredSize() {
        Dimension sz = super.getPreferredSize();
        if (!sz.equals(prevSize)) {
            prevSize = sz;
            prevResult = new Dimension(sz.width + 5, sz.height + 2);
        }
        return prevResult;
    }

    public static int showOptionDialog(Component parentComponent,
                                       Object message,
                                       String title,
                                       int optionType,
                                       int messageType,
                                       Icon icon,
                                       Object[] options,
                                       Object initialValue)
    {
        JOptionPane pane = new OptionPane(message, messageType,
                                          optionType, icon,
                                          options, initialValue);
        pane.setInitialValue(initialValue);

        JDialog dialog = pane.createDialog(parentComponent, title);

        pane.selectInitialValue();

        dialog.show();

        Object selectedValue = pane.getValue();

        if (selectedValue == null)
            return JOptionPane.CLOSED_OPTION;
        if(options == null) {
            if(selectedValue instanceof Integer)
                return ((Integer)selectedValue).intValue();
            return JOptionPane.CLOSED_OPTION;
        }
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return counter;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    public static Object showInputDialog(Component parentComponent,
                                         Object message,
                                         String title,
                                         int messageType,
                                         Icon icon,
                                         Object[] selectionValues,
                                         Object initialSelectionValue)
    {
        JOptionPane pane = new OptionPane(message,
                                          messageType,
                                          JOptionPane.OK_CANCEL_OPTION,
                                          icon,
                                          null,
                                          null);

        pane.setWantsInput(true);
        pane.setSelectionValues(selectionValues);
        pane.setInitialSelectionValue(initialSelectionValue);

        JDialog dialog = pane.createDialog(parentComponent, title);

        pane.selectInitialValue();
        dialog.pack();              // This makes the difference!
        dialog.show();

        Object value = pane.getInputValue();

        if (value == JOptionPane.UNINITIALIZED_VALUE)
            return null;
        return value;
    }
}
