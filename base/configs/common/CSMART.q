database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryAssemblyID = \
 SELECT ASSEMBLY_ID \
   FROM V3_ASB_ASSEMBLY \
  WHERE ASSEMBLY_TYPE = ':assembly_type'

queryAgentNames = \
 SELECT A.COMPONENT_NAME \
   FROM V3_ASB_COMPONENT A, \
        V3_LIB_COMPONENT B \
  WHERE A.ASSEMBLY_ID = ':assemblyMatch' \
    AND B.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND B.INSERTION_POINT = 'Node.AgentManager.Agent' 

queryAgentData = \
 SELECT A.COMPONENT_ID, COMPONENT_CATEGORY \
   FROM V3_ASB_COMPONENT A, \
        V3_LIB_COMPONENT B \
  WHERE A.ASSEMBLY_ID = ':assemblyMatch' \ 
    AND A.COMPONENT_NAME = ':component_name' \
    AND B.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND B.INSERTION_POINT = 'Node.AgentManager.Agent' 
