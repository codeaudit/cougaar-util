A new directory, configs/small-135, has been included to allow some
small tests of this release.  Its name reflects the scope of the tests
it is designed to support: Classes 1, 3, and 5 (Subsistence, Fuel and
Ammunition) as well as Strategic Transportation.

This config directory includes a node file,
configs/small-135/SINGLE-FOOD-FUEL-AMMO.ini, which can be run on a
single machine. This node includes 22 clusters, which fall into the several logical groups.

Below is a listing of these groups and their primary functions in this society:

****************************************************

Top-level Command Orgs:
  NCA
  CENTCOM-HHC
  JTF-HHC

Divisional and Divisional-Support Orgs:
  3ID-HHC
  3-DISCOM-HHC
  703-MSB

Interim Brigade Combat Team (IBCT) Orgs:
  3-BDE-2ID-HHC
  1-23-INFBN
  296-SPTBN

2nd Brigade Orgs:
  2-BDE-3ID-HHC
  2-7-INFBN
  3-69-ARBN
  3-FSB

Combat Service Support Group Orgs:
  24-SPTGP-HHC
  24-CSB-HHD
  553-CSB-HHD
  10-TCBN-HHC
  89-TKCO-CGO
  110-QMCO-POLSPLY

CONUS Supply Orgs:
  DLAHQ
  IOC

Strategic Transportation Provider Org:
  TRANSCOM

****************************************************

Functions of the Orgs:

Top-level Command Orgs:

  The OPLAN and GetLogSupport tasks originate at NCA, and propagate down 
  to the other orgs

Divisional and Divisional-Support Orgs:

  3ID-HHC distributes the OPLAN and GetLogSUpport tasks to subordinates.

  Also it is the StrategicTransportationProvider for all of its
  subordinates. In turn, TRANSCOM is the StrategicTransportationProvider
  for 3ID-HHC. In this way, all StrategicTransport tasks go fromeach deploying
  organization, directly through 3ID-HHC, and then to TRANSCOM.

  703-MSB (Main Support Battalion) serves as an inventory manager for
  the lower-level Support Battalions (FSBs or Forward Support Battalions.
  3-DISCOM-HHC is the manager for703-MSB and passes the OPLAN and GetLogSUpport
  Task to 703-MSB.

Interim Brigade Combat Team (IBCT) Orgs

  This is a skeleton of the IBCT orgs.

  3-BDE-2ID-HHC is the HQ of the IBCT. It passes the OPLAN and GetLogSUpport
  Tasks to its subordinates.

  1-23-INFBN is the lone combat org in this IBCT subset. It generates demand for
  Fuel and Ammunition, based on its equipment (major end items). In the current
  test config, this does not work, as this org only has aggregate assets, and 
  by default, this demand is generated only for individual assets. This org does
  generate demand for Subsistence (Food).

  296-SPTBN is the support organization for the IBCT. In this limited config, it
  provides Food (and Fuel and Ammo) to the 1-23-INFBN

2nd Brigade Orgs:

  This is a skeleton of the 2nd BDE of the 3ID.

  2-BDE-3ID-HHC is the HQ of the 2nd BDE of the 3ID. It passes the OPLAN and GetLogSUpport
  Tasks to its subordinates.

  2-7-INFBN and 3-69-ARBN are the combat orgs. They generate demand for
  Fuel and Ammunition, based on their equipment (major end items), and for Subsistence
  based on their personnel.

  3-FSB is the support organization for the 2nd BDE. In this limited
  config, it provides Food (and Fuel and Ammo) to 2-7-INFBN and 3-69-ARBN.

Combat Service Support Group Orgs:

  This is a skeleton of the 24th Support Group.

  24-SPTGP-HHC is the HQ. It passes the OPLAN and GetLogSUpport
  Tasks to its subordinates.

  553-CSB-HHD is a major inventory manager for the society. In this subset,
  it is the SubsistenceSupplyProvider for 703-MSB.

  110-QMCO-POLSPLY is the Bulk POL (Fuel) and Packaged POL Supply provider
  for 703-MSB

  10-TCBN-HHC is another major inventory manager for the society. In this
  subset, it is the SubsistenceSupplyProvider for 553-CSB-HHD, and the
  Bulk POL (Fuel) and Packaged POL Supply provider for 110-QMCO-POLSPLY.

  89-TKCO-CGO is a theater transportation provider (MaterielTransportProvider) for
  10-TCBN-HHC. It transports Subsistence and Packaged POL. It is just a stub in this
  limited test society, and has a universal allocator for all Transport tasks

CONUS Supply Orgs:

  DLAHQ is a stub representing DLA. It has a universal allocator for all Supply tasks.
  It is the SubsistenceSupplyProvider, FuelSupplyProvider, PackagedPOLSupplyProvider
  for 10-TCBN-HHC.

  IOC is a stub representing Industrial Operations center. It is the AmmunitionSupply
  Provider for 3-FSB and 296-STPBN. It sends transport tasks for ammunition containers
  to TRANSCOM.

Strategic Transportation Provider Org:

  TRANSCOM is a stub representing USTRANSCOM. It has a universal allocator for
  all Transport tasks. It is the StrategicTransportationProvider for 3ID-HHC and
  for IOC.

****************************************************


To run the society:

  (1) Make sure that COUGAAR_INSTALL_PATH is set properly
      to the top  release directory

  (2) cd to $COUGAAR_INSTALL_PATH/configs/small-135/

  (3) type Node SINGLE-FOOD-FUEL-AMMO-NODE

****************************************************
