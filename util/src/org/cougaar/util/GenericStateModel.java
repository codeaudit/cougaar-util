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

/** GenericStateModel interface.
 *  This is the interface that defines state transitions for
 *  clusters, components and plugins.
 *
 * @author  ALPINE <alpine-software@bbn.com>
 *
 */

public interface GenericStateModel {

  /** UNINITIALIZED state - should never be returned by getModelState() **/
  int UNINITIALIZED = -1;
  /** initialized but not yet attached to an enclosing object **/
  int UNLOADED = 1;
  /** attached to a parent container **/
  int LOADED = 2;
  /** possibly doing work **/
  int ACTIVE = 3;
  /** forbidden from doing new work, but may be reactivated **/
  int IDLE = 4;

  /** Initialize.  Transition object from undefined to UNLOADED state.
   * Treat initialize() as an extended constructor.
   *  @exception org.cougaar.util.StateModelException Cannot transition to UNLOADED because initial state wasn't UNITIALIZED.
   **/

  void initialize() throws StateModelException;

  /**
   *  Object should transition to the LOADED state.
   * After initialize and before load, an object in notified about its
   * parents, services, etc.  After load, it should be ready to run (but not 
   * actually running). 
   *  @exception org.cougaar.util.StateModelException Cannot transition to LOADED because initial state wasn't UNLOADED.
   **/

  void load() throws StateModelException;

  /** Called object should start any threads it requires.
   *  Called object should transition to the ACTIVE state.
   *  @exception org.cougaar.util.StateModelException Cannot transition to ACTIVE because initial state wasn't LOADED.
   **/

  void start() throws StateModelException;

  /** Called object should pause operations in such a way that they may
   *  be cleanly resumed or the object can be unloaded.
   *  Called object should transition from the ACTIVE state to
   *  the IDLE state.
   *  @exception org.cougaar.util.StateModelException Cannot transition to IDLE because initial state wasn't ACTIVE.
   **/

  void suspend() throws StateModelException;

  /** Called object should transition from the IDLE state back to
   *  the ACTIVE state.
   *  @exception org.cougaar.util.StateModelException Cannot transition to ACTIVE because initial state wasn't IDLE.
   **/

  void resume() throws StateModelException;

  /** Called object should transition from the IDLE state
   *  to the LOADED state.
   *  @exception org.cougaar.util.StateModelException Cannot transition to LOADED because initial state wasn't IDLE.
   **/

  void stop() throws StateModelException;

  /**  Called object should transition from ACTIVE state
   *   to the LOADED state.
   *  @exception org.cougaar.util.StateModelException Cannot transition to LOADED because initial state wasn't ACTIVE.
   **/

  void halt() throws StateModelException;

  /** Called object should perform any cleanup operations and transition
   *  to the UNLOADED state.
   *  @exception org.cougaar.util.StateModelException Cannot transition to UNLOADED because initial state wasn't LOADED.
   **/

  void unload() throws StateModelException;

  /** Return the current state of the object: LOADED, UNLOADED, 
   * ACTIVE, or IDLE.
   * @return object state
   **/

  int getModelState();
}
 
