Driver = oracle.jdbc.driver.OracleDriver
Database = jdbc:oracle:thin:@${org.cougaar.database}
Username = ${org.cougaar.database.user}
Password = ${org.cougaar.database.password} 


# Next, get the MOS levels and generate an aggregate asset
%SQLAggregateAssetCreator
query = select CAPABILITY MOS_LEVEL, PERSONNEL MOS_QTY, 'Dummy Nomenclature' \
	from ORG_MOS \
	where ORG_NAME = :org_name
