/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

/** 
 * A tuple containing a Component description and the Component's state.
 *
 * @see StateObject
 */
public final class StateTuple implements java.io.Serializable
{
  private final ComponentDescription cd;
  private final Object state;

  public StateTuple(
      ComponentDescription cd,
      Object state) {
    this.cd = cd;
    this.state = state;
    if (cd == null) {
      throw new IllegalArgumentException(
          "ComponentDescription is null");
    }
    // okay for state to be null
  }

  /**
   * Get the ComponentDescription.
   */
  public ComponentDescription getComponentDescription() {
    return cd;
  }

  /**
   * Get the state for the Component.
   * <p>
   * The state refers to the state of the Component specified
   * in the descriptor, including any child Components.
   */
  public Object getState() {
    return state;
  }

  public String toString() {
    return 
      "Tuple {"+cd+", "+
      ((state != null) ? 
       state.getClass().getName() : 
       "null")+
      "}";
  }
}
