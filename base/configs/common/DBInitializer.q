# DBInitializer.q
# Used by core's Node Initialization code
# to read component definitions from the CSMART configuration
# database.
# See org.cougaar.core.node.DBInitializerServiceProvider
database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryGeolocLocation.database=${org.cougaar.database}
queryGeolocLocation.username=${org.cougaar.database.user}
queryGeolocLocation.password=${org.cougaar.database.password}

queryExperiment = \
 SELECT ASSEMBLY_ID \
   FROM v4_expt_trial_assembly \
  WHERE TRIAL_ID = ':trial_id:'

queryAgentPrototype = \
 SELECT LAO.AGENT_ORG_CLASS \
   FROM v4_alib_component AC, v4_lib_agent_org LAO, v4_asb_agent AA \
  WHERE AA.ASSEMBLY_ID :assemblyMatch: \
    AND AC.COMPONENT_LIB_ID = LAO.COMPONENT_LIB_ID \
    AND AA.COMPONENT_ALIB_ID = AC.COMPONENT_ALIB_ID \
    AND AC.COMPONENT_NAME = ':agent_name:'

queryComponents = \
 SELECT A.COMPONENT_NAME COMPONENT_NAME, C.COMPONENT_CLASS COMPONENT_CLASS, \
        A.COMPONENT_ALIB_ID COMPONENT_ID, C.INSERTION_POINT, H.INSERTION_ORDER INSERTION_ORDER \
   FROM v4_alib_component A, \
        v4_alib_component P, \
        v4_asb_component_hierarchy H, \
        v4_lib_component C \
  WHERE H.ASSEMBLY_ID :assemblyMatch: \
    AND A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
    AND P.COMPONENT_ALIB_ID = H.PARENT_COMPONENT_ALIB_ID \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND C.INSERTION_POINT like ':container_insertion_point:%' \
    AND INSTR(SUBSTR(C.INSERTION_POINT, LENGTH(':container_insertion_point:') + 1), '.') = 0 \
    AND P.COMPONENT_NAME = ':parent_name:' \
ORDER BY INSERTION_ORDER

# Separate query needed due to SUBSTRING
queryComponents.mysql = \
    SELECT A.COMPONENT_NAME COMPONENT_NAME, C.COMPONENT_CLASS COMPONENT_CLASS, \
           A.COMPONENT_ALIB_ID COMPONENT_ID, C.INSERTION_POINT, H.INSERTION_ORDER INSERTION_ORDER \
      FROM v4_alib_component P, \
	   v4_asb_component_hierarchy H, \
	   v4_alib_component A, \
	   v4_lib_component C \
     WHERE H.ASSEMBLY_ID :assemblyMatch: \
       AND A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
       AND P.COMPONENT_ALIB_ID = H.PARENT_COMPONENT_ALIB_ID \
       AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
       AND C.INSERTION_POINT LIKE ':container_insertion_point:%' \
       AND INSTR(SUBSTRING(C.INSERTION_POINT, LENGTH(':container_insertion_point:') + 1), '.') = 0 \
       AND P.COMPONENT_NAME = ':parent_name:' \
  ORDER BY INSERTION_ORDER

queryComponentParams = \
 SELECT ARGUMENT \
   FROM v4_asb_component_arg \
  WHERE ASSEMBLY_ID :assemblyMatch: \
    AND COMPONENT_ALIB_ID = ':component_id:' \
  ORDER BY ARGUMENT_ORDER, ARGUMENT

queryAgentPGNames = \
 SELECT distinct A.PG_NAME \
   FROM v4_asb_agent H, \
        v4_asb_agent_pg_attr B, \
        v4_lib_pg_attribute A \
  WHERE H.COMPONENT_ALIB_ID = B.COMPONENT_ALIB_ID \
    AND A.PG_ATTRIBUTE_LIB_ID = B.PG_ATTRIBUTE_LIB_ID \
    AND H.ASSEMBLY_ID :assemblyMatch: \
    AND B.ASSEMBLY_ID :assemblyMatch: \
    AND H.COMPONENT_NAME = ':agent_name:'

queryLibProperties = \
 SELECT ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE, \
        PG_ATTRIBUTE_LIB_ID \
   FROM v4_lib_pg_attribute \
  WHERE PG_NAME = ':pg_name:'

queryAgentProperties = \
 SELECT A.ATTRIBUTE_VALUE \
   FROM v4_asb_agent_pg_attr A, v4_alib_component B, v4_asb_agent H \
  WHERE A.ASSEMBLY_ID :assemblyMatch: \
    AND H.ASSEMBLY_ID :assemblyMatch: \
    AND A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
    AND B.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
    AND B.COMPONENT_NAME = ':agent_name:' \
    AND A.PG_ATTRIBUTE_LIB_ID = ':pg_attribute_id:'

queryAgentRelation.oracle = \
 SELECT ASB_REL.ROLE, SPTD.COMPONENT_NAME ITEM_IDENTIFICATION, \
        ASB_PG.ATTRIBUTE_VALUE TYPE_IDENTIFICATION, SPTD.COMPONENT_NAME SUPPORTED, \
        TO_CHAR(ASB_REL.start_date, 'MM/DD/YYYY HH:MI AM'), \
        TO_CHAR(ASB_REL.end_date, 'MM/DD/YYYY HH:MI AM') \
   FROM v4_asb_agent_relation ASB_REL, \
        v4_lib_pg_attribute LIB_PG, \
        v4_asb_agent_pg_attr ASB_PG, \
        v4_alib_component SPTD, \
        v4_alib_component SPTG \
  WHERE LIB_PG.PG_ATTRIBUTE_LIB_ID = ASB_PG.PG_ATTRIBUTE_LIB_ID \
    AND ASB_PG.COMPONENT_ALIB_ID = ASB_REL.SUPPORTED_COMPONENT_ALIB_ID \
    AND ASB_REL.SUPPORTING_COMPONENT_ALIB_ID = SPTG.COMPONENT_ALIB_ID \
    AND ASB_REL.SUPPORTED_COMPONENT_ALIB_ID = SPTD.COMPONENT_ALIB_ID \
    AND ASB_PG.ASSEMBLY_ID :assemblyMatch: \
    AND ASB_REL.ASSEMBLY_ID :assemblyMatch: \
    AND LIB_PG.PG_NAME = 'TypeIdentificationPG' \
    AND LIB_PG.ATTRIBUTE_NAME = 'TypeIdentification' \
    AND SPTG.COMPONENT_NAME = ':agent_name:'

queryAgentRelation.mysql = \
 SELECT ASB_REL.ROLE, SPTD.COMPONENT_NAME ITEM_IDENTIFICATION, \
        ASB_PG.ATTRIBUTE_VALUE TYPE_IDENTIFICATION, SPTD.COMPONENT_NAME SUPPORTED, \
        date_format(ASB_REL.start_date, '%m/%e/%Y %l:%i %p'), \
        date_format(ASB_REL.end_date, '%m/%e/%Y %l:%i %p') \
   FROM v4_asb_agent_relation ASB_REL, \
        v4_lib_pg_attribute LIB_PG, \
        v4_asb_agent_pg_attr ASB_PG, \
        v4_alib_component SPTD, \
        v4_alib_component SPTG \
  WHERE LIB_PG.PG_ATTRIBUTE_LIB_ID = ASB_PG.PG_ATTRIBUTE_LIB_ID \
    AND ASB_PG.COMPONENT_ALIB_ID = ASB_REL.SUPPORTED_COMPONENT_ALIB_ID \
    AND ASB_REL.SUPPORTING_COMPONENT_ALIB_ID = SPTG.COMPONENT_ALIB_ID \
    AND ASB_REL.SUPPORTED_COMPONENT_ALIB_ID = SPTD.COMPONENT_ALIB_ID \
    AND ASB_PG.ASSEMBLY_ID :assemblyMatch: \
    AND ASB_REL.ASSEMBLY_ID :assemblyMatch: \
    AND LIB_PG.PG_NAME = 'TypeIdentificationPG' \
    AND LIB_PG.ATTRIBUTE_NAME = 'TypeIdentification' \
    AND SPTG.COMPONENT_NAME = ':agent_name:'

# This query used for MilitaryOrgPG.HomeLocation when creating
# OrgAssets when running from the DB. This happens
# because in the lib_pg table in the DB this query name
# is listed.
# Sometimes units are listed as at 'fake' GEOLOCs, ALPLOCs
# So union with the ALPLOC table
queryGeolocLocation = \
 SELECT 'GeolocLocation', ':key:' \
      ||', InstallationTypeCode=' || INSTALLATION_TYPE_CODE \
      ||', CountryStateCode='     || COUNTRY_STATE_CODE \
      ||', CountryStateName='     || REPLACE(COUNTRY_STATE_LONG_NAME, ' ', '_') \
      ||', Name='                 || REPLACE(LOCATION_NAME, ' ', '_') \
      ||', Latitude=Latitude '    || LATITUDE || 'degrees' \
      ||', Longitude=Longitude '  || LONGITUDE || 'degrees' \
   FROM geoloc \
  WHERE GEOLOC_CODE = SUBSTR(':key:', 12) 

## Except the default org.cougaar.database does not have
# an ALPLOC table. Now, if you have installed everything
# from our one dump, you get the data. But for now
# you dont, so we dont do the union, and you better
# have a real GEOLOC
#      UNION \
# SELECT 'GeolocLocation', ':key:' \
#      ||', InstallationTypeCode=' \
#      ||', CountryStateCode='     \
#      ||', CountryStateName='     \
#      ||', Name='                 || REPLACE(LOCATION_NAME, ' ', '_') \
#      ||', Latitude=Latitude '    || LATITUDE || 'degrees' \
#      ||', Longitude=Longitude '  || LONGITUDE || 'degrees' \
#   FROM alploc \
#  WHERE ALPLOC_CODE = SUBSTR(':key:', 12)

# FIXME: This needs to be unioned with the ALPLOC table, like above, 
# but MySQL won't support that until v4 is out of alpha
queryGeolocLocation.mysql = \
 SELECT 'GeolocLocation', concat(':key:' \
      ,', InstallationTypeCode=' , INSTALLATION_TYPE_CODE \
      ,', CountryStateCode='     , COUNTRY_STATE_CODE \
      ,', CountryStateName='     , REPLACE(COUNTRY_STATE_LONG_NAME, ' ', '_') \
      ,', Name='                 , REPLACE(LOCATION_NAME, ' ', '_') \
      ,', Latitude=Latitude '    , LATITUDE , 'degrees' \
      ,', Longitude=Longitude '  , LONGITUDE , 'degrees') \
   FROM geoloc \
  WHERE GEOLOC_CODE = SUBSTRING(':key:', 12)
