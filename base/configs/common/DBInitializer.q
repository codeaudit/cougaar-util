database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryExperiment = \
 SELECT ASSEMBLY_ID \
   FROM V3_EXPT_ASSEMBLY \
  WHERE EXPT_ID = ':expt_id'

queryAgentPrototype = \
 SELECT A.AGENT_ORG_PROTOTYPE \
   FROM V3_LIB_AGENT_ORG A, V3_ASB_COMPONENT B \
  WHERE B.ASSEMBLY_ID :assemblyMatch \
    AND B.COMPONENT_NAME = ':agent_name' \
    AND A.COMPONENT_LIB_ID = B.COMPONENT_LIB_ID

queryAgentNames = \
 SELECT A.COMPONENT_NAME \
   FROM V3_ASB_COMPONENT A, \
        V3_ASB_COMPONENT_NODE B, \
        V3_LIB_COMPONENT C \
  WHERE B.ASSEMBLY_ID :assemblyMatch \
    AND A.ASSEMBLY_ID :assemblyMatch \
    AND B.COMPONENT_ID = A.COMPONENT_ID \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND C.INSERTION_POINT = 'Node.AgentManager.Agent' \
    AND B.NODE_ID = ':node_name'

queryComponents = \
 SELECT A.COMPONENT_NAME, C.COMPONENT_CLASS, A.COMPONENT_ID \
   FROM V3_ASB_COMPONENT A, \
        V3_LIB_COMPONENT C, \
        V3_ASB_COMPONENT P \
  WHERE A.ASSEMBLY_ID :assemblyMatch \
    AND P.ASSEMBLY_ID :assemblyMatch \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND C.INSERTION_POINT = ':insertion_point' \
    AND A.PARENT_COMPONENT_ID = P.COMPONENT_ID \
    AND P.COMPONENT_NAME = ':parent_name' \
UNION ALL \
 SELECT A.COMPONENT_NAME, C.COMPONENT_CLASS, A.COMPONENT_ID \
   FROM V3_ASB_COMPONENT A, \
        V3_ASB_COMPONENT_NODE B, \
        V3_ASB_NODE D, \
        V3_LIB_COMPONENT C \
  WHERE B.ASSEMBLY_ID :assemblyMatch \
    AND A.ASSEMBLY_ID :assemblyMatch \
    AND D.ASSEMBLY_ID :assemblyMatch \
    AND B.COMPONENT_ID = A.COMPONENT_ID \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND B.NODE_ID = D.NODE_ID \
    AND C.INSERTION_POINT = ':insertion_point' \
    AND A.PARENT_COMPONENT_ID is NULL \
    AND D.NODE_NAME = ':parent_name'

queryComponentParams = \
 SELECT ARGUMENT \
   FROM V3_ASB_COMPONENT_ARG \
  WHERE ASSEMBLY_ID :assemblyMatch \
    AND COMPONENT_ID = ':component_id' \
  ORDER BY ARGUMENT_ORDER

queryPluginNames = \
 SELECT C.COMPONENT_CLASS, A.COMPONENT_ID \
   FROM V3_ASB_COMPONENT A, \
        V3_LIB_COMPONENT C \
  WHERE A.ASSEMBLY_ID :assemblyMatch \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND C.INSERTION_POINT = 'Node.AgentManager.Agent.PluginManager.Plugin' \
    AND A.PARENT_COMPONENT_ID = ':agent_name'

queryPluginParams = \
 SELECT ARGUMENT \
   FROM V3_ASB_COMPONENT_ARG \
  WHERE ASSEMBLY_ID :assemblyMatch \
    AND COMPONENT_ID = ':agent_component_id' \
  ORDER BY ARGUMENT_ORDER

queryAgentPGNames = \
 SELECT distinct A.PG_NAME \
   FROM V3_LIB_PG_ATTRIBUTE A, V3_ASB_AGENT_PG_ATTR B, V3_ASB_COMPONENT ASB_COMP \
  WHERE B.ASSEMBLY_ID :assemblyMatch \
    AND ASB_COMP.ASSEMBLY_ID :assemblyMatch \
    AND ASB_COMP.COMPONENT_ID = B.COMPONENT_ID \
    AND A.PG_ATTRIBUTE_LIB_ID = B.PG_ATTRIBUTE_LIB_ID \
    AND ASB_COMP.COMPONENT_NAME = ':agent_name'

queryLibProperties = \
 SELECT ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE, \
        PG_ATTRIBUTE_LIB_ID \
   FROM V3_LIB_PG_ATTRIBUTE \
  WHERE PG_NAME = ':pg_name'

queryAgentProperties = \
 SELECT A.ATTRIBUTE_VALUE \
   FROM V3_ASB_AGENT_PG_ATTR A, V3_ASB_COMPONENT B \
  WHERE A.ASSEMBLY_ID :assemblyMatch \
    AND B.ASSEMBLY_ID :assemblyMatch \
    AND A.COMPONENT_ID = B.COMPONENT_ID \
    AND B.COMPONENT_NAME = ':agent_name' \
    AND A.PG_ATTRIBUTE_lib_ID = ':pg_attribute_id'

queryAgentRelation = \
 SELECT ASB_REL.ROLE, SPTD.COMPONENT_NAME, \
        ASB_PG.ATTRIBUTE_VALUE, SPTD.COMPONENT_NAME, NULL, NULL \
   FROM V3_ASB_AGENT_RELATION ASB_REL, \
        V3_LIB_PG_ATTRIBUTE LIB_PG, \
        V3_ASB_AGENT_PG_ATTR ASB_PG, \
        V3_ASB_COMPONENT SPTD, \
        V3_ASB_COMPONENT SPTG \
  WHERE LIB_PG.PG_ATTRIBUTE_LIB_ID = ASB_PG.PG_ATTRIBUTE_LIB_ID \
    AND ASB_PG.COMPONENT_ID = ASB_REL.SUPPORTED_COMPONENT_ID \
    AND ASB_REL.SUPPORTING_COMPONENT_ID = SPTG.COMPONENT_ID \
    AND ASB_REL.SUPPORTED_COMPONENT_ID = SPTD.COMPONENT_ID \
    AND ASB_PG.ASSEMBLY_ID :assemblyMatch \
    AND ASB_REL.ASSEMBLY_ID :assemblyMatch \
    AND SPTD.ASSEMBLY_ID :assemblyMatch \
    AND SPTG.ASSEMBLY_ID :assemblyMatch \
    AND LIB_PG.PG_NAME = 'TypeIdentificationPG' \
    AND LIB_PG.ATTRIBUTE_NAME = 'TypeIdentification' \
    AND SPTG.COMPONENT_NAME = ':agent_name'

queryGeolocLocation = \
 SELECT 'GeolocLocation', ':key' \
      ||', InstallationTypeCode=' || INSTALLATION_TYPE_CODE \
      ||', CountryStateCode='     || COUNTRY_STATE_CODE \
      ||', CountryStateName='     || REPLACE(COUNTRY_STATE_LONG_NAME, ' ', '_') \
      ||', Name='                 || REPLACE(LOCATION_NAME, ' ', '_') \
      ||', Latitude=Latitude '    || LATITUDE || 'degrees' \
      ||', Longitude=Longitude '  || LONGITUDE || 'degrees' \
   FROM GEOLOC \
  WHERE GEOLOC_CODE = SUBSTR(':key', 12)
