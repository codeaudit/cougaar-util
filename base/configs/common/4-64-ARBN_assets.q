Driver = oracle.jdbc.driver.OracleDriver
Database = jdbc:oracle:thin:@${org.cougaar.database}
Username = ${org.cougaar.database.user}
Password = ${org.cougaar.database.password} 

# First, get the personnel and generate an aggregate asset
%SQLAggregateAssetCreator
query = select 'Personnel' NSN, personnel QTY_OH, 'MilitaryPersonnel' NOMENCLATURE \
	from ue_summary_mtmc \
    	where uic = :uic

# Then, get the containers and generate an aggregate asset
%SQLAggregateAssetCreator
query = select '8115001682275' NSN, container_20_ft_qty QTY_OH, 'Container' NOMENCLATURE \
	from ue_summary_mtmc \
	where uic = :uic

# all other items are coming from tcaims - see tcaims.q
