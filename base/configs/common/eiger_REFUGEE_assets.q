Database = ${org.cougaar.database}
Username = ${org.cougaar.database.user}
Password = ${org.cougaar.database.password} 


# Next, get the MOS levels and generate an aggregate asset
%SQLAggregateAssetCreator
query = select CAPABILITY AS MOS_LEVEL, PERSONNEL AS MOS_QTY, 'Dummy Nomenclature' \
	AS "Dummy Nomenclature" from org_mos where ORG_NAME = :org_name
