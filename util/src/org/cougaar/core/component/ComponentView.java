/*
 *
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
package org.cougaar.core.component;

import java.util.Map;

/**
 * A {@link ViewService} view of a component, providing visibility
 * into the component's {@link ComponentDescription} and
 * advertised/obtained services.
 */
public interface ComponentView {

  /**
   * Get the unique identifier of this component, which can also be
   * used to create a component creation-order timeline.
   */
  int getId();

  /**
   * Get the time in milliseconds when the component was loaded.
   */
  long getTimestamp();

  /**
   * Get the {@link ComponentDescription}, which includes the
   * classname.
   */
  ComponentDescription getComponentDescription();

  /**
   * Get a view of the parent container of this component.
   */
  ContainerView getParentView();

  /**
   * Get a map of all services advertised by this component via {@link
   * ServiceBroker#addService}.
   * <p>
   * In the standard implementation this is an ordered {@link 
   * java.util.LinkedHashMap} by {@link ServiceView#getId}.
   *
   * @return a Map of {@link java.lang.Class}es to {@link
   * ServiceView}s.
   */ 
  Map getAdvertisedServices();

  /**
   * Get a map of obtained service classes {@link
   * ServiceBroker#getService} to information about that obtained
   * service.
   * <p> 
   * In the standard implementation this is an ordered {@link
   * java.util.LinkedHashMap} by {@link ServiceView#getId}.
   *
   * @return a Map of {@link java.lang.Class}es to {@link
   * ServiceView}s.
   */ 
  Map getObtainedServices();
}
