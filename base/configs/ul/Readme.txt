This directory contains XML file specifications for Ultra*Log 1AD
societies.

As of Cougaar 10.2, there is some support for running Cougaar
societies from XML files. To do so, supply your society configuration
as XML, following the schema in core/configs/common/society.xsd
Then start your Node using one of the supplied XMLNode scripts in
/bin: in particular, specify the argument
-Dorg.cougaar.core.node.InitializationComponent=XML
which indicates the component intialization and community
initialization should come from XML
It also indicates that the OrgAsset (or Entity Asset) definitions
should come from the new "refconfig" database (a definition for which
supporting the 1AD societies comes in the csmart module).

Also, you must specify the name of the XML file from which to get the
society definition using the system property:
org.cougaar.society.file=<file name to be found using ConfigFinder>

To run this sample 1AD society, you must
1) Create a new database to be the "org.cougaar.refconfig" database
2) Add an entry for it in your cougaar.rc file
3) Load the file csmart/data/database/refconfigdb.zip (after
unzipping) into that database
4) Then run XMLNode from this directory, giving it the 2 arguments:
   TINY-1AD-TRANS-STUB-1359.xml 1AD_TINY
   indicating you want to use this XML file, and run the Node with that
   name (after extracting that file, or another society definition,
   from the ZIP file).

5) You will then likely need to run the GLSInit.[bat/sh] client to
   publish the Oplan, etc.

communities.xml
	This defines the 1AD logistics communities used by this
	configuration.

1ad-configs.zip
	ZIP file containing baseline 1AD military logistics society
	configurations -- the following 6 files:

TINY-1AD-TRANS-STUB-1359.xml
	This defines a society consisting of a single node named
	"1AD_TINY" to run the Tiny 1AD society with the TRANSCOM-STUB
	functionality, including all four threads of supply. 

TINY-1AD-TRANS-1359.xml
	This defines a society consisting of a single node named
	"1AD_TINY" to run the Tiny 1AD society with the full TRANSCOM
	functionality, including all four threads of supply. 

SMALL-1AD-TRANS-STUB-1359.xml
	This defines a society consisting of a single node named
	"SMALL_1AD" to run the Small 1AD society with the TRANSCOM-STUB
	functionality, including all four threads of supply. 

SMALL-1AD-TRANS-1359.xml
	This defines a society consisting of a single node named
	"SMALL_1AD" to run the Small 1AD society with the full TRANSCOM
	functionality, including all four threads of supply. 

FULL-1AD-TRANS-STUB-1359.xml
	This defines a society consisting of one node named
	"FULL_1AD" to run the 
	full 1AD society with the TRANSCOM-STUB
	functionality, including all four threads of supply. 
	Note that the node is configured to start on "localhost"

FULL-1AD-TRANS-1359.xml
	This defines a society consisting of a node named
	"FULL_1AD" to run the 
	full 1AD society with the full TRANSCOM
	functionality, including all four threads of supply. 
	Note that the node is configured to start on "localhost"


