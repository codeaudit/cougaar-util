Database = ${icis.database}
Username = ${icis.database.user}
Password = ${icis.database.password}
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

headerQuery=select commodity, nsn, nomenclature, ui, ssc, price, icc, alt, plt, pcm, boq, diq, iaq, nso, qfd, rop, owrmrp, weight, cube, aac, slq from header where NSN = :nsn
assetsQuery=select nsn, ric, purpose, condition, iaq from assets where NSN = :nsn
nomen=select nomenclature from header where NSN = :nsn	
cost=select price from header where NSN = :nsn
volume=select cube from header where NSN = :nsn
weight=select weight from header where NSN = :nsn
classIXData=select nomenclature, ui, price, cube, weight from header where NSN = :nsn 
classIIIPackagedData=select nomenclature, ui, price, cube, weight from header where NSN = :nsn 
classVData=select nomenclature, weight, ccc from ammo_characteristics where DODIC = :nsn
ui=select ui from header where NSN = :nsn
# MEI
#
meiQuery=select NOMENCLATURE from aggregated_mei_nomenclature where MEI = :nsn and SERVICE = :service
# ARMY
#
ConsumableArmyNSN=select MEI_NSN, PART_NSN, OPTEMPO, DCR from army_spares_dcr_by_optempo where MEI_NSN = :nsn and OPTEMPO = 'HIGH' order by DCR desc
PackagedPOLArmyNSN=select MEI_NSN, PACKAGED_NSN, OPTEMPO, DCR from army_packaged_dcr_by_optempo where MEI_NSN = :nsn order by DCR desc
#BulkPOLArmyNSN=select NSN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from army_fuels_dcr_by_optempo where NSN = :nsn order by GALLONS_PER_DAY desc
BulkPOLArmyNSN=select NSN, FUEL_NSN, UCASE(OPTEMPO), GALLONS_PER_DAY from alp_mei_fuel where NSN = :nsn order by GALLONS_PER_DAY desc
AmmunitionArmyNSN=select MEI_NSN, DODIC, UCASE(OPTEMPO), TONS_PER_DAY from alp_mei_dodic_2_view where MEI_NSN = :nsn order by TONS_PER_DAY desc
# AirForce
#
ConsumableAirforceMDS=select MDS, NSN, OPTEMPO, DEMANDS_PER_DAY from airforce_spares_dcr_by_optempo where MDS = :nsn order by DEMANDS_PER_DAY
BulkPOLAirforceMDS=select MDS, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from airforce_fuels_dcr_by_optempo where MDS = :nsn order by GALLONS_PER_DAY
# Marine
# 
ConsumableMarineTAMCN=select TAMCN, PART_NSN, OPTEMPO, DCR from mcgrd_spares_dcr_by_optempo where TAMCN = :nsn order by DCR
ConsumableMarineNSN=select MEI_NSN, PART_NSN, OPTEMPO, DCR from mcgrd_spares_dcr_by_optempo where MEI_NSN = :nsn order by DCR
ConsumableMarineMDS=select MDS,NSN, OPTEMPO, DEMANDS_PER_DAY from usmcair_spares_dcr_by_optempo where MDS = :nsn order by DEMANDS_PER_DAY
BulkPOLMarineTAMCN=select TAMCN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from marine_ground_fuels_dcr_by_op where TAMCN = :nsn order by GALLONS_PER_DAY
BulkPOLMarineMDS=select MDS, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from marine_air_fuels_dcr_by_op where MDS = :nsn order by GALLONS_PER_DAY
# Navy
#
ConsumableNavyMEI=select MEI_ID, NSN, OPTEMPO, DCR from navy_spares_dcr_by_optempo where MEI_ID = :nsn order by DCR
ConsumableNavyMDS=select MDS, NSN, OPTEMPO, DEMANDS_PER_DAY from navyair_spares_dcr_by_optempo where MDS = :nsn order by DEMANDS_PER_DAY
# Prototype & Property Provider
#
#%org.cougaar.glm.ldm.GLMPrototypeProvider
#%org.cougaar.glm.ldm.GLMPropertyProvider