database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryGeolocLocation.database=${org.cougaar.database}
queryGeolocLocation.username=${org.cougaar.database.user}
queryGeolocLocation.password=${org.cougaar.database.password}

queryExperiment = \
 SELECT ASSEMBLY_ID \
   FROM V4_EXPT_TRIAL_ASSEMBLY \
  WHERE TRIAL_ID = ':trial_id:'

queryAgentPrototype = \
 SELECT LAO.AGENT_ORG_CLASS \
   FROM V4_ALIB_COMPONENT AC, V4_LIB_AGENT_ORG LAO, V4_ASB_AGENT AA \
  WHERE AA.ASSEMBLY_ID :assemblyMatch: \
    AND AC.COMPONENT_LIB_ID = LAO.COMPONENT_LIB_ID \
    AND AA.COMPONENT_ALIB_ID = AC.COMPONENT_ALIB_ID \
    AND AC.COMPONENT_NAME = ':agent_name:'

queryAgentPrototype.mysql = \
 SELECT LAO.AGENT_ORG_CLASS \
   FROM V4_ALIB_COMPONENT AC \
  INNER JOIN V4_ASB_AGENT AA \
     ON AA.COMPONENT_ALIB_ID = AC.COMPONENT_ALIB_ID \
  INNER JOIN V4_LIB_AGENT_ORG LAO \
     ON AC.COMPONENT_LIB_ID = LAO.COMPONENT_LIB_ID \
  WHERE AA.ASSEMBLY_ID :assemblyMatch: \
    AND AC.COMPONENT_NAME = ':agent_name:'

queryComponents = \
 SELECT A.COMPONENT_NAME COMPONENT_NAME, C.COMPONENT_CLASS COMPONENT_CLASS, \
        A.COMPONENT_ALIB_ID COMPONENT_ID, C.INSERTION_POINT, H.INSERTION_ORDER INSERTION_ORDER \
   FROM V4_ALIB_COMPONENT A, \
        V4_ALIB_COMPONENT P, \
        V4_ASB_COMPONENT_HIERARCHY H, \
        V4_LIB_COMPONENT C \
  WHERE H.ASSEMBLY_ID :assemblyMatch: \
    AND A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
    AND P.COMPONENT_ALIB_ID = H.PARENT_COMPONENT_ALIB_ID \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND C.INSERTION_POINT like ':container_insertion_point:%' \
    AND INSTR(SUBSTR(C.INSERTION_POINT, LENGTH(':container_insertion_point:') + 1), '.') = 0 \
    AND P.COMPONENT_NAME = ':parent_name:' \
ORDER BY INSERTION_ORDER

queryComponents.mysql = \
    SELECT A.COMPONENT_NAME COMPONENT_NAME, C.COMPONENT_CLASS COMPONENT_CLASS, \
           A.COMPONENT_ALIB_ID COMPONENT_ID, C.INSERTION_POINT, H.INSERTION_ORDER INSERTION_ORDER \
      FROM V4_ALIB_COMPONENT P \
INNER JOIN V4_ASB_COMPONENT_HIERARCHY H \
        ON P.COMPONENT_ALIB_ID = H.PARENT_COMPONENT_ALIB_ID \
INNER JOIN V4_ALIB_COMPONENT A \
        ON A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
INNER JOIN V4_LIB_COMPONENT C \
        ON C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
     WHERE H.ASSEMBLY_ID :assemblyMatch: \
       AND C.INSERTION_POINT LIKE ':container_insertion_point:%' \
       AND INSTR(SUBSTRING(C.INSERTION_POINT, LENGTH(':container_insertion_point:') + 1), '.') = 0 \
       AND P.COMPONENT_NAME = ':parent_name:' \
  ORDER BY INSERTION_ORDER

queryComponentParams = \
 SELECT ARGUMENT \
   FROM V4_ASB_COMPONENT_ARG \
  WHERE ASSEMBLY_ID :assemblyMatch: \
    AND COMPONENT_ALIB_ID = ':component_id:' \
  ORDER BY ARGUMENT_ORDER

queryAgentPGNames = \
 SELECT distinct A.PG_NAME \
   FROM V4_ASB_AGENT H, \
        V4_ASB_AGENT_PG_ATTR B, \
        V4_LIB_PG_ATTRIBUTE A \
  WHERE H.COMPONENT_ALIB_ID = B.COMPONENT_ALIB_ID \
    AND A.PG_ATTRIBUTE_LIB_ID = B.PG_ATTRIBUTE_LIB_ID \
    AND H.ASSEMBLY_ID :assemblyMatch: \
    AND B.ASSEMBLY_ID :assemblyMatch: \
    AND H.COMPONENT_NAME = ':agent_name:'

queryAgentPGNames.mysql = \
    select distinct a.pg_name \
      from v4_asb_agent h \
inner join v4_asb_agent_pg_attr b \
        on h.component_alib_id = b.component_alib_id \
inner join v4_lib_pg_attribute a \
        on a.pg_attribute_lib_id = b.pg_attribute_lib_id \
     where b.assembly_id :assemblyMatch: \
       and h.assembly_id :assemblyMatch: \
       and h.component_name = ':agent_name:'

queryLibProperties = \
 SELECT ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE, \
        PG_ATTRIBUTE_LIB_ID \
   FROM V4_LIB_PG_ATTRIBUTE \
  WHERE PG_NAME = ':pg_name:'

queryAgentProperties = \
 SELECT A.ATTRIBUTE_VALUE \
   FROM V4_ASB_AGENT_PG_ATTR A, V4_ALIB_COMPONENT B, V4_ASB_AGENT H \
  WHERE A.ASSEMBLY_ID :assemblyMatch: \
    AND H.ASSEMBLY_ID :assemblyMatch: \
    AND A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
    AND B.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
    AND B.COMPONENT_NAME = ':agent_name:' \
    AND A.PG_ATTRIBUTE_LIB_ID = ':pg_attribute_id:'

queryAgentProperties.mysql = \
    select a.attribute_value \
      from v4_asb_agent h \
inner join v4_asb_agent_pg_attr a \
        on a.component_alib_id = h.component_alib_id \
inner join v4_alib_component b \
        on b.component_alib_id = h.component_alib_id \
     where a.assembly_id :assemblyMatch: \
       and h.assembly_id :assemblyMatch: \
       and b.component_name = ':agent_name:' \
       and a.pg_attribute_lib_id = ':pg_attribute_id:'

queryAgentRelation = \
 SELECT ASB_REL.ROLE, SPTD.COMPONENT_NAME ITEM_IDENTIFICATION, \
        ASB_PG.ATTRIBUTE_VALUE TYPE_IDENTIFICATION, SPTD.COMPONENT_NAME SUPPORTED, NULL, NULL \
   FROM V4_ASB_AGENT_RELATION ASB_REL, \
        V4_LIB_PG_ATTRIBUTE LIB_PG, \
        V4_ASB_AGENT_PG_ATTR ASB_PG, \
        V4_ALIB_COMPONENT SPTD, \
        V4_ALIB_COMPONENT SPTG \
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
 select asb_rel.role, sptd.component_name item_identification, \
        asb_pg.attribute_value type_identification, sptd.component_name supported, null, null \
   from v4_alib_component sptg \
inner join v4_asb_agent_relation asb_rel \
        on asb_rel.supporting_component_alib_id = sptg.component_alib_id \
inner join v4_asb_agent_pg_attr asb_pg \
        on asb_pg.component_alib_id = asb_rel.supported_component_alib_id \
inner join v4_lib_pg_attribute lib_pg \
        on lib_pg.pg_attribute_lib_id = asb_pg.pg_attribute_lib_id \
inner join v4_alib_component sptd \
        on asb_rel.supported_component_alib_id = sptd.component_alib_id \
  where asb_pg.assembly_id :assemblyMatch: \
    and asb_rel.assembly_id :assemblyMatch: \
    and lib_pg.pg_name = 'typeidentificationpg' \
    and lib_pg.attribute_name = 'typeidentification' \
    and sptg.component_name = ':agent_name:'

queryGeolocLocation = \
 SELECT 'GeolocLocation', ':key:' \
      ||', InstallationTypeCode=' || INSTALLATION_TYPE_CODE \
      ||', CountryStateCode='     || COUNTRY_STATE_CODE \
      ||', CountryStateName='     || REPLACE(COUNTRY_STATE_LONG_NAME, ' ', '_') \
      ||', Name='                 || REPLACE(LOCATION_NAME, ' ', '_') \
      ||', Latitude=Latitude '    || LATITUDE || 'degrees' \
      ||', Longitude=Longitude '  || LONGITUDE || 'degrees' \
   FROM GEOLOC \
  WHERE GEOLOC_CODE = SUBSTR(':key:', 12)

queryGeolocLocation.mysql = \
 SELECT 'GeolocLocation', concat(':key:' \
      ,', InstallationTypeCode=' , INSTALLATION_TYPE_CODE \
      ,', CountryStateCode='     , COUNTRY_STATE_CODE \
      ,', CountryStateName='     , REPLACE(COUNTRY_STATE_LONG_NAME, ' ', '_') \
      ,', Name='                 , REPLACE(LOCATION_NAME, ' ', '_') \
      ,', Latitude=Latitude '    , LATITUDE , 'degrees' \
      ,', Longitude=Longitude '  , LONGITUDE , 'degrees') \
   FROM GEOLOC \
  WHERE GEOLOC_CODE = SUBSTRING(':key:', 12)
