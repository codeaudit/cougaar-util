database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryAssemblyID = \
 SELECT ASSEMBLY_ID, DESCRIPTION \
   FROM V3_ASB_ASSEMBLY \
  WHERE ASSEMBLY_TYPE = ':assembly_type'

queryAgentNames = \
 SELECT COMPONENT_NAME \
   FROM  V3_ASB_COMPONENT \
  WHERE ASSEMBLY_ID :assemblyMatch \
   AND COMPONENT_CATEGORY='agent'

queryAgentData = \
 SELECT COMPONENT_ID, COMPONENT_CATEGORY \
   FROM  V3_ASB_COMPONENT \
  WHERE ASSEMBLY_ID :assemblyMatch \
   AND COMPONENT_CATEGORY='agent'

queryNodes = \
 SELECT NODE_NAME \
   FROM V3_ASB_NODE \
  WHERE ASSEMBLY_ID :assemblyMatch

queryPluginNames = \
 SELECT C.COMPONENT_CLASS \
   FROM V3_ASB_COMPONENT A, \
        V3_LIB_COMPONENT C \
  WHERE A.ASSEMBLY_ID :assemblyMatch \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND COMPONENT_CATEGORY='plugin'

queryExptDescriptions = \
 SELECT A.EXPT_ID, A.DESCRIPTION \
   FROM V3_EXPT_EXPERIMENT A, V3_EXPT_TRIAL B \
  WHERE A.EXPT_ID = B.EXPT_ID

queryExperiment = \
 SELECT ASSEMBLY_ID \
   FROM V3_EXPT_TRIAL_ASSEMBLY \
  WHERE EXPT_ID = ':expt_id' \
    AND TRIAL_ID = ':trial_id'

queryTrials = \
 SELECT TRIAL_ID, DESCRIPTION \
   FROM V3_EXPT_TRIAL \
  WHERE EXPT_ID = ':expt_id'

