/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.tools.server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface ConfigurationWriter extends Serializable {
    void writeConfigFiles(File configDir) throws IOException;
}
