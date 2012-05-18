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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public abstract class MinMaxPanel extends JPanel {
  /**
    * 
    */
   private static final long serialVersionUID = 1L;
private JCheckBox enable = new JCheckBox();
  private JProgressBar progress = new JProgressBar() {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;

      @Override
      public Dimension getPreferredSize() {
        return new Dimension(100, super.getPreferredSize().height);
      }
    };

  private Spinner minSpinner = new Spinner(0, 1000, 1000);
  private Spinner maxSpinner = new Spinner(0, 1000, 1000);

  private ActionListener minListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      int min = minSpinner.getValue();
      int max = maxSpinner.getValue();
      if (min > max) {
        maxSpinner.setValue(min);
      }
      newMin(min);
    }
  };

  private ActionListener maxListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      int min = minSpinner.getValue();
      int max = maxSpinner.getValue();
      if (min > max) {
        minSpinner.setValue(max);
      }
      newMax(max);
    }
  };

  private ActionListener enableListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      newEnable(enable.isSelected());
    }
  };

  public MinMaxPanel() {
    super(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    add(enable, gbc);
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    add(minSpinner, gbc);
    add(maxSpinner, gbc);
    add(progress, gbc);
    minSpinner.addActionListener(minListener);
    maxSpinner.addActionListener(maxListener);
    enable.addActionListener(enableListener);
    progress.setStringPainted(true);
    progress.setBorder(BorderFactory.createLoweredBevelBorder());
  }

  public void setColumns(int cols) {
    minSpinner.setColumns(cols);
    maxSpinner.setColumns(cols);
  }

  public void setMin(int min) {
    minSpinner.setValue(min);
  }

  public void setMax(int max) {
    maxSpinner.setValue(max);
  }

  public void setProgressMax(int max) {
    progress.setMaximum(max);
  }

  public void setProgressValue(float completed) {
    int max = progress.getMaximum();
    progress.setValue((int) (max * completed));
    progress.setString(((int) (max * (1.0f - completed))) + "");
  }

  @Override
public void setEnabled(boolean b) {
    enable.setSelected(b);
  }

  @Override
public boolean isEnabled() {
    return enable.isSelected();
  }

  public void setText(String labelText) {
    enable.setText(labelText);
  }

  protected abstract void newMin(int min);
  protected abstract void newMax(int max);
  protected abstract void newEnable(boolean enable);
}
