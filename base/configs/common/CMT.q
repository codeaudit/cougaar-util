database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryAssemblyID = \
 SELECT ASSEMBLY_ID, DESCRIPTION \
   FROM V4_ASB_ASSEMBLY \
  WHERE ASSEMBLY_TYPE = ':assemblytype'

updateAssemblyID = \
 UPDATE V4_ASB_ASSEMBLY \
   SET ASSEMBLY_ID=ASSEMBLY_ID \
   WHERE ASSEMBLY_ID = ':assembly_id'


unusedAssemblies = \
 SELECT ASSEMBLY_ID FROM \
  V4_ASB_ASSEMBLY AA \
  WHERE AA.ASSEMBLY_ID NOT IN \
  (SELECT ASSEMBLY_ID FROM V4_EXPT_TRIAL_ASSEMBLY)

insertASBAssembly = \
 INSERT INTO V4_ASB_ASSEMBLY (ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION) \
  values (:assembly_id,'CMT',:assembly_description)

addClonedASBAgents = \
 INSERT INTO  V4_ASB_AGENT \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
    SELECT DISTINCT ':assembly_id'  AS ASSEMBLY_ID, \
      (CS.CLONE_SET_ID || '-' || OGOM.ORG_ID) AS COMPONENT_ALIB_ID,	
      OGOM.ORG_ID AS COMPONENT_LIB_ID, \
      CS.CLONE_SET_ID AS CLONE_SET_ID, \
      AC.COMPONENT_NAME AS COMPONENT_NAME \
    FROM V6_CFW_ORG_GROUP_ORG_MEMBER OGOM, \
         V4_ALIB_COMPONENT AC, \
	 V4_LIB_CLONE_SET CS \
    WHERE OGOM.ORG_GROUP_ID= ':org_group_id'
          AND AC.COMPONENT_ALIB_ID=(CS.CLONE_SET_ID || '-' || OGOM.ORG_ID) \
	  AND CS.CLONE_SET_ID>0 AND CS.CLONE_SET_ID< :n \
	  AND NOT EXISTS \
	  (SELECT COMPONENT_ALIB_ID \
	    FROM  V4_ASB_AGENT AA \
	    WHERE \
	     AA.COMPONENT_ALIB_ID=(CS.CLONE_SET_ID || '-' || OGOM.ORG_ID) \
	     AND AA.ASSEMBLY_ID=':assembly_id' )

addNewBaseAgentAlibComponents = \
 INSERT INTO V4_ALIB_COMPONENT \
 (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
 SELECT DISTINCT \
        GO.ORG_ID AS COMPONENT_ALIB_ID, \
        ORG.ORG_ID AS COMPONENT_NAME, \
	ORG.ORG_ID AS COMPONENT_LIB_ID, \
	'agent' AS COMPONENT_TYPE, \
	0 as  CLONE_SET_ID \
    FROM  V6_CFW_GROUP_ORG GO, \
          V6_LIB_ORGANIZATION ORG \
      WHERE GO.CFW_GROUP_ID= ':cfw_group_id' \
      AND GO.ORG_ID =ORG.ORG_ID \
      AND GO.ORG_ID NOT IN \
       (SELECT COMPONENT_ALIB_ID FROM  V4_ALIB_COMPONENT)

# check how this should be based on the CFW_GROUP_ID!

addNewClonedAgentAlibComponents = \
 INSERT INTO V4_ALIB_COMPONENT \
 (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
 SELECT DISTINCT  \
      (CLONE_SET_ID || '-' || OGOM.ORG_ID) AS COMPONENT_ALIB_ID, \
      (CLONE_SET_ID || '-' || OGOM.ORG_ID) AS COMPONENT_NAME, \
      OGOM.ORG_ID AS COMPONENT_LIB_ID, \
      'agent' AS COMPONENT_TYPE, \
      CLONE_SET_ID AS  CLONE_SET_ID \
     FROM  V6_CFW_ORG_GROUP_ORG_MEMBER OGOM, \
      V4_LIB_CLONE_SET cs \
      WHERE OGOM.ORG_GROUP_ID=':org_group_id' \
      AND CS.CLONE_SET_ID>0 AND CS.CLONE_SET_ID< :n \
      AND (CLONE_SET_ID || '-' || OGOM.ORG_ID) NOT IN \
       (SELECT COMPONENT_ALIB_ID FROM  V4_ALIB_COMPONENT)

addBaseASBAgents = \
 INSERT INTO  V4_ASB_AGENT \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
    SELECT DISTINCT ':assembly_id'  AS ASSEMBLY_ID, \
        GO.ORG_ID AS COMPONENT_ALIB_ID, \
	GO.ORG_ID AS COMPONENT_LIB_ID, \
	0 AS  CLONE_SET_ID, \
	AC.COMPONENT_NAME AS COMPONENT_NAME \
      FROM  V6_CFW_GROUP_ORG GO, \
        V4_ALIB_COMPONENT AC \
	WHERE GO.CFW_GROUP_ID= ':cfw_group_id' \
	AND AC.COMPONENT_ALIB_ID=GO.ORG_ID \
	AND NOT EXISTS \
	 (SELECT COMPONENT_ALIB_ID \
	   FROM V4_ASB_AGENT AA \
	   WHERE \
	    AA.COMPONENT_ALIB_ID=GO.ORG_ID \
	    AND AA.ASSEMBLY_ID= ':assembly_id') 

addNewPluginAlibComponents = \
 INSERT INTO V4_ALIB_COMPONENT \
  (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
  SELECT DISTINCT \
    (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_CLASS) AS COMPONENT_ALIB_ID, \
     (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_CLASS) AS COMPONENT_NAME, \
     'plugin|' || PL.PLUGIN_CLASS AS COMPONENT_LIB_ID, \
     'plugin' AS COMPONENT_TYPE, \
     0 AS  CLONE_SET_ID \
   FROM  V4_ASB_AGENT AA, \
         V6_CFW_GROUP_MEMBER GM, \
	 V6_CFW_ORG_ORGTYPE OT, \
	 V6_CFW_ORGTYPE_PLUGIN_GRP PG, \
	 V6_CFW_PLUGIN_GROUP_MEMBER PL, \
	 V6_LIB_PLUGIN_THREAD PTH \
   WHERE \
     GM.CFW_GROUP_ID= ':cfw_group_id' \
     AND AA.ASSEMBLY_ID=  ':assembly_id' \
     AND AA.COMPONENT_LIB_ID=OT.ORG_ID \
     AND GM.CFW_ID=OT.CFW_ID \
     AND OT.ORGTYPE_ID=PG.ORGTYPE_ID \
     AND GM.CFW_ID=PG.CFW_ID \
     AND PG.PLUGIN_GROUP_ID = PL.PLUGIN_GROUP_ID \
     AND GM.CFW_ID=PG.CFW_ID \
     AND PTH.PLUGIN_CLASS=PL.PLUGIN_CLASS \
     AND PTH.THREAD_ID IN :threads \
     AND (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_CLASS) NOT IN \
      (SELECT COMPONENT_ALIB_ID FROM  V4_ALIB_COMPONENT) 

addPluginASBComponentHierarchy = \
 INSERT INTO  V4_ASB_COMPONENT_HIERARCHY \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER) \
  SELECT DISTINCT \
    ':assembly_id' AS ASSEMBLY_ID, \
    (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_CLASS) AS COMPONENT_ALIB_ID, \
    AA.COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, \
    (PL.PLUGIN_CLASS_ORDER+(1000* PG.PLUGIN_GROUP_ORDER)) AS INSERTION_ORDER \
   FROM \
     V4_ASB_AGENT AA, \
     V6_CFW_GROUP_MEMBER GM, \
     V6_CFW_ORG_ORGTYPE OT, \
     V6_CFW_ORGTYPE_PLUGIN_GRP OPG, \
     V6_LIB_PLUGIN_GROUP PG, \
     V6_CFW_PLUGIN_GROUP_MEMBER PL, \
     V6_LIB_PLUGIN_THREAD PTH \
    WHERE \
      GM.CFW_GROUP_ID=   ':cfw_group_id' \
      AND AA.ASSEMBLY_ID= ':assembly_id' \
      AND AA.COMPONENT_LIB_ID=OT.ORG_ID \
      AND GM.CFW_ID=OT.CFW_ID \
      AND OT.ORGTYPE_ID=OPG.ORGTYPE_ID \
      AND PG.PLUGIN_GROUP_ID = PL.PLUGIN_GROUP_ID \
      AND PG.PLUGIN_GROUP_ID = OPG.PLUGIN_GROUP_ID \
      AND GM.CFW_ID=OPG.CFW_ID \
      AND GM.CFW_ID=PL.CFW_ID \
      AND PTH.PLUGIN_CLASS=PL.PLUGIN_CLASS \
      AND PTH.THREAD_ID IN :threads \
      AND NOT EXISTS \
       (SELECT COMPONENT_ALIB_ID \
         FROM  V4_ASB_COMPONENT_HIERARCHY ACH \
         WHERE \
	  ACH.ASSEMBLY_ID=':assembly_id' \
	  AND ACH.COMPONENT_ALIB_ID=(AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_CLASS) \
	  AND ACH.PARENT_COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID)

addAgentNameComponentArg = \
 INSERT INTO  V4_ASB_COMPONENT_ARG  \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   AA.COMPONENT_ALIB_ID AS ARGUMENT, \
   0 AS ARGUMENT_ORDER \
   FROM \
    V4_ASB_AGENT AA \
   WHERE \
    AA.ASSEMBLY_ID= ':assembly_id' \
    AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM  \
        V4_ASB_COMPONENT_ARG ACA \
      WHERE \
        ASSEMBLY_ID=':assembly_id' \
	AND ACA.COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID \
	AND ACA.ARGUMENT=AA.COMPONENT_ALIB_ID )

addASBAgentPGAttr = \
 INSERT INTO  V4_ASB_AGENT_PG_ATTR \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE) \
  SELECT DISTINCT \
     ':assembly_id'  AS ASSEMBLY_ID, \
     AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
     PGA.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, \
     PGA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, \
     PGA.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, \
     PGA.START_DATE AS START_DATE, \
     PGA.END_DATE AS END_DATE \
   FROM \
     V4_ASB_AGENT AA, \
     V6_CFW_GROUP_MEMBER GM, \
     V6_CFW_ORG_PG_ATTR PGA, \
     V4_LIB_PG_ATTRIBUTE LPGA \
   WHERE \
     AA.ASSEMBLY_ID= ':assembly_id' \
     AND GM.CFW_GROUP_ID= ':cfw_group_id' \
     AND AA.COMPONENT_LIB_ID=PGA.ORG_ID \
     AND GM.CFW_ID =PGA.CFW_ID \
     AND LPGA.PG_ATTRIBUTE_LIB_ID=PGA.PG_ATTRIBUTE_LIB_ID \
     AND NOT EXISTS \
      (SELECT ASSEMBLY_ID \
       FROM  V4_ASB_AGENT_PG_ATTR PX \
       WHERE \
        PX.ASSEMBLY_ID=':assembly_id' \
	AND PX.COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID \
	AND PX.PG_ATTRIBUTE_LIB_ID=PGA.PG_ATTRIBUTE_LIB_ID \
        AND PX.START_DATE=PGA.START_DATE) 


addASBAgentRelationToBase = \
 INSERT INTO  V4_ASB_AGENT_RELATION \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
  SELECT DISTINCT \
    ':assembly_id'  AS ASSEMBLY_ID, \
    ORGREL.ROLE AS ROLE, \
    SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
    SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
    ORGREL.START_DATE AS START_DATE, \
    ORGREL.END_DATE AS END_DATE \
   FROM \
     V4_ASB_AGENT SUPPORTED_ORG, \
     V4_ASB_AGENT SUPPORTING_ORG, \
     V6_CFW_ORG_OG_RELATION ORGREL, \
     V6_CFW_ORG_GROUP_ORG_MEMBER OGOM \
    WHERE \
     ORGREL.CFW_ID IN \
      (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID=':cfw_group_id' ) \
     AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
     AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
     AND SUPPORTING_ORG.COMPONENT_LIB_ID=ORGREL.ORG_ID \
     AND OGOM.CFW_ID IN \
      (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID=':cfw_group_id') \
     AND OGOM.ORG_GROUP_ID = ORGREL.ORG_GROUP_ID \
     AND SUPPORTED_ORG.COMPONENT_LIB_ID=OGOM.ORG_ID \
     AND SUPPORTING_ORG.CLONE_SET_ID=0 \
     AND SUPPORTING_ORG.COMPONENT_ALIB_ID<>SUPPORTED_ORG.COMPONENT_ALIB_ID \
     AND ORGREL.ROLE <> 'Subordinate' \
     AND ORGREL.ROLE <> 'Superior' \
     AND NOT EXISTS \
      (SELECT ASSEMBLY_ID \
        FROM \
	 V4_ASB_AGENT_RELATION AR \
	WHERE \
	 AR.ASSEMBLY_ID=':assembly_id' \
	 AND AR.SUPPORTED_COMPONENT_ALIB_ID =SUPPORTED_ORG.COMPONENT_ALIB_ID \
	 AND AR.ROLE=ORGREL.ROLE \
	 AND AR.START_DATE=ORGREL.START_DATE)


addASBAgentRelationToCloneset = \
 INSERT INTO  V4_ASB_AGENT_RELATION \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   ORGREL.ROLE AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   ORGREL.START_DATE AS START_DATE, \
   ORGREL.END_DATE AS END_DATE \
  FROM \
   V4_ASB_AGENT SUPPORTED_ORG, \
   V4_ASB_AGENT SUPPORTING_ORG, \
   V6_CFW_ORG_OG_RELATION ORGREL, \
   V6_CFW_ORG_GROUP_ORG_MEMBER OGOM \
  WHERE \
   ORGREL.CFW_ID IN \
    (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID=':cfw_group_id' ) \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.COMPONENT_LIB_ID=ORGREL.ORG_ID \
   AND OGOM.CFW_ID IN \
    (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID=':cfw_group_id' )\
   AND OGOM.ORG_GROUP_ID = ORGREL.ORG_GROUP_ID \
   AND SUPPORTED_ORG.COMPONENT_LIB_ID=OGOM.ORG_ID \
   AND SUPPORTING_ORG.CLONE_SET_ID=SUPPORTED_ORG.CLONE_SET_ID \
   AND SUPPORTING_ORG.COMPONENT_ALIB_ID<>SUPPORTED_ORG.COMPONENT_ALIB_ID \
   AND ORGREL.ROLE <> 'Subordinate' \
   AND ORGREL.ROLE <> 'Superior' \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
       V4_ASB_AGENT_RELATION AR \
      WHERE \
       AR.ASSEMBLY_ID=':assembly_id' \
       AND AR.SUPPORTED_COMPONENT_ALIB_ID =SUPPORTED_ORG.COMPONENT_ALIB_ID \
       AND AR.ROLE=ORGREL.ROLE \
       AND AR.START_DATE=ORGREL.START_DATE)

addASBAgentHierarchyRelationToBase = \
 INSERT INTO  V4_ASB_AGENT_RELATION \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   'Subordinate' AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   TO_DATE('1-JAN-2001') AS START_DATE, \
   NULL AS END_DATE \
  FROM \
   V4_ASB_AGENT SUPPORTED_ORG, \
   V4_ASB_AGENT SUPPORTING_ORG, \
   V6_CFW_ORG_HIERARCHY OH \
  WHERE \
   OH.CFW_ID IN \
    (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID=':cfw_group_id') \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND OH.ORG_ID=SUPPORTING_ORG.COMPONENT_LIB_ID \
   AND OH.SUPERIOR_ORG_ID=SUPPORTED_ORG.COMPONENT_LIB_ID \
   AND SUPPORTED_ORG.CLONE_SET_ID=0 \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
       V4_ASB_AGENT_RELATION AR \
      WHERE \
       AR.ASSEMBLY_ID=':assembly_id' \
       AND AR.SUPPORTING_COMPONENT_ALIB_ID=SUPPORTING_ORG.COMPONENT_ALIB_ID \
       AND AR.ROLE='Subordinate' \
       AND AR.START_DATE=TO_DATE('1-JAN-2001'))


addASBAgentHierarchyRelationToCloneset = \
 INSERT INTO  V4_ASB_AGENT_RELATION \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   'Subordinate' AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   TO_DATE('1-JAN-2001') AS START_DATE, \
   NULL AS END_DATE \
  FROM \
   V4_ASB_AGENT SUPPORTED_ORG, \
   V4_ASB_AGENT SUPPORTING_ORG, \
   V6_CFW_ORG_HIERARCHY OH \
  WHERE \
   OH.CFW_ID IN (SELECT CFW_ID FROM V6_CFW_GROUP_MEMBER WHERE CFW_GROUP_ID=':cfw_group_id') \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND OH.ORG_ID=SUPPORTING_ORG.COMPONENT_LIB_ID \
   AND OH.SUPERIOR_ORG_ID=SUPPORTED_ORG.COMPONENT_LIB_ID \
   AND SUPPORTING_ORG.CLONE_SET_ID=SUPPORTED_ORG.CLONE_SET_ID \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
       V4_ASB_AGENT_RELATION AR \
      WHERE \
       AR.ASSEMBLY_ID=':assembly_id' \
       AND AR.SUPPORTING_COMPONENT_ALIB_ID=SUPPORTING_ORG.COMPONENT_ALIB_ID \
       AND AR.ROLE='Subordinate' \
       AND AR.START_DATE=TO_DATE('1-JAN-2001'))

addPluginAgentASBComponentArg = \
 INSERT INTO  V4_ASB_COMPONENT_ARG  \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
  FROM \
    V4_ASB_COMPONENT_HIERARCHY CH, \
    V4_ALIB_COMPONENT PLUGIN_ALIB, \
    V4_ASB_AGENT ORG_AGENT, \
    V6_CFW_CONTEXT_PLUGIN_ARG CPA, \
    V6_LIB_PLUGIN_ARG PA, \
    V6_LIB_PLUGIN_ARG_THREAD PAT, \
    V6_CFW_GROUP_MEMBER GM \
  WHERE \
    GM.CFW_GROUP_ID=':cfw_group_id' \
    AND CH.ASSEMBLY_ID=':assembly_id' \
    AND PLUGIN_ALIB.COMPONENT_TYPE='plugin' \
    AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
    AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
    AND CPA.CFW_ID=GM.CFW_ID \
    AND PA.ARGUMENT IS NOT NULL \
    AND CPA.ORG_CONTEXT = ORG_AGENT.COMPONENT_LIB_ID \
    AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
    AND ('plugin|' || PA.PLUGIN_CLASS)=PLUGIN_ALIB.COMPONENT_LIB_ID \
    AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
    AND PAT.THREAD_ID IN :threads \
    AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM  \
       V4_ASB_COMPONENT_ARG ACA \
      WHERE \
       ASSEMBLY_ID=':assembly_id' \
       AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID)

addPluginOrgtypeASBComponentArg = \
 INSERT INTO  V4_ASB_COMPONENT_ARG  \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
  FROM \
   V4_ASB_COMPONENT_HIERARCHY CH, \
   V4_ALIB_COMPONENT PLUGIN_ALIB, \
   V4_ASB_AGENT ORG_AGENT, \
   V6_CFW_CONTEXT_PLUGIN_ARG CPA, \
   V6_CFW_ORG_ORGTYPE OT, \
   V6_LIB_PLUGIN_ARG PA, \
   V6_LIB_PLUGIN_ARG_THREAD PAT, \
   V6_CFW_GROUP_MEMBER GM \
  WHERE \
   GM.CFW_GROUP_ID=':cfw_group_id' \
   AND CH.ASSEMBLY_ID=':assembly_id' \
   AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
   AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
   AND CPA.CFW_ID=GM.CFW_ID \
   AND OT.CFW_ID=GM.CFW_ID \
   AND OT.ORG_ID=ORG_AGENT.COMPONENT_LIB_ID \
   AND PA.ARGUMENT IS NOT NULL \
   AND CPA.ORG_CONTEXT = OT.ORGTYPE_ID \
   AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
   AND ('plugin|' || PA.PLUGIN_CLASS)=PLUGIN_ALIB.COMPONENT_LIB_ID \
   AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
   AND PAT.THREAD_ID IN :threads \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
       FROM \
         V4_ASB_COMPONENT_ARG ACA \
       WHERE \
         ASSEMBLY_ID=':assembly_id' \
	 AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID \
	 AND ACA.ARGUMENT_ORDER=PA.ARGUMENT_ORDER)

addPluginAllASBComponentArg = \
 INSERT INTO  V4_ASB_COMPONENT_ARG  \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
  FROM \
   V4_ASB_COMPONENT_HIERARCHY CH, \
   V4_ALIB_COMPONENT PLUGIN_ALIB, \
   V4_ASB_AGENT ORG_AGENT, \
   V6_CFW_CONTEXT_PLUGIN_ARG CPA, \
   V6_LIB_PLUGIN_ARG PA, \
   V6_LIB_PLUGIN_ARG_THREAD PAT, \
   V6_CFW_GROUP_MEMBER GM \
  WHERE \
   GM.CFW_GROUP_ID=':cfw_group_id' \
   AND CH.ASSEMBLY_ID=':assembly_id' \
   AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
   AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
   AND CPA.CFW_ID=GM.CFW_ID \
   AND PA.ARGUMENT IS NOT NULL \
   AND CPA.ORG_CONTEXT = 'ALL' \
   AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
   AND ('plugin|' || PA.PLUGIN_CLASS)=PLUGIN_ALIB.COMPONENT_LIB_ID \
   AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
   AND PAT.THREAD_ID IN :threads \
   AND NOT EXISTS \
     (SELECT ASSEMBLY_ID \
       FROM \
        V4_ASB_COMPONENT_ARG ACA \
       WHERE \
        ASSEMBLY_ID=':assembly_id' \
	AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID)

addASBOplans = \
 INSERT INTO  V4_ASB_OPLAN \
   (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME , PRIORITY, C0_DATE) \
   SELECT DISTINCT \
    ':assembly_id'  AS ASSEMBLY_ID, \
    OP.OPLAN_ID AS OPLAN_ID, \
    OP.OPERATION_NAME AS OPERATION_NAME, \
    OP.PRIORITY AS PRIORITY, \
    OP.C0_DATE AS C0_DATE \
   FROM \
    V6_CFW_OPLAN OP, \
    V6_CFW_GROUP_MEMBER GM \
   WHERE \
    GM.CFW_ID=OP.CFW_ID \
    AND OP.OPLAN_ID IN :oplan_ids \
    AND NOT EXISTS  \
     (SELECT OPLAN_ID \
       FROM \
        V4_ASB_OPLAN \
       WHERE \
        ASSEMBLY_ID=':assembly_id' \
	AND OPLAN_ID IN :oplan_ids)

addASBOplanAgentAttr = \
 INSERT INTO  V4_ASB_OPLAN_AGENT_ATTR  \
   (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME , END_CDAY, ATTRIBUTE_VALUE) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   OOA.OPLAN_ID AS OPLAN_ID, \
   AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   AA.COMPONENT_ALIB_ID AS COMPONENT_ID, \
   OOA.START_CDAY AS START_CDAY, \
   OOA.ATTRIBUTE_NAME AS ATTRIBUTE_NAME, \
   OOA.END_CDAY AS END_CDAY, \
   OOA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE \
  FROM \
   V4_ASB_AGENT AA, \
   V6_CFW_GROUP_MEMBER GM, \
   V6_CFW_OPLAN_OG_ATTR OOA, \
   V6_CFW_ORG_GROUP_ORG_MEMBER OGOM \
  WHERE \
   GM.CFW_GROUP_ID=':cfw_group_id' \
   AND AA.ASSEMBLY_ID=':assembly_id' \
   AND OOA.CFW_ID=GM.CFW_ID \
   AND OGOM.CFW_ID=GM.CFW_ID \
   AND OGOM.ORG_ID=AA.COMPONENT_LIB_ID \
   AND OOA.ORG_GROUP_ID=OGOM.ORG_GROUP_ID \
   AND OOA.OPLAN_ID IN :oplan_ids \
   AND NOT EXISTS  \
   (SELECT ASSEMBLY_ID \
     FROM \
      V4_ASB_OPLAN_AGENT_ATTR AR \
     WHERE \
      AR.ASSEMBLY_ID=':assembly_id' \
      AND AR.OPLAN_ID=OOA.OPLAN_ID \
      AND AR.COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID \
      AND AR.COMPONENT_ID=AA.COMPONENT_ALIB_ID \
      AND AR.START_CDAY=OOA.START_CDAY \
      AND AR.ATTRIBUTE_NAME=OOA.ATTRIBUTE_NAME)

getExperimentNames = \
 SELECT  NAME,EXPT_ID FROM V4_EXPT_EXPERIMENT

getTrialNames = \
 SELECT  DESCRIPTION,TRIAL_ID FROM V4_EXPT_TRIAL WHERE EXPT_ID=':experiment_id' AND DESCRIPTION IS NOT NULL  

getTrialId = \
 SELECT  TRIAL_ID FROM V4_EXPT_TRIAL WHERE EXPT_ID=':experiment_id'

addTrialName = \
INSERT INTO V4_EXPT_TRIAL(TRIAL_ID, EXPT_ID, DESCRIPTION, NAME) \
       values (':trial_id',':experiment_id',':trial_name',':trial_name')


addAssembly = \
 insert into V4_EXPT_TRIAL_ASSEMBLY (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION)
   values (':experiment_id',':trial_id',':assembly_id',':trial_name')

getSocietyTemplateForExperiment = \
 SELECT  CFW_GROUP_ID FROM V4_EXPT_EXPERIMENT WHERE EXPT_ID=':experiment_id'

createExperiment = \
 insert into V4_EXPT_EXPERIMENT (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) \
  values (':experiment_id',':experiment_id',':experiment_id',':cfw_group_id')

updateCMTAssemblyThreadID = \
 SELECT THREAD_ID FROM V4_EXPT_TRIAL_THREAD WHERE EXPT_ID=':experiment_id'

updateCMTAssemblyCFW_GROUP_ID = \
 SELECT CFW_GROUP_ID FROM V4_EXPT_EXPERIMENT WHERE  EXPT_ID=':experiment_id'

updateCMTAssemblyClones = \
 SELECT ORG_GROUP_ID, MULTIPLIER FROM V4_EXPT_TRIAL_ORG_MULT WHERE MULTIPLIER >1 AND EXPT_ID=':experiment_id'

updateCMTAssembly =\
 UPDATE V4_EXPT_TRIAL_ASSEMBLY SET ASSEMBLY_ID= ':assembly_id' WHERE EXPT_ID=':experiment_id' AND ASSEMBLY_ID LIKE 'CMT-%'

getSocietyTemplates = \
 SELECT  DESCRIPTION,CFW_GROUP_ID FROM V6_CFW_GROUP

getOrganizationGroups = \
 SELECT DISTINCT \
  OG.DESCRIPTION ,\
  OGOM.ORG_GROUP_ID \
 FROM V4_EXPT_EXPERIMENT EXP, \
  V6_CFW_GROUP_MEMBER GM, \
  V6_LIB_ORG_GROUP OG, \
  V6_CFW_ORG_GROUP_ORG_MEMBER OGOM \
 where \
  EXP.EXPT_ID=':experiment_id' \
  AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
  AND OGOM.CFW_ID=GM.CFW_ID \
  AND OG.ORG_GROUP_ID=OGOM.ORG_GROUP_ID \
  AND OG.DESCRIPTION LIKE '%CLONABLE%'

getOrganizationsInGroup = \
 SELECT OM.ORG_ID \
  FROM V4_EXPT_EXPERIMENT EXP, \
  V6_CFW_GROUP_MEMBER GM, \
  V6_CFW_ORG_GROUP_ORG_MEMBER OM \
 WHERE EXP.EXPT_ID=':experiment_id' \
  AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
  AND OM.CFW_ID=GM.CFW_ID \
  AND OM.ORG_GROUP_ID=':group_id'

addNodeAssignments = \
 INSERT INTO V4_ASB_COMPONENT_HIERARCHY \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER) \
  SELECT DISTINCT \
   ':assembly_id', \	
   AGENT.COMPONENT_ALIB_ID, \
   NODE.COMPONENT_ALIB_ID, \
   0 \
  FROM \
   V4_ALIB_COMPONENT NODE, \
   V4_ALIB_COMPONENT AGENT \
  WHERE \
   NODE.COMPONENT_NAME=':nodename' \
   AND AGENT.COMPONENT_NAME=':agentname'

addCSMARTAssembly = \
 INSERT INTO V4_ASB_ASSEMBLY \
   (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) \
  values (':assembly_id','CSMART',':assembly_description')

addMachineAssignmentsUpdate = \
UPDATE V4_ASB_COMPONENT_HIERARCHY A \
 SET ASSEMBLY_ID = ASSEMBLY_ID
  WHERE \
   ASSEMBLY_ID = ':assembly_id' \
     AND EXISTS \
      (SELECT ASSEMBLY_ID \
        FROM \
	V4_ASB_COMPONENT_HIERARCHY B, \
	V4_ALIB_COMPONENT machine, \
	V4_ALIB_COMPONENT node \
       WHERE	\
        ASSEMBLY_ID = ':assembly_id' \
	AND MACHINE.COMPONENT_NAME=':machinename' \
	AND NODE.COMPONENT_NAME=':nodename' \
	AND B.COMPONENT_ALIB_ID=NODE.COMPONENT_ALIB_ID \
	AND B.PARENT_COMPONENT_ALIB_ID=MACHINE.COMPONENT_ALIB_ID)


addMachineAssignmentsInsert = \
 INSERT INTO V4_ASB_COMPONENT_HIERARCHY \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER) \
  SELECT DISTINCT \
   ':assembly_id' , \
   NODE.COMPONENT_ALIB_ID, \
   MACHINE.COMPONENT_ALIB_ID, \
   0 \
  FROM \
    V4_ALIB_COMPONENT MACHINE, \
    V4_ALIB_COMPONENT NODE \
   WHERE \
   MACHINE.COMPONENT_NAME=':machinename' \
   AND NODE.COMPONENT_NAME=':nodename'


isULThreadSelected = \
 UPDATE V4_EXPT_TRIAL_THREAD \
   SET THREAD_ID=THREAD_ID \
   WHERE \
   TRIAL_ID=':trial_id' \
   AND THREAD_ID=':thread_id'

setULThreadSelected = \
 INSERT INTO V4_EXPT_TRIAL_THREAD \
   (EXPT_ID,TRIAL_ID, THREAD_ID) \
   SELECT \
    EXPT_ID, \
    TRIAL_ID, \
    ':thread_id' \
   FROM V4_EXPT_TRIAL \
    WHERE \
     TRIAL_ID = ':trial_id'

setULThreadNotSelected = \
 DELETE FROM V4_EXPT_TRIAL_THREAD TT \
   WHERE \
    TT.TRIAL_ID=':trial_id' \
    AND TT.THREAD_ID=':thread_id'

getGroupId = \
 SELECT OG.ORG_GROUP_ID FROM \
  V4_EXPT_EXPERIMENT EXP, \
  V4_EXPT_TRIAL ET, \
  V6_CFW_GROUP_MEMBER GM, \
  V6_LIB_ORG_GROUP OG, \
  V6_CFW_ORG_GROUP_ORG_MEMBER OGOM \
 WHERE ET.TRIAL_ID=':trial_id' \
  AND EXP.EXPT_ID=ET.EXPT_ID \
   AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
   AND OGOM.CFW_ID=GM.CFW_ID \
   AND OGOM.ORG_GROUP_ID=OG.ORG_GROUP_ID \
   AND OG.DESCRIPTION=':group_name' \

isGroupSelected = \
 UPDATE V4_EXPT_TRIAL_ORG_MULT \
    SET ORG_GROUP_ID=ORG_GROUP_ID
  WHERE TRIAL_ID=':trial_id' \
  AND ORG_GROUP_ID=':group_id'

setGroupNotSelected = \
 DELETE FROM \
   V4_EXPT_TRIAL_ORG_MULT OM \
  WHERE OM.TRIAL_ID=':trial_id' \
  AND OM.ORG_GROUP_ID =':group_id'

setGroupSelected = \   
 INSERT INTO V4_EXPT_TRIAL_ORG_MULT \
   (TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION) \
   SELECT DISTINCT \
   ET.EXPT_ID, \
   ET.TRIAL_ID, \
   GM.CFW_ID, \
   OG.ORG_GROUP_ID, \
   1, \
   NULL \
   FROM \
    V4_EXPT_EXPERIMENT EXP, \
    V4_EXPT_TRIAL ET, \
    V6_CFW_GROUP_MEMBER GM, \
    V6_LIB_ORG_GROUP OG \
   WHERE ET.TRIAL_ID=':trial_id' \
   AND EXP.EXPT_ID=ET.EXPT_ID \
   AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
   AND OG.CFW_ID=GM.CFW_ID \
   AND OG.ORG_GROUP_ID=':group_id'


getMultiplier = \
 SELECT OM.MULTIPLIER FROM \
   V4_EXPT_TRIAL_ORG_MULT OM \
 WHERE \
   OM.TRIAL_ID=':trial_id' \
   AND OM.ORG_GROUP_ID=':group_id'


setMultiplier = \
 UPDATE V4_EXPT_TRIAL_ORG_MULT \
   SET MULTIPLIER= :value \
  WHERE TRIAL_ID=':trial_id' \
  AND ORG_GROUP_ID=':group_id'

nextExperimentId = \
 SELECT EXPERIMENT_NUMBER.NEXTVAL FROM DUAL


cloneExperimentEXPT_EXPERIMENT = \
 INSERT INTO  V4_EXPT_EXPERIMENT \
   (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) \
  SELECT \
   ':new_expt_id' , \
   ':new_name' , \
   ':new_name' , \
   CFW_GROUP_ID \
  FROM \
   V4_EXPT_EXPERIMENT \
  WHERE \
  EXPT_ID=':experiment_id'

cloneExperimentEXPT_TRIAL = \
 INSERT INTO  V4_EXPT_TRIAL \
   (TRIAL_ID, EXPT_ID, DESCRIPTION, NAME) \
  SELECT \
   (':new_expt_id' || '.TRIAL'), \
   ':new_expt_id' , \
   ':new_name' , \
   ':new_name' \
  FROM \
  V4_EXPT_TRIAL WHERE EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIAL_THREAD = \
 INSERT INTO  V4_EXPT_TRIAL_THREAD \
   (EXPT_ID, TRIAL_ID, THREAD_ID) \
  SELECT \
   ':new_expt_id' , \
   (':new_expt_id' || '.TRIAL'), \
   THREAD_ID \
  FROM \
   V4_EXPT_TRIAL_THREAD WHERE EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIAL_ORG_MULT = \
 INSERT INTO  V4_EXPT_TRIAL_ORG_MULT \
   (TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION) \
  SELECT \
   ':new_expt_id', \
   (':new_expt_id' || '.TRIAL'), \
   CFW_ID, \
   ORG_GROUP_ID, \
   MULTIPLIER, \
   DESCRIPTION \
  FROM \
   V4_EXPT_TRIAL_ORG_MULT \
  WHERE \
   EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIAL_ASSEMBLY = \
 INSERT INTO  V4_EXPT_TRIAL_ASSEMBLY \
   (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION) \
  SELECT \
  ':new_expt_id' , \
   (':new_expt_id' || '.TRIAL'), \
   TA.ASSEMBLY_ID, \
   TA.DESCRIPTION \
  FROM \
   V4_EXPT_TRIAL_ASSEMBLY TA, \
   V4_ASB_ASSEMBLY A \
   WHERE \
    EXPT_ID= ':experiment_id' \
    AND TA.ASSEMBLY_ID=A.ASSEMBLY_ID \
    AND A.ASSEMBLY_TYPE <> 'CSM'

