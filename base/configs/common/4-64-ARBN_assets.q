Database = ${org.cougaar.database}
Username = ${org.cougaar.database.user}
Password = ${org.cougaar.database.password} 

# First, get the personnel and generate an aggregate asset
%SQLAggregateAssetCreator
query = select 'Personnel' AS NSN, personnel AS QTY_OH, 'MilitaryPersonnel' AS NOMENCLATURE \
	from ue_summary_mtmc \
    	where uic = :uic

# Then, get the containers and generate an aggregate asset
%SQLAggregateAssetCreator
query = select '8115001682275' AS NSN, container_20_ft_qty AS QTY_OH, 'Container' AS NOMENCLATURE \
	from ue_summary_mtmc \
	where uic = :uic

# all other items are coming from tcaims - see tcaims.q
