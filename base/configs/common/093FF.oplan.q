# Used by glm/src/org/cougaar/mlm/plugin/ldm/OrgActivityQueryHandler
# Note that the word select must be lowercase cause of bad plugin

Database=${org.cougaar.configuration.database}
Username=${org.cougaar.configuration.user}
Password=${org.cougaar.configuration.password}
oplanid = 093FF
activity = ACTIVITY_TYPE
opTempo = OPTEMPO
location = LOCATION

# get AlpLoc info
%AlpLocQueryHandler
AlpLocQuery = select alploc_code, location_name, latitude, longitude from V6_CFW_ALPLOC

# get GeoLoc info
%GeoLocQueryHandler
GeoLocQuery = \
select DISTINCT \
    GEOLOC_CODE, \
    LOCATION_NAME, \
    INSTALLATION_TYPE_CODE, \
    CIVIL_AVIATION_CODE, \
    LATITUDE, \
    LONGITUDE, \
    COUNTRY_STATE_CODE, \
    COUNTRY_STATE_LONG_NAME \
FROM GEOLOC, \
     V4_ASB_OPLAN_AGENT_ATTR \
WHERE ATTRIBUTE_NAME = 'LOCATION' \
    AND GEOLOC_CODE=ATTRIBUTE_VALUE

# get Oplan info
%OplanQueryHandler
OplanInfoQuery = \
select OPERATION_NAME, \
   PRIORITY, \
   C0_DATE \
FROM V4_ASB_OPLAN OPLAN, \
   V4_EXPT_TRIAL_ASSEMBLY ETA \
WHERE OPLAN_ID = '093FF' \
 AND ETA.TRIAL_ID=':exptid' \
 AND ETA.ASSEMBLY_ID=OPLAN.ASSEMBLY_ID

#Get Orgactivities
%OrgActivityQueryHandler

OrgActivityQuery = \
select ATTRIBUTE_NAME AS RELATION_NAME, \
        COMPONENT_ALIB_ID AS FORCE, \
	'FORCE_TYPE' AS FORCE_TYPE, \
	ATTRIBUTE_VALUE AS RELATES_TO, \
	'RELATES_TO_TYPE' AS RELATES_TO_TYPE, \
	START_CDAY AS START_DAY, \
	END_CDAY AS END_DAY, \
	TO_DATE('10-MAY-2001') AS LAST_MODIFIED \
  FROM V4_ASB_OPLAN_AGENT_ATTR \
   WHERE OPLAN_ID = '093FF' \
   AND ASSEMBLY_ID IN \
   (select DISTINCT ASSEMBLY_ID FROM V4_EXPT_TRIAL_ASSEMBLY WHERE TRIAL_ID=':exptid') AND ATTRIBUTE_NAME IN ('ACTIVITY_TYPE','OPTEMPO','LOCATION')

OrgActivityQuery.mysql = \
select DISTINCT ATTRIBUTE_NAME AS RELATION_NAME, \
    COMPONENT_ALIB_ID AS FORCE, \
    'FORCE_TYPE' AS FORCE_TYPE, \
    ATTRIBUTE_VALUE AS RELATES_TO, \
    'RELATES_TO_TYPE' AS RELATES_TO_TYPE, \
    START_CDAY AS START_DAY, \
    END_CDAY AS END_DAY, \
    OP.C0_DATE AS LAST_MODIFIED \
 FROM V4_ASB_OPLAN_AGENT_ATTR ATTR, \
      V4_EXPT_TRIAL_ASSEMBLY ETA, \
      V4_ASB_OPLAN OP \		     
 WHERE \
  ATTR.OPLAN_ID = '093FF' \
  AND OP.OPLAN_ID = '093FF' \
  AND ETA.TRIAL_ID=':exptid' \
  AND ETA.ASSEMBLY_ID=ATTR.ASSEMBLY_ID \
  AND ATTRIBUTE_NAME IN ('ACTIVITY_TYPE','OPTEMPO','LOCATION')

