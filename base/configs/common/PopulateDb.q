database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

insertAlibComponent=\
 INSERT INTO V4_ALIB_COMPONENT \
    (COMPONENT_ALIB_ID, COMPONENT_NAME, \
     COMPONENT_LIB_ID, \
     COMPONENT_TYPE) \
 VALUES (:component_alib_id, :component_name, :component_lib_id, :component_category)

checkAlibComponent=\
 SELECT COMPONENT_ALIB_ID \
   FROM V4_ALIB_COMPONENT \
  WHERE COMPONENT_ALIB_ID = :component_alib_id

insertComponentHierarchy=\
 INSERT INTO V4_ASB_COMPONENT_HIERARCHY \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, \
     PARENT_COMPONENT_ALIB_ID, \
     INSERTION_ORDER) \
 VALUES (:assembly_id, :component_alib_id, :parent_component_alib_id, :insertion_order)

insertComponentArg=\
 INSERT INTO V4_ASB_COMPONENT_ARG \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
 VALUES (:assembly_id, :component_alib_id, :argument_value, :argument_order)

insertAgentOrg=\
 INSERT INTO V4_ASB_AGENT_ORG \
    (COMPONENT_LIB_ID, AGENT_ORG_PROTOTYPE) \
 VALUES
    (:component_lib_id, :agentOrgPrototype)

insertRelationship=\
 INSERT INTO V4_ASB_AGENT_RELATION \
    (ASSEMBLY_ID, ROLE, \
     SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, \
     START_DATA, END_DATE) \
 VALUES
    (:assembly_id, :role, :supporting, :supported, :start_date, :end_date)

insertAttribute=\
 INSERT INTO V4_ASB_AGENT_PG_ATTR \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, \
     ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, \
     START_DATE, END_DATE) \
 VALUES \
    (:assembly_id, :component_alib_id, pg_attribute_lib_id, \
     :attribute_value, :attribute_order, \
     :start_date, :end_date)

queryLibPGAttribute=\
 SELECT PG_NAME, ATTRIBUTE_NAME, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_TYPE, AGGREGATE_TYPE \
   FROM V4_LIB_PG_ATTRIBUTE \
  WHERE PG_NAME = :pg_name

queryMaxAssemblyId=\
 SELECT MAX(ASSEMBLY_ID) \
   FROM V4_ASB_ASSEMBLY \
  WHERE ASSEMBLY_TYPE = ':assembly_type' \
    AND ASSEMBLY_ID LIKE ':assembly_id_pattern'

insertAssemblyId=\
 INSERT INTO V4_ASB_ASSEMBLY (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) \
 VALUES (:assembly_id, ':assembly_type', ':assembly_type assembly')

insertTrialAssembly=\
 INSERT INTO V4_EXPT_TRIAL_ASSEMBLY (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 VALUES (':expt_id', ':trial_id', :assembly_id, ':assembly_type assembly')

cleanTrialAssembly=\
 DELETE FROM V4_EXPT_TRIAL_ASSEMBLY \
  WHERE EXPT_ID = ':expt_id' \
    AND TRIAL_ID = ':trial_id' \
    AND ASSEMBLY_ID IN \
        (SELECT ASSEMBLY_ID \
           FROM V4_ASB_ASSEMBLY \
          WHERE ASSEMBLY_TYPE = ':assembly_type')
