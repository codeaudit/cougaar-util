Database=${org.cougaar.configuration.database}
Username=${org.cougaar.configuration.user}
Password=${org.cougaar.configuration.password}

%SQLFDMAggregateAssetCreator
query= select \
    fue.unit_identifier as uic, \
    fue.unit_equipment_qty as qty, \
    fted.materiel_item_identifier as nsn, \
    fue.ti_id as ti_id, \
    fted.tid_id as tid_id, \
    fted.shppng_cnfgrtn_cd, \
    substr(fte.ti_nm,1,30) as ti_nm, \
    tid_lg_dm as length, \
    tid_wdth_dm as width, \
    tid_ht_dm as height, \
    tid_wt as weight, \
    tid_vl as volume, \
    fted.cgo_tp_cd as cgo_tp_cd, \
    fted.cgo_xtnt_cd as cgo_xtnt_cd, \
    fted.cgo_cntzn_cd as cgo_cntzn_cd, \
    fted.materiel_item_identifier as materiel_item_identifier, \
    fted.type_pack_code as type_pack_code, \
    fted.tid_eq_ty_cd as tid_eq_ty_cd, \
    tid_ftprnt_ar as footprint \
from \
    fdm_unit_equipment fue, \
    fdm_transportable_item fte, \
    fdm_transportable_item_detail fted, \
    v6_lib_organization liborg \
where \
liborg.org_id=:agent \
and fue.unit_identifier = liborg.uic \
and fue.ti_id = fte.ti_id \
and fue.ti_id = fted.ti_id \
and fted.tid_id = '01' \
and fted.cgo_tp_cd in ('A', 'B', 'C', 'K', 'R') \
and fted.materiel_item_identifier <> '0000000000000' \
order by \
    fue.unit_identifier, \
    fue.unit_equipment_qty asc

%SQLAggregateAssetCreator
query = select CAPABILITY AS MOS_LEVEL, PERSONNEL AS MOS_QTY, 'Dummy Nomenclature' \
	AS "Dummy Nomenclature" \
	from ORG_MOS, \
	v6_lib_organization liborg \
	where \
	liborg.org_id=:agent \
	and org_mos.uic = liborg.uic

# Then, get the containers and generate an aggregate asset
%SQLAggregateAssetCreator
query = select '8115001682275' AS NSN, container_20_ft_qty AS QTY_OH, 'Container' AS \
	NOMENCLATURE \
	from ue_summary_mtmc ues, \
	v6_lib_organization liborg \
	where \
	liborg.org_id=:agent \
	and ues.uic = liborg.uic















