Database=${org.cougaar.configuration.database}
Username=${org.cougaar.configuration.user}
Password=${org.cougaar.configuration.password}
oplanid = 093FF
# get AlpLoc info
%AlpLocQueryHandler
AlpLocQuery = select alploc_code, location_name, latitude, longitude from V6_CFW_ALPLOC

# get GeoLoc info
%GeoLocQueryHandler
GeoLocQuery = select geoloc_code, location_name, installation_type_code, civil_aviation_code, latitude, longitude, country_state_code, country_state_long_name from geoloc where geoloc_code in (select attribute_value from v4_asb_oplan_agent_attr where attribute_name = 'LOCATION')


# get Oplan info
%OplanQueryHandler
OplanInfoQuery = select operation_name, priority, c0_date from v4_asb_oplan where oplan_id = ':oplanid' and assembly_id in (select distinct assembly_id from v4_expt_trial_assembly where trial_id=':exptid')

#get OrgActivities
%OrgActivityQueryHandler
activity = ACTIVITY_TYPE
opTempo = OPTEMPO
location = LOCATION
OrgActivityQuery = select attribute_name as relation_name, component_alib_id as force, 'FORCE_TYPE' as force_type, attribute_value as relates_to, 'RELATES_TO_TYPE' as relates_to_type, start_cday as start_day, end_cday as end_day, to_date('10-MAY-2001') as last_modified from v4_asb_oplan_agent_attr where oplan_id = ':oplanid' and assembly_id in (select distinct assembly_id from v4_expt_trial_assembly where trial_id=':exptid') and attribute_name in (':activity',':opTempo',':location')

