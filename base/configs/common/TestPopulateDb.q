database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryMaxExpt=\
 SELECT MAX(EXPT_ID) \
   FROM v4_expt_experiment \
  WHERE EXPT_ID LIKE 'REGRESSION_%'

insertExpt=\
 INSERT INTO v4_expt_experiment (EXPT_ID, DESCRIPTION) \
 VALUES (':expt_id', ':description')

queryMaxTrial=\
 SELECT MAX(TRIAL_ID) \
   FROM v4_expt_trial \
  WHERE TRIAL_ID like 'REGRESSION_%'

insertTrial=\
 INSERT INTO v4_expt_trial (TRIAL_ID, EXPT_ID, DESCRIPTION) \
 VALUES (':trial_id', ':expt_id', ':description')

queryMaxAssembly=\
 SELECT MAX(ASSEMBLY_ID) \
   FROM v4_asb_assembly \
  WHERE ASSEMBLY_TYPE = 'REGRESSION_TEST'

insertAssembly=\
 INSERT INTO v4_asb_assembly (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) \
 VALUES (':assembly_id', 'REGRESSION_TEST', ':description')

insertTrialAssembly=\
 INSERT INTO v4_expt_trial_assembly (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 VALUES (':expt_id', ':trial_id', ':assembly_id', ':description')

insertLibComponent=\
 INSERT INTO v4_lib_component \
   (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) \
 VALUES \
   (':component_lib_id', ':component_type', ':component_class', ':insertion_point', ':description')

queryLibComponent=\
 SELECT COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION \
   FROM v4_lib_component \
  WHERE COMPONENT_LIB_ID = ':component_lib_id'

deleteLibComponent=\
 DELETE FROM v4_lib_component \
  WHERE COMPONENT_LIB_ID = ':component_lib_id'

deleteAlibComponent=\
 DELETE FROM v4_alib_component \
  WHERE COMPONENT_LIB_ID = ':component_lib_id'

queryTableWithTrialId=\
 SELECT TABLE_NAME FROM USER_TAB_COLUMNS \
  WHERE COLUMN_NAME = 'TRIAL_ID' \
    AND TABLE_NAME LIKE 'v4_%' \
    AND TABLE_NAME <> 'v4_expt_trial'

queryTableWithAssemblyId=\
 SELECT TABLE_NAME FROM USER_TAB_COLUMNS \
  WHERE COLUMN_NAME = 'ASSEMBLY_ID' \
    AND TABLE_NAME LIKE 'v4_%' \
    AND TABLE_NAME <> 'v4_asb_assembly'

queryTableWithExptId=\
 SELECT TABLE_NAME FROM USER_TAB_COLUMNS \
  WHERE COLUMN_NAME = 'EXPT_ID' \
    AND TABLE_NAME LIKE 'v4_%' \
    AND TABLE_NAME <> 'v4_expt_experiment'

deleteFromTableWithAssemblyId=\
 DELETE FROM :table \
  WHERE ASSEMBLY_ID = ':assembly_id'

deleteFromTableWithTrialId=\
 DELETE FROM :table \
  WHERE TRIAL_ID = ':trial_id'

deleteFromTableWithExptId=\
 DELETE FROM :table \
  WHERE EXPT_ID = ':expt_id'

queryAllTableColumns=\
 SELECT TABLE_NAME, COLUMN_NAME FROM USER_TAB_COLUMNS \
  WHERE TABLE_NAME LIKE 'v4_%' \
  ORDER BY TABLE_NAME

deleteFromTableInitial=DELETE FROM :table WHERE :column like '%:clean_type%'
deleteFromTableMore=\ OR :column like ':clean_type%'

deleteAssembly=\
 DELETE FROM v4_expt_trial_assembly \
  WHERE ASSEMBLY_ID = ':assembly_id'

deleteTrial=\
 DELETE FROM v4_expt_trial \
  WHERE TRIAL_ID = ':trial_id'

deleteExpt=\
  DELETE FROM v4_expt_experiment \
   WHERE EXPT_ID = ':expt_id'

deleteAllFromTableWithAssemblyId=\
 DELETE FROM :table \
  WHERE ASSEMBLY_ID like ':clean_type_%'

deleteAllFromTableWithTrialId=\
 DELETE FROM :table \
  WHERE TRIAL_ID like ':clean_type_%'

deleteAllFromTableWithExptId=\
 DELETE FROM :table \
  WHERE EXPT_ID like ':clean_type_%'

deleteAllAssembly=\
 DELETE FROM v4_expt_trial_assembly \
  WHERE ASSEMBLY_ID like ':clean_type_%'

deleteAllTrial=\
 DELETE FROM v4_expt_trial \
  WHERE TRIAL_ID like ':clean_type_%'

deleteAllExpt=\
  DELETE FROM v4_expt_experiment \
   WHERE EXPT_ID like ':clean_type_%'
