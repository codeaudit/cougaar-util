#!/bin/csh

# <copyright>
#  Copyright 2001 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>


switch( $HOSTNAME )
case "alp-7":
	setenv CVS_RSH "/home/demo/bin/ssh.cvs"
 	setenv PATH /home/demo/bin:${PATH}
	breaksw
case "alp-39":
	setenv DEFAULT_NODE admin
	breaksw
case "alp-11":
	setenv DEFAULT_NODE 3IDCENTCOMNode
	breaksw
case "alp-19":
	setenv DEFAULT_NODE 3ID-1BDENode
	breaksw
case "alp-105":
	setenv DEFAULT_NODE 3ID-2BDENode
	breaksw
case "alp-103":
	setenv DEFAULT_NODE 3ID-3BDENode
	breaksw
case "alp-121":
	setenv DEFAULT_NODE 3ID-AVNBDENode
	setenv CVS_RSH "/home/demo/bin/ssh.cvs"
 	setenv PATH ~/bin:${PATH}
	breaksw
case "alp-17":
	setenv DEFAULT_NODE SupportNode
	breaksw
case "alp-37":
	setenv DEFAULT_NODE SupplyNode
	breaksw
case "alp-41":
	setenv DEFAULT_NODE Tops3AirGroundNode
	breaksw
case "alp-13":
	setenv DEFAULT_NODE Tops3PortsTranscapNode
	setenv CVS_RSH "/home/demo/bin/ssh.cvs"
 	setenv PATH ~/bin:${PATH}
	breaksw
case "alp-15":
	setenv DEFAULT_NODE Tops3SeaTheaterNode
	breaksw
case "alp-101":
	setenv CVS_RSH "/home/demo/bin/ssh.cvs"
 	setenv PATH ~/bin:${PATH}
	breaksw
case "alp-21":
	setenv DEFAULT_NODE 3ID-1BDENode
	breaksw
case "alp-25":
	setenv DEFAULT_NODE 3IDNode
	breaksw
case "alp-27":
	setenv DEFAULT_NODE 3ID-2BDENode
	breaksw
case "alp-29":
	setenv DEFAULT_NODE 3ID-3BDENode
	breaksw
case "alp-33":
	setenv DEFAULT_NODE admin
	breaksw
case "alp-35":
	setenv DEFAULT_NODE SupplyNode
	breaksw
case "alp-53":
	setenv DEFAULT_NODE SupportNode
	breaksw
case "alp-47":
	setenv DEFAULT_NODE CENTCOMNode
	breaksw
case "alp-55":
	setenv DEFAULT_NODE Tops3AirGroundNode
	breaksw
case "alp-57":
	setenv DEFAULT_NODE Tops3SeaTheaterNode
	breaksw
case "alp-59":
	setenv DEFAULT_NODE Tops3PortsTranscapNode
	breaksw
endsw
