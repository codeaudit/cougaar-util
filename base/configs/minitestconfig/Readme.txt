Readme for minitestconfig

Note that old INI based configurations for minitestconfig have been
removed.

MiniTestConfig is a simple 4 agent application used as a basic
plumbing test. It is useful to verify that your installation is
working, or that recent modifications you have made to
core/util/planning or glm have not seriously broken anything.

It is _not_ a good example of developing plugins or interfaces. It is
based largely on deprecated base classes and development patterns.

To run minitestconfig, use the Cougaar scripts, as in:

     $COUGAAR_INSTALL_PATH/bin/Cougaar MiniNode.xml MiniNode

This runs the single node named "MiniNode" which is definied in
MiniNode.xml. You can split the agents across multiple Nodes by
editing this XML file. This is useful for a more complete test of the
infrastructure.


