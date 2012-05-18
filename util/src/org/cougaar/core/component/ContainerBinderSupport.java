/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
package org.cougaar.core.component;

import java.util.Collections;
import java.util.List;

/** 
 * A Shell implementation of a ContainerBinder based upon
 * BinderSupport.
 *
 * @see BinderSupport
 **/
public abstract class ContainerBinderSupport
extends BinderSupport 
implements ContainerBinder
{
  /** All subclasses must implement a matching constructor. **/
  public ContainerBinderSupport(BinderFactory bf, Object child) {
    super(bf, child);
  }

  public boolean add(Object o) {
    Object c = getComponent();
    if (c instanceof Container) {
      return ((Container)c).add(o);
    } else {
      throw new IncorrectInsertionPointException(
          "Component is not a container", c);
    }
  }

  public boolean remove(Object o) {
    Object c = getComponent();
    if (c instanceof Container) {
      return ((Container)c).remove(o);
    } else {
      return false;
    }
  }

  public boolean contains(Object o) {
    Object c = getComponent();
    if (c instanceof Container) {
      return ((Container)c).contains(o);
    } else {
      return false;
    }
  }

  public boolean isContainer() {
    Object c = getComponent();
    if (c instanceof ContainerBinder) {
      return ((ContainerBinder) c).isContainer();
    } else {
      return (c instanceof ContainerSupport);
    }
  }

  public List getChildViews() {
    Object c = getComponent();
    if (c instanceof ContainerBinder) {
      return ((ContainerBinder) c).getChildViews();
    } else if (c instanceof ContainerSupport) {
      return ((ContainerSupport) c).getChildViews();
    } else {
      return Collections.EMPTY_LIST;
    }
  }

  /**
   * @see BinderSupport#getBinderProxy() get the binding site
   */
  @Override
protected abstract BindingSite getBinderProxy();

  @Override
public String toString() {
    return getComponent()+"/ContainerBinder";
  }

}
