database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryAssemblyID = \
 SELECT ASSEMBLY_ID \
   FROM V3_ASB_ASSEMBLY \
  WHERE ASSEMBLY_TYPE = ':assembly_type'

queryAgentNames = \
 SELECT COMPONENT_NAME \
   FROM  V3_ASB_COMPONENT \
  WHERE ASSEMBLY_ID=':assemblyMatch' \
   AND COMPONENT_CATEGORY='agent'

queryAgentData = \
 SELECT COMPONENT_ID, COMPONENT_CATEGORY \
   FROM  V3_ASB_COMPONENT \
  WHERE ASSEMBLY_ID=':assemblyMatch' \
   AND COMPONENT_CATEGORY='agent'

