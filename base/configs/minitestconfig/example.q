# non live eiger jtav query
%SQLAssetPrototypeProvider
Assetclass = PhysicalAsset

%ECPropertyProvider
DB_NAME1 = jdbc:oracle:thin:@199.170.235.61:1521:alpdump
query1 = select substr(model_desc||' '||lin_desc,1,30) model_desc, \
	length, width, height, max_wgt, \
	sq_ft, cubic_ft, cgo_cat_cd \
	from equipment_characteristics \
  where nsn = (:nsns) \
	and lin_index=(select min(lin_index) \
  from equipment_characteristics \
  where nsn = (:nsns) )


%Global
DB_NAME1 = jdbc:oracle:thin:@199.170.235.61:1521:alpdump
query1 = select nsn, sum(qty_oh), nomenclature \
  from jtav_equipment \
  where substr(nsn,5,13) in (:niins) \
  and uic4 in (:uics) \
  group by nsn, nomenclature

  
#Tank: M1A1
%SQLAssetCreator
niins = '010871095'

#Tank: M1A1, Recovery Vehicle: M88A1, Helicopter: AH-64A, Howitzer: M109A6, Fighting Veh: M2A2 W/ODS, Fighting Veh: M3A2, Helicopters: UH-60A, EH-60A, OH-58D, HMMWV M998, HMMWV M985, HEMMT M978 WOWN (Tank), CommandPostCarrier: M57782, PersonnelCarrier: M113A3, TrackedPersonnelCarrier: M981, PLS Truck: M1075, PLS Trailer: M1076, PLS Flatrack: M1077, HET Tractor: M1070, HET Trailer: M1000
%SQLAggregateAssetCreator
niins = '011007672'