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
select distinct \
    geoloc_code, \
    location_name, \
    installation_type_code, \
    civil_aviation_code, \
    latitude, \
    longitude, \
    country_state_code, \
    country_state_long_name \
from geoloc, \
     v4_asb_oplan_agent_attr \
where attribute_name = 'LOCATION' \
    and geoloc_code=attribute_value

# get Oplan info
%OplanQueryHandler
OplanInfoQuery = \
select operation_name, \
   priority, \
   c0_date \
from v4_asb_oplan oplan, \
   v4_expt_trial_assembly eta \
where oplan_id = '093FF' \
 and eta.trial_id=':exptid' \
 and eta.assembly_id=oplan.assembly_id

#get OrgActivities
%OrgActivityQueryHandler

OrgActivityQuery = \
select attribute_name as relation_name, \
        component_alib_id as force, \
	'FORCE_TYPE' as force_type, \
	attribute_value as relates_to, \
	'RELATES_TO_TYPE' as relates_to_type, \
	start_cday as start_day, \
	end_cday as end_day, \
	to_date('10-MAY-2001') as last_modified \
  from v4_asb_oplan_agent_attr \
   where oplan_id = '093FF' \
   and assembly_id in \
   (select distinct assembly_id from v4_expt_trial_assembly where trial_id=':exptid') and attribute_name in ('ACTIVITY_TYPE','OPTEMPO','LOCATION')

OrgActivityQuery.mysql = \
select distinct attribute_name as relation_name, \
    component_alib_id as force, \
    'FORCE_TYPE' as force_type, \
    attribute_value as relates_to, \
    'RELATES_TO_TYPE' as relates_to_type, \
    start_cday as start_day, \
    end_cday as end_day, \
    op.c0_date as last_modified \
 from v4_asb_oplan_agent_attr attr, \
      v4_expt_trial_assembly eta, \
      v4_asb_oplan op \		     
 where \
  attr.oplan_id = '093FF' \
  and op.oplan_id = '093FF' \
  and eta.trial_id=':exptid' \
  and eta.assembly_id=attr.assembly_id \
  and attribute_name in ('ACTIVITY_TYPE','OPTEMPO','LOCATION')

