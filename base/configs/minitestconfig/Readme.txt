Readme for minitestconfig

Note that minitestconfig is old and deprecated, and likely to be
removed in the next major Cougaar release. See the pizza application
instaed.

MiniTestConfig is a simple 4 agent application used as a basic
plumbing test. It is useful to verify that your installation is
working, or that recent modifications you have made to
core/util/planning or glm have not seriously broken anything.

It is _not_ a good example of developing plugins or interfaces. It is
based largely on deprecated base classes and development patterns.

To run minitestconfig, use the Cougaar scripts, as in:

     $COUGAAR_INSTALL_PATH/bin/cougaar MiniNode.xml MiniNode

This runs the single node named "MiniNode" which is definied in
MiniNode.xml. You can split the agents across multiple Nodes by
editing this XML file. This is useful for a more complete test of the
infrastructure.

When you run, you should see something like:

/cougaar/B11_2/configs/minitestconfig $ cougaar.bat MiniNode.xml MiniNode

COUGAAR 11.2.2 built on Thu Nov 18 19:23:35 GMT 2004
Repository: B11_2 on Thu Nov 18 19:20:17 GMT 2004
VM: JDK 1.4.2_03-b02 (mixed mode)
OS: Windows 2000 (5.0)
2004-11-19 14:25:01,109 SHOUT [XMLComponentInitializerServiceProvider] - Initializing node
 "MiniNode" from XML file "MiniNode.xml"
2004-11-19 14:25:08,329 WARN  [ExecutionTimer] - Multi-node societies will have execution-
time clock skew: Set org.cougaar.core.society.startTime or society.timeOffset to avoid thi
s problem.
2004-11-19 14:25:08,329 WARN  [ExecutionTimer] - Starting Time set to Wed Aug 10 00:05:00
GMT 2005 offset=22757991671ms
2004-11-19 14:25:09,330 SHOUT [AssetInitializerServiceComponent] - Not using a database, i
nitializing solely from Files.
2004-11-19 02:25:11,293 SHOUT [DOTS] - +-+-+-+-+-.
.....

At this point, push the "Publish Oplan" button. Then push the "Send
GLS Root" button. You should see 20 succesful Allocations in the
Allocations window. You have a running Cougaar install!
