package org.cougaar.tools.server.system.linux;

import org.cougaar.tools.server.system.SystemAccessFactory;
import org.cougaar.tools.server.system.ProcessLauncher;
import org.cougaar.tools.server.system.JavaThreadDumper;
import org.cougaar.tools.server.system.ProcessStatusReader;

/**
 * Linux-specific factory for system access.
 */
public class LinuxSystemAccessFactory 
extends SystemAccessFactory {

  private static final LinuxSystemAccessFactory SINGLETON =
    new LinuxSystemAccessFactory();

  private final LinuxProcessLauncher lpl;
  private final LinuxJavaThreadDumper ljtd;
  private final LinuxProcessStatusReader lpsr;

  /**
   * Obtain an instance of the Linux-specific factory.
   */
  public static LinuxSystemAccessFactory getLinuxInstance() {
    return SINGLETON;
  }

  /**
   * @see #getLinuxInstance()
   */
  private LinuxSystemAccessFactory() {
    // assert "os.name" == Linux?
    //
    // may need to check system configuration here!
    //   (e.g. "where is 'ps' installed?", etc)

    // for now these are singleton utilities -- in the
    //   future we may need to create an instance per
    //   use.
    lpl = new LinuxProcessLauncher();
    ljtd = new LinuxJavaThreadDumper();
    lpsr = new LinuxProcessStatusReader();
  }

  public ProcessLauncher createProcessLauncher() {
    return lpl;
  }

  public JavaThreadDumper createJavaThreadDumper() {
    return ljtd;
  }

  public ProcessStatusReader createProcessStatusReader() {
    return lpsr;
  }
}
