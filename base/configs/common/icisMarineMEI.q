Database = ${marine.database}
Username = ${marine.database.supply.user}
Password = ${marine.database.supply.password}
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
meiQuery=select NOMENCLATURE from AGGREGATED_MEI_NOMENCLATURE where MEI = :nsn and SERVICE = :service
# Marine
 
# ==>BulkPOL (Fuels)
BulkPOLMarineNSN=select T.NSN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from MARINE_GROUND_FUELS_DCR_BY_OP M, TAMCN_NSN_MAP T where M.TAMCN = T.TAMCN and NSN = :nsn order by GALLONS_PER_DAY
BulkPOLMarineTAMCN=select TAMCN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from MARINE_GROUND_FUELS_DCR_BY_OP where TAMCN = :nsn order by GALLONS_PER_DAY
BulkPOLMarineMDS=select T.NSN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from MARINE_AIR_FUELS_DCR_BY_OP M, TAMCN_NSN_MAP T  where M.MDS = T.TAMCN and NSN = :nsn order by GALLONS_PER_DAY

PackagedPOLMarineNSN=select MEI_NSN, PACKAGED_NSN, OPTEMPO, DCR from MARINE_PACKAGED_DCR_BY_OPTEMPO where MEI_NSN = :nsn order by DCR desc
PackagedPOLMarineMDS=select MEI_NSN, PACKAGED_NSN, OPTEMPO, DCR from MARINE_PACKAGED_DCR_BY_OPTEMPO where MEI_NSN = :nsn order by DCR desc

# ==>Ammunition
AmmunitionMarineNSN=select MEI_NSN, DODIC, OPTEMPO, TONS_PER_DAY from alp_mei_dodic_2_view where MEI_NSN = :nsn order by TONS_PER_DAY
AmmunitionMarineMDS=select MEI_NSN, DODIC, OPTEMPO, TONS_PER_DAY from alp_mei_dodic_2_view where MEI_NSN = :nsn order by TONS_PER_DAY

# ==>Consumable (Critical Spare Parts)
ConsumableMarineTAMCN=select TAMCN, PART_NSN, OPTEMPO, DCR from MCGRD_SPARES_DCR_BY_OPTEMPO where TAMCN = :nsn order by DCR
ConsumableMarineNSN=select MEI_NSN, PART_NSN, OPTEMPO, DCR from MCGRD_SPARES_DCR_BY_OPTEMPO where MEI_NSN = :nsn order by DCR
ConsumableMarineMDS=select MDS,NSN, OPTEMPO, DEMANDS_PER_DAY from USMCAIR_SPARES_DCR_BY_OPTEMPO where MDS = :nsn order by DEMANDS_PER_DAY
