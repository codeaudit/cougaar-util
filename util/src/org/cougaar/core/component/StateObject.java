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
 * An <code>Object</code> that contains internal state.
 * <p>
 * Component mobility and persistence requires the saving and
 * restoration of internal Component state.  The typical lifecycle
 * for both scenarios is:<ol>
 *    <li>The Component is asked for it's state</li>
 *    <li>The Component is destroyed</li>
 *    <li>A new instance of the Component is created from a 
 *        <code>ComponentDescription</code>.  The new location
 *        might be on a different host.</li>
 *    <li>The state is passed to the StateComponent<li>
 * </ol><br>
 * <p>
 * All <code>Container</code>s are StateComponents because (minimally)
 * they contain a tree of child Components.
 * <p>
 * <code>Binder</code>s act as proxies for the child state-saving.
 * <p>
 * This interface might be removed in the future and replaced
 * with reflective method-lookup, similar to <code>BindingUtility</code>.
 */
public interface StateObject
{

  /**
   * Get the current state of the Component that is sufficient to
   * reload the Component from a ComponentDescription.
   *
   * @return null if this Component currently has no state
   */
  Object getState();

  /**
   * Set-state is called by the parent Container if the state
   * is non-null.
   * <p>
   * The state Object is whatever this StateComponent provided
   * in it's <tt>getState()</tt> implementation.
   */
  void setState(Object o);

}
