Database = ${org.cougaar.database}
Username = ${org.cougaar.database.user}
Password = ${org.cougaar.database.password}

# M915 Truck Tractor 6x4
pacing = '2320010284395'

# Next, get the MOS levels and generate an aggregate asset
%SQLAggregateAssetCreator
query = select CAPABILITY AS MOS_LEVEL, PERSONNEL AS MOS_QTY, 'Dummy Nomenclature' AS \
	"Dummy Nomenclature" from ORG_MOS where UIC = :uic

# Then, get the containers and generate an aggregate asset
%SQLAggregateAssetCreator
query = select '8115001682275' AS NSN, container_20_ft_qty AS QTY_OH, 'Container' AS NOMENCLATURE \
	from ue_summary_mtmc where uic = :uic

# Now, get 'eaches' for all pacing assets from fdm
%SQLAssetCreator
query = select NSN, QUANTITY, substr(MODEL_DESC,1,12)||'-'||substr(LIN_DESC,1,21) AS NOMENCLATURE \ 
	from fdm_vehicle where  UIC = :uic and NSN in (:pacing) and substr(NSN,1,1) != '0' \ 
	order by NSN

query.mysql = select NSN, QUANTITY, concat(substring(MODEL_DESC,1,12),'-',substring(LIN_DESC,1,21)) \
	AS NOMENCLATURE from fdm_vehicle where  UIC = :uic and NSN in (:pacing) and \
	substring(NSN,1,1) != '0' order by NSN

# Now, get all 'non-pacing' assets as aggregates from fdm
%SQLAggregateAssetCreator
query = select NSN, QUANTITY, substr(MODEL_DESC,1,12)||'-'||substr(LIN_DESC,1,21) AS NOMENCLATURE from \ 
	fdm_vehicle where  UIC = :uic and NSN not in (:pacing) and substr(NSN,1,1) != '0' \
	order by NSN	

query.mysql = select NSN, QUANTITY, concat(substring(MODEL_DESC,1,12),'-',substring(LIN_DESC,1,21)) \
	as NOMENCLATURE from fdm_vehicle where  UIC = :uic and NSN not in (:pacing) \ 
	and substring(NSN,1,1) != '0' order by NSN



