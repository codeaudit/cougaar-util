# PopulateDb.q
# Used by CSMART to populate the Configuration database, the assembly
# tables, with data for running the society.
# See org.cougaar.tools.csmart.core.db.PopulateDb
database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

# Get all assemblies used in running this trial
queryTrialAssemblies=\
 SELECT ASSEMBLY_ID FROM V4_EXPT_TRIAL_ASSEMBLY \
  WHERE TRIAL_ID = ':trial_id:'

# Get all assemblies used in configuring this Trial
queryConfigTrialAssemblies=\
 SELECT ASSEMBLY_ID FROM V4_EXPT_TRIAL_CONFIG_ASSEMBLY \
  WHERE TRIAL_ID = ':trial_id:'

# See if Assembly is used anywhere. Return nothing if not
checkUsedAssembly=\
 SELECT	'1' FROM V4_EXPT_TRIAL_ASSEMBLY R, \
   V4_EXPT_TRIAL_CONFIG_ASSEMBLY C \
   WHERE (C.ASSEMBLY_ID = :assembly_id: \
      and C.TRIAL_ID != ':trial_id:') \
      OR (R.ASSEMBLY_ID = :assembly_id: \
      and R.TRIAL_ID != ':trial_id:')

# Is this assembly in this trial's config
checkThisConfigUsesAssembly=\
 SELECT	'1' FROM \
   V4_EXPT_TRIAL_CONFIG_ASSEMBLY C \
   WHERE C.ASSEMBLY_ID = :assembly_id: \
      and C.TRIAL_ID = ':trial_id:' \

# Is this assembly in this trial's runtime
checkThisRuntimeUsesAssembly=\
 SELECT	'1' FROM \
   V4_EXPT_TRIAL_ASSEMBLY R \
   WHERE R.ASSEMBLY_ID = :assembly_id: \
      and R.TRIAL_ID = ':trial_id:' \

queryExptAlibComponents=\
 SELECT H.COMPONENT_ALIB_ID \
   FROM V4_ASB_COMPONENT_HIERARCHY H \
        V4_ALIB_COMPONENT C \
  WHERE H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID \
    AND H.ASSEMBLY_ID :assembly_match:

insertAlibComponent=\
 INSERT INTO V4_ALIB_COMPONENT \
    (COMPONENT_ALIB_ID, COMPONENT_NAME, \
     COMPONENT_LIB_ID, \
     COMPONENT_TYPE, \
     CLONE_SET_ID) \
 VALUES (:component_alib_id:, :component_name:, :component_lib_id:, :component_category:, 0)

updateAlibComponent=\
 UPDATE V4_ALIB_COMPONENT \n\
    SET COMPONENT_NAME = :component_name:, \n\
        COMPONENT_LIB_ID = :component_lib_id:, \n\
        COMPONENT_TYPE = :component_category: \n\
 WHERE COMPONENT_ALIB_ID = :component_alib_id:

checkAlibComponent=\
 SELECT COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID \
   FROM V4_ALIB_COMPONENT \
  WHERE COMPONENT_ALIB_ID = :component_alib_id:

checkLibComponent=\
 SELECT COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT \
   FROM V4_LIB_COMPONENT \
  WHERE COMPONENT_LIB_ID = :component_lib_id:

insertLibComponent=\
 INSERT INTO V4_LIB_COMPONENT \
    (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) \
 VALUES (:component_lib_id:, :component_category:, :component_class:, :insertion_point:, :description:)

updateLibComponent=\
 UPDATE V4_LIB_COMPONENT \n\
    SET COMPONENT_TYPE = :component_category:, \n\
        COMPONENT_CLASS = :component_class:, \n\
        INSERTION_POINT = :insertion_point: \n\
 WHERE COMPONENT_LIB_ID = :component_lib_id:

# Is this component already in the RUNTIME 
# configuration hierarchy for this Trial? Uses assembly_match
checkComponentHierarchy=\
 SELECT COMPONENT_ALIB_ID \
   FROM V4_ASB_COMPONENT_HIERARCHY \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND COMPONENT_ALIB_ID = :component_alib_id:

insertComponentHierarchy=\
 INSERT INTO V4_ASB_COMPONENT_HIERARCHY \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, \
     PARENT_COMPONENT_ALIB_ID, \
     INSERTION_ORDER) \
 VALUES (:assembly_id:, :component_alib_id:, :parent_component_alib_id:, :insertion_order:)

# Used in populate
queryComponentArgs=\
 SELECT ARGUMENT, ARGUMENT_ORDER \
   FROM V4_ASB_COMPONENT_ARG \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND COMPONENT_ALIB_ID = :component_alib_id: \
  ORDER BY ARGUMENT_ORDER, ARGUMENT
 
checkComponentArg=\
 SELECT COMPONENT_ALIB_ID \
   FROM V4_ASB_COMPONENT_ARG \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND COMPONENT_ALIB_ID = :component_alib_id: \
    AND ARGUMENT = :argument_value: \
    AND ARGUMENT_ORDER = :argument_order:
 
insertComponentArg=\
 INSERT INTO V4_ASB_COMPONENT_ARG \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
 VALUES (:assembly_id:, :component_alib_id:, :argument_value:, :argument_order:)

checkAgentOrg=\
 SELECT COMPONENT_LIB_ID \
   FROM V4_LIB_AGENT_ORG \
  WHERE COMPONENT_LIB_ID = :component_lib_id:

insertAgentOrg=\
 INSERT INTO V4_LIB_AGENT_ORG \
    (COMPONENT_LIB_ID, AGENT_LIB_NAME, AGENT_ORG_CLASS) \
 VALUES \
    (:component_lib_id:, :agent_lib_name:, :agent_org_class:)

# Is this relationship already in the RUNTIME configuration
# for this trial? Use assembly_match
checkRelationship=\
 SELECT '1' \
   FROM V4_ASB_AGENT_RELATION \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND ROLE = :role: \
    AND SUPPORTING_COMPONENT_ALIB_ID = :supporting: \
    AND SUPPORTED_COMPONENT_ALIB_ID = :supported: \
    AND START_DATE = :start_date:

insertRelationship=\
 INSERT INTO V4_ASB_AGENT_RELATION \
    (ASSEMBLY_ID, ROLE, \
     SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, \
     START_DATE, END_DATE) \
 VALUES \
    (:assembly_id:, :role:, :supporting:, :supported:, :start_date:, :end_date:)

# Add a new PG attribute/value set
insertAttribute=\
 INSERT INTO V4_ASB_AGENT_PG_ATTR \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, \
     ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, \
     START_DATE, END_DATE) \
 VALUES \
    (:assembly_id:, :component_alib_id:, :pg_attribute_lib_id:, \
     :attribute_value:, :attribute_order:, \
     :start_date:, :end_date:)

# Check that this PG attribute/value pair is not already
# in the DB (before trying to insert it)
checkAttribute=\
 SELECT '1' FROM V4_ASB_AGENT_PG_ATTR  \
   WHERE COMPONENT_ALIB_ID = :component_alib_id: AND  \
         PG_ATTRIBUTE_LIB_ID = :pg_attribute_lib_id: AND  \
         ATTRIBUTE_VALUE = :attribute_value: AND \
         ATTRIBUTE_ORDER = :attribute_order: AND \
         START_DATE = :start_date: AND \
         ASSEMBLY_ID :assembly_match:
 
# CMT assemblies use attribute_orders of zero
# So need to do some special casing in some situations
queryAttributeValueZeroOrder=\
 SELECT ATTRIBUTE_VALUE FROM V4_ASB_AGENT_PG_ATTR  \
   WHERE COMPONENT_ALIB_ID = :component_alib_id: AND  \
         PG_ATTRIBUTE_LIB_ID = :pg_attribute_lib_id: AND  \
         ATTRIBUTE_ORDER = '0' AND \
         START_DATE = :start_date: AND \
         ASSEMBLY_ID :assembly_match:

queryLibPGAttribute=\
 SELECT PG_NAME, ATTRIBUTE_NAME, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_TYPE, AGGREGATE_TYPE \
   FROM V4_LIB_PG_ATTRIBUTE \
  WHERE PG_NAME = ':pg_name:'

insertLibPGAttribute=\
 INSERT INTO V4_LIB_PG_ATTRIBUTE \
   (PG_ATTRIBUTE_LIB_ID, PG_NAME, ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE) \
  VALUES \
     (:attribute_lib_id:, :pg_name:, :attribute_name:, \
      :attribute_type:, :aggregate_type:)

queryMaxExptId=\
 SELECT MAX(EXPT_ID) \
   FROM V4_EXPT_EXPERIMENT \
  WHERE EXPT_ID LIKE ':max_id_pattern:'

queryExptCFWGroupId=\
 SELECT CFW_GROUP_ID \
   FROM V4_EXPT_EXPERIMENT \
  WHERE EXPT_ID LIKE ':expt_id:'

queryExptName=\
 SELECT NAME \
   FROM V4_EXPT_EXPERIMENT \
  WHERE EXPT_ID LIKE ':expt_id:'

# Used from Experiment via PopulateDb to avoid complete resaves
updateExptName=\
 UPDATE V4_EXPT_EXPERIMENT SET NAME = ':expt_name:' \
  WHERE EXPT_ID LIKE ':expt_id:'

insertExptId=\
 INSERT INTO V4_EXPT_EXPERIMENT (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) \
 VALUES (':expt_id:', ':description:', ':expt_name:', ':cfw_group_id:')

queryMaxTrialId=\
 SELECT MAX(TRIAL_ID) \
   FROM V4_EXPT_TRIAL \
  WHERE TRIAL_ID LIKE ':max_id_pattern:'

insertTrialId=\
 INSERT INTO V4_EXPT_TRIAL (EXPT_ID, TRIAL_ID, DESCRIPTION, NAME) \
 VALUES (':expt_id:', ':trial_id:', ':description:', ':trial_name:')

queryMaxAssemblyId=\
 SELECT MAX(ASSEMBLY_ID) \
   FROM V4_ASB_ASSEMBLY \
  WHERE ASSEMBLY_TYPE = ':assembly_type:' \
    AND ASSEMBLY_ID LIKE ':assembly_id_pattern:'

insertAssemblyId=\
 INSERT INTO V4_ASB_ASSEMBLY (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) \
 VALUES (':assembly_id:', ':assembly_type:', ':assembly_type: assembly')

# Used in PopulateDb.java when just added a CSA assembly
updateAssemblyDesc=\
 UPDATE V4_ASB_ASSEMBLY SET DESCRIPTION = ':soc_desc:' \
   WHERE ASSEMBLY_ID = ':assembly_id:'

# Used in PopulateDb to create new assembly with name of old
queryAssemblyDesc=\
 SELECT DESCRIPTION FROM V4_ASB_ASSEMBLY WHERE ASSEMBLY_ID = :assembly_id:

# Add new RUNTIME assembly
insertTrialAssembly=\
 INSERT INTO V4_EXPT_TRIAL_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 VALUES (':expt_id:', ':trial_id:', ':assembly_id:', ':assembly_type: assembly')

# Delete runtime assembly from trial
cleanTrialAssembly=\
 DELETE FROM V4_EXPT_TRIAL_ASSEMBLY \
  WHERE EXPT_ID = ':expt_id:' \
    AND TRIAL_ID = ':trial_id:' \
    AND ASSEMBLY_ID IN :assemblies_to_clean:

# Add new CONFIG assembly to the trial
insertTrialConfigAssembly=\
 INSERT INTO V4_EXPT_TRIAL_CONFIG_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 VALUES (':expt_id:', ':trial_id:', :assembly_id:, ':assembly_type: assembly')

# Delete config time assembly from trial
cleanTrialConfigAssembly=\
 DELETE FROM V4_EXPT_TRIAL_CONFIG_ASSEMBLY \
  WHERE EXPT_ID = ':expt_id:' \
    AND TRIAL_ID = ':trial_id:' \
    AND ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAssembly=\
 DELETE FROM V4_ASB_ASSEMBLY \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBComponentArg=\
 DELETE FROM V4_ASB_COMPONENT_ARG \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBComponentHierarchy=\
 DELETE FROM V4_ASB_COMPONENT_HIERARCHY \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAgent=\
 DELETE FROM V4_ASB_AGENT \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAgentPGAttr=\
 DELETE FROM V4_ASB_AGENT_PG_ATTR \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAgentRel=\
 DELETE FROM V4_ASB_AGENT_RELATION \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBOplan=\
 DELETE FROM V4_ASB_OPLAN \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBOplanAAttr=\
 DELETE FROM V4_ASB_OPLAN_AGENT_ATTR \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

# Get list of non CMT assemblies in RUNTIME configuration
queryAssembliesToClean=\
 SELECT AA.ASSEMBLY_ID \
   FROM V4_ASB_ASSEMBLY AA, V4_EXPT_TRIAL_ASSEMBLY ETA \
  WHERE AA.ASSEMBLY_ID = ETA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':trial_id:' \
    AND AA.ASSEMBLY_TYPE != ':cmt_type:'

# Get all the CSMI and CSHNA assemblies
# Used to find orphaned assemblies
queryNonSocietyAssemblies=\
 SELECT ASSEMBLY_ID \
   FROM V4_ASB_ASSEMBLY \
 WHERE ASSEMBLY_TYPE NOT IN (':cmt_type:', ':csa_type:')

# Get list of config assemblies of either of 2 types in this trial
# Used to make sure that only most recent society definition included
queryOldSocietyConfigAssembliesToClean=\
 SELECT ETC.ASSEMBLY_ID \
   FROM V4_EXPT_TRIAL_CONFIG_ASSEMBLY ETC, V4_ASB_ASSEMBLY AA \
  WHERE ETC.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND AA.ASSEMBLY_TYPE IN (':cmt_type:', ':csa_type:') \
    AND ETC.TRIAL_ID = ':trial_id:'

# Get list of runtime assemblies of either of 2 types in this trial
# Used to make sure that only most recent society definition included
queryOldSocietyRuntimeAssembliesToClean=\
 SELECT ETC.ASSEMBLY_ID \
   FROM V4_EXPT_TRIAL_ASSEMBLY ETC, V4_ASB_ASSEMBLY AA \
  WHERE ETC.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND AA.ASSEMBLY_TYPE IN (':cmt_type:', ':csa_type:') \
    AND ETC.TRIAL_ID = ':trial_id:'

copyCMTAssembliesQueryNames=copyCMTAssemblies

# Copy RUNTIME CMT assemblies from old trial to new
copyCMTAssemblies=\
 INSERT INTO V4_EXPT_TRIAL_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT ':expt_id:', ':new_trial_id:', ETA.ASSEMBLY_ID, ETA.DESCRIPTION \
   FROM V4_EXPT_TRIAL_ASSEMBLY ETA, V4_ASB_ASSEMBLY AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTAssembliesQueryNames.mysql=copyCMTAssemblies1 copyCMTAssemblies2 copyCMTAssemblies3

# Copy RUNTIME CMT assemblies from old trial to new
copyCMTAssemblies1=\
 CREATE TEMPORARY TABLE `TMP_CMTA_:expt_id:` AS \
 SELECT ':expt_id:' as EXPT_ID, \
        ':new_trial_id:' as TRIAL_ID, \
        ETA.ASSEMBLY_ID as ASSEMBLY_ID, \
        ETA.DESCRIPTION as DESCRIPTION \
   FROM V4_EXPT_TRIAL_ASSEMBLY ETA, V4_ASB_ASSEMBLY AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTAssemblies2=\
 INSERT INTO V4_EXPT_TRIAL_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION \
   FROM `TMP_CMTA_:expt_id:`

copyCMTAssemblies3=DROP TABLE `TMP_CMTA_:expt_id:`

copyCMTConfigAssembliesQueryNames=copyCMTConfigAssemblies

# Copy CONFIG CMT assemblies from old trial to new
copyCMTConfigAssemblies=\
 INSERT INTO V4_EXPT_TRIAL_CONFIG_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT ':expt_id:', ':new_trial_id:', ETA.ASSEMBLY_ID, ETA.DESCRIPTION \
   FROM V4_EXPT_TRIAL_CONFIG_ASSEMBLY ETA, V4_ASB_ASSEMBLY AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTConfigAssembliesQueryNames.mysql=copyCMTConfigAssemblies1 copyCMTConfigAssemblies2 copyCMTConfigAssemblies3

# Copy CONFIG CMT assemblies from old trial to new
copyCMTConfigAssemblies1=\
 CREATE TEMPORARY TABLE `TMP_CMTA_:expt_id:` AS \
 SELECT ':expt_id:' as EXPT_ID, \
        ':new_trial_id:' as TRIAL_ID, \
        ETA.ASSEMBLY_ID as ASSEMBLY_ID, \
        ETA.DESCRIPTION as DESCRIPTION \
   FROM V4_EXPT_TRIAL_CONFIG_ASSEMBLY ETA, V4_ASB_ASSEMBLY AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTConfigAssemblies2=\
 INSERT INTO V4_EXPT_TRIAL_CONFIG_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION \
   FROM `TMP_CMTA_:expt_id:`

copyCMTConfigAssemblies3=DROP TABLE `TMP_CMTA_:expt_id:`

# Copy the CMT Threads from one Trial to another
copyCMTThreadsQueryNames.oracle=copyCMTThreads

copyCMTThreads=\
 INSERT INTO V4_EXPT_TRIAL_THREAD (EXPT_ID, TRIAL_ID, THREAD_ID) \
 SELECT ':expt_id:', ':new_trial_id:', THREAD_ID \
   FROM V4_EXPT_TRIAL_THREAD \
  WHERE TRIAL_ID = ':old_trial_id:'

copyCMTThreadsQueryNames.mysql=copyCMTThreads1 copyCMTThreads2 copyCMTThreads3

copyCMTThreads1=\
 CREATE TEMPORARY TABLE `TMP_CMTT_:expt_id:` AS \
 select THREAD_ID \
   FROM V4_EXPT_TRIAL_THREAD \
  WHERE TRIAL_ID = ':old_trial_id:'

copyCMTThreads2=\
 INSERT INTO V4_EXPT_TRIAL_THREAD (EXPT_ID, TRIAL_ID, THREAD_ID) \
 SELECT ':expt_id:', ':new_trial_id:', THREAD_ID \
   FROM `TMP_CMTT_:expt_id:`

copyCMTThreads3=DROP TABLE `TMP_CMTT_:expt_id:`

copyOPLANQueryNames.oracle=copyOPLANEntry copyOPLANAttrEntries

copyOPLANEntry=\
 INSERT INTO V4_ASB_OPLAN (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE) \
 SELECT ':new_assembly_id:', OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE \
	FROM V4_ASB_OPLAN \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANAttrEntries=\
 INSERT INTO V4_ASB_OPLAN_AGENT_ATTR (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE) \
   SELECT ':new_assembly_id:', OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE) \
       FROM V4_ASB_OPLAN_AGENT_ATTR \
    WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANQueryNames.mysql=copyOPLANEntry1 copyOPLANEntry2 copyOPLANEntry3 copyOPLANAttrEntries1 copyOPLANAttrEntries2 copyOPLANAttrEntries3

copyOPLANEntry1=\
 CREATE TEMPORARY TABLE `TMP_OPE_:old_assembly_id:` AS \
   SELECT OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE \
    FROM V4_ASB_OPLAN \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANEntry2=\
 INSERT INTO V4_ASB_OPLAN (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE) \
  SELECT ':new_assembly_id:', OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE \
   FROM `TMP_OPE_:old_assembly_id:`

copyOPLANEntry3=DROP TABLE `TMP_OPE_:old_assembly_id:`

copyOPLANAttrEntries1=\
 CREATE TEMPORARY TABLE `TMP_OPAE_:old_assembly_id:` AS \
   SELECT OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE \
  FROM V4_ASB_OPLAN_AGENT_ATTR \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANAttrEntries2=\
 INSERT INTO V4_ASB_OPLAN_AGENT_ATTR (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE) \
   SELECT ':new_assembly_id:', OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE \
    FROM `TMP_OPAE_:old_assembly_id:`

copyOPLANAttrEntries3=DROP TABLE `TMP_OPAE_:old_assembly_id:`

queryLibRecipeByName=\
 SELECT MOD_RECIPE_LIB_ID, JAVA_CLASS \
   FROM V4_LIB_MOD_RECIPE \
  WHERE NAME = ':recipe_name:'

queryLibRecipeProps=\
 SELECT ARG_NAME, ARG_VALUE \
   FROM V4_LIB_MOD_RECIPE_ARG \
  WHERE MOD_RECIPE_LIB_ID = ':recipe_id:' \
  ORDER BY ARG_ORDER

queryMaxRecipeId=\
 SELECT MAX(MOD_RECIPE_LIB_ID) \
   FROM V4_LIB_MOD_RECIPE \
  WHERE MOD_RECIPE_LIB_ID LIKE ':max_id_pattern:'

insertLibRecipe=\
 INSERT INTO V4_LIB_MOD_RECIPE \
    (MOD_RECIPE_LIB_ID, NAME, JAVA_CLASS, DESCRIPTION) \
 VALUES (':recipe_id:', ':recipe_name:', ':java_class:', ':description:')

insertLibRecipeProp=\
 INSERT INTO V4_LIB_MOD_RECIPE_ARG \
    (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_VALUE, ARG_ORDER) \
 VALUES (':recipe_id:', ':arg_name:', ':arg_value:', ':arg_order:')

deleteLibRecipe=\
 DELETE FROM V4_LIB_MOD_RECIPE \
  WHERE MOD_RECIPE_LIB_ID = ':recipe_id:'

deleteLibRecipeArgs=\
 DELETE FROM V4_LIB_MOD_RECIPE_ARG \
  WHERE MOD_RECIPE_LIB_ID = ':recipe_id:'

insertTrialRecipe=\
 INSERT INTO V4_EXPT_TRIAL_MOD_RECIPE \
    (TRIAL_ID, MOD_RECIPE_LIB_ID, RECIPE_ORDER, EXPT_ID) \
 VALUES (':trial_id:', ':recipe_id:', ':recipe_order:', ':expt_id:')

cleanTrialRecipe=\
 DELETE FROM V4_EXPT_TRIAL_MOD_RECIPE \
  WHERE TRIAL_ID = ':trial_id:'

# Is this agent listed in any of the matching assemblies?
checkAsbAgent=\
 SELECT '1' \
   FROM V4_ASB_AGENT \
  WHERE COMPONENT_ALIB_ID = :component_alib_id: \
    AND ASSEMBLY_ID :assembly_match:

insertAsbAgent=\
 INSERT INTO V4_ASB_AGENT \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
 VALUES \
    (:assembly_id:, :component_alib_id:, :component_lib_id:, :clone_set_id:, :component_name:)

########
# Sample recipe queries follow
# _ALL_ queries that you use in your recipes _must_ be included in this file.
# Typical usage therefore is to create a new query by editing this file,
# copying one of the provided sample queries.

# First, sample target component queries: where to insert the component
# First, those that look for agents

# Find all agents
recipeQueryAllAgents=\
 SELECT C.COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT C, V4_ASB_COMPONENT_HIERARCHY H \
  WHERE C.COMPONENT_TYPE='agent' \
    AND (H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID OR H.PARENT_COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID) \
    AND H.ASSEMBLY_ID :assembly_match:

# Find some agents by name - in this case, subordinates of 2BDE
recipeQuery2BDE_Sub_AgentsByName=\
 SELECT C.COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT C, V4_ASB_COMPONENT_HIERARCHY H \
  WHERE C.COMPONENT_TYPE='agent' \
    AND (H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID OR H.PARENT_COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID) \
    AND H.ASSEMBLY_ID :assembly_match: \
    AND C.COMPONENT_NAME in ('2-7-INFBN', '3-69-ARBN', '3-FSB')

# Find the subordinates of 3BDE. By changing the agent name and the role,
# you can do different relationships. Note that this must be a direct relationship - 
# this is not transitive.
recipeQuerySubordinatesOf3_BDE_2ID_HHC=\
 SELECT SPTG.COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT SPTG, V4_ALIB_COMPONENT SPTD, V4_ASB_AGENT_RELATION R \
  WHERE R.SUPPORTED_COMPONENT_ALIB_ID = SPTD.COMPONENT_ALIB_ID \
    AND R.SUPPORTING_COMPONENT_ALIB_ID = SPTG.COMPONENT_ALIB_ID \
    AND R.ASSEMBLY_ID :assembly_match: \
    AND R.ROLE = 'Subordinate' \
    AND SPTD.COMPONENT_NAME = '3-BDE-2ID-HHC'

# Similar to above, but a different agent name
recipeQuerySubordinatesOf2_BDE_3ID_HHC=\
 SELECT SPTG.COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT SPTG, V4_ALIB_COMPONENT SPTD, V4_ASB_AGENT_RELATION R \
  WHERE R.SUPPORTED_COMPONENT_ALIB_ID = SPTD.COMPONENT_ALIB_ID \
    AND R.SUPPORTING_COMPONENT_ALIB_ID = SPTG.COMPONENT_ALIB_ID \
    AND R.ASSEMBLY_ID :assembly_match: \
    AND R.ROLE = 'Subordinate' \
    AND SPTD.COMPONENT_NAME = '2-BDE-3ID-HHC'

# Next, sample queries where the target is a Node (for inserting Agents or
# Agent level Binders)
# First, get all nodes in the experiment
recipeQueryAllNodes =\
 SELECT C.COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT C, V4_ASB_COMPONENT_HIERARCHY H \
  WHERE C.COMPONENT_TYPE='node' \
    AND H.PARENT_COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID \
    AND H.ASSEMBLY_ID :assembly_match:

# Then, get a set of Nodes by name
recipeQuerySetOfNodes =\
 SELECT COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT \
  WHERE COMPONENT_TYPE = 'node' \
  AND COMPONENT_NAME IN ('Name1', 'Name2')

# Finally, get some Nodes by specifying the names of the agent
# that should be in those Nodes
recipeQueryNodesWithSpecificAgents =\
 SELECT N.COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT A, V4_ASB_COMPONENT_HIERARCHY H, V4_ALIB_COMPONENT N \
  WHERE A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
   AND H.PARENT_COMPONENT_ALIB_ID = N.COMPONENT_ALIB_ID \
   AND N.COMPONENT_TYPE='node' \
   AND H.ASSEMBLY_ID :assembly_match: \
   AND A.COMPONENT_NAME='Agent Name'

### Now, queries to get the component to insert
# These examples dont actually get the data from the DB,
# but rather, hard code the values.
# We are retrieving the component name, type, and class
recipeQueryExampleBinderSpecification=\
 SELECT 'org.cougaar.core.examples.PluginServiceFilter', 'agent binder', 'org.cougaar.core.examples.PluginServiceFilter' \
   FROM DUAL

# Here we load the MIC TechSpecBinder. Be sure that techspecs.jar (built
# against your version of Cougaar and including the appropriate
# default_techspecs.xml is in CIP/sys on all machines)
recipeQueryMICBinder=\
 SELECT 'com.mobile_intelligence.contracts.TechSpecBinderFactory', 'agent binder', 'com.mobile_intelligence.contracts.TechSpecBinderFactory' \
   FROM DUAL

#####
# Next, queries to get the arguments to the inserted component.
# The number of arguments is arbitrary - use the last selectNothing query if you
# don't need any arguments

# Here we supply the properties for the MIC TechSpecBinder for one community in capture mode
# Note that you will want to put the property file on the ConfigPath.
# One option is to create a separate directory for these files,
# And add the a -D argument to the appropriate Nodes to include that directory on
# the config path, as in:
# org.cougaar.config.path="C\:\\Cougaar\\configs\\mic\;"
recipeQueryMICBinderParams2BDEPropFileCaptureMode=\
 SELECT 'file=2bde.properties', 'CAPTURE' FROM DUAL WHERE DUMMY = 'X'

recipeQueryMICBinderParamsOnePropFileMonitorMode=\
 SELECT 'file=2bde.properties', 'MONITOR' FROM DUAL WHERE DUMMY = 'X'

# Here is a query that gives no arguments to the component.
recipeQueryExampleBinderArgs=\
 SELECT NULL, NULL FROM DUAL WHERE DUMMY IS NULL

recipeQuerySelectNothing=\
 SELECT * FROM DUAL WHERE DUMMY IS NULL

recipeQueryNCAAgent=\
 SELECT COMPONENT_ALIB_ID FROM V4_ALIB_COMPONENT WHERE COMPONENT_TYPE = 'agent' AND COMPONENT_NAME='NCA'
