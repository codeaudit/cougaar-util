database = ${icis.database}
username = ${icis.database.user}
password = ${icis.database.password}
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

# ACR Multipliers
#
multipliersQuery=select supply_type, optempo, orgactivity, multiplier from acr_multipliers where supply_type = :supType and orgactivity = 'OFFENSIVE' order by orgactivity

# ACR Adjustments
#
adjustmentsQuery=select supply_type, optempo, orgactivity, multiplier from acr_multipliers where supply_type = :supType and (orgactivity = 'DEPLOYMENT' or orgactivity = 'STAND_DOWN') order by orgactivity
