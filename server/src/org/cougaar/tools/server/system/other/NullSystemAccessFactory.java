package org.cougaar.tools.server.system.other;

import org.cougaar.tools.server.system.SystemAccessFactory;
import org.cougaar.tools.server.system.ProcessLauncher;
import org.cougaar.tools.server.system.JavaThreadDumper;
import org.cougaar.tools.server.system.ProcessStatusReader;
import org.cougaar.tools.server.system.ProcessKiller;

/**
 * Stub <code>SystemAccessFactory<code> for Operating Systems
 * that are either:<pre>
 *   - unknown
 * or
 *   - don't support process-aware commands (Windows, etc)
 * </pre>
 */
public class NullSystemAccessFactory 
extends SystemAccessFactory {

  private static final NullSystemAccessFactory SINGLETON =
    new NullSystemAccessFactory();

  private final NullProcessLauncher npl;

  private NullSystemAccessFactory() {
    // see "getNullInstance()"
    npl = new NullProcessLauncher();
  }

  public static NullSystemAccessFactory getNullInstance() {
    return SINGLETON;
  }

  public ProcessLauncher createProcessLauncher() {
    return npl;
  }

  public JavaThreadDumper createJavaThreadDumper() {
    return null;
  }

  public ProcessStatusReader createProcessStatusReader() {
    return null;
  }

  public ProcessKiller createProcessKiller() {
    return null;
  }

}
