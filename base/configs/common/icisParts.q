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
packagedPOLQuery=select PACKAGED_NSN from army_packaged_dcr_by_optempo where PACKAGED_NSN = :nsn
