This directory contains XML file specification(s) for UltraLog 1AD
societies.

communities.xml
	This defines the 1AD logistics communities used by this
	configuration.

TINY-1AD-TRANS-STUB-1359.xml
	This defines a society consisting of a single node named
	"1AD_TINY" to run the Tiny 1AD society with the TRANSCOM-STUB
	functionality, including all four threads of supply. This
	configuration depends on the "refconfig" database, whose
	definition is supplied by the similarly-named zip file in
	csmart/data/database. If using this configuration, be sure to
	create such a database, adding that entry to your cougaar.rc
	file, and run the node using the XMLNode scripts.
	You will also need to either:
	a) edit configs/common/fdm_equip.q to point to the refconfig
	database, or
	b) Also create the CSMART configuration database, as the
	org.cougaar.configuration.database, as normal

