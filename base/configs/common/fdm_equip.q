Database=${org.cougaar.configuration.database}
Username=${org.cougaar.configuration.user}
Password=${org.cougaar.configuration.password}

%SQLFDMAggregateAssetCreator
query= SELECT \
    FUE.UNIT_IDENTIFIER AS UIC, \
    FUE.UNIT_EQUIPMENT_QTY AS QTY, \
    FTED.MATERIEL_ITEM_IDENTIFIER AS NSN, \
    FUE.TI_ID AS TI_ID, \
    FTED.TID_ID AS TID_ID, \
    FTED.SHPPNG_CNFGRTN_CD, \
    SUBSTR(FTE.TI_NM,1,30) AS TI_NM, \
    TID_LG_DM AS LENGTH, \
    TID_WDTH_DM AS WIDTH, \
    TID_HT_DM AS HEIGHT, \
    TID_WT AS WEIGHT, \
    TID_VL AS VOLUME, \
    FTED.CGO_TP_CD AS CGO_TP_CD, \
    FTED.CGO_XTNT_CD AS CGO_XTNT_CD, \
    FTED.CGO_CNTZN_CD AS CGO_CNTZN_CD, \
    FTED.MATERIEL_ITEM_IDENTIFIER AS MATERIEL_ITEM_IDENTIFIER, \
    FTED.TYPE_PACK_CODE AS TYPE_PACK_CODE, \
    FTED.TID_EQ_TY_CD AS TID_EQ_TY_CD, \
    TID_FTPRNT_AR AS FOOTPRINT \
FROM \
    FDM_UNIT_EQUIPMENT FUE, \
    FDM_TRANSPORTABLE_ITEM FTE, \
    FDM_TRANSPORTABLE_ITEM_DETAIL FTED, \
    V6_LIB_ORGANIZATION LIBORG \
WHERE \
LIBORG.ORG_ID=:agent \
AND FUE.UNIT_IDENTIFIER = LIBORG.UIC \
AND FUE.TI_ID = FTE.TI_ID \
AND FUE.TI_ID = FTED.TI_ID \
AND FTED.TID_ID = '01' \
AND FTED.CGO_TP_CD IN ('A', 'B', 'C', 'K', 'R') \
AND FTED.MATERIEL_ITEM_IDENTIFIER <> '0000000000000' \
ORDER BY \
    FUE.UNIT_IDENTIFIER, \
    FUE.UNIT_EQUIPMENT_QTY ASC


query.mysql= SELECT \
    FUE.UNIT_IDENTIFIER AS UIC, \
    FUE.UNIT_EQUIPMENT_QTY AS QTY, \
    FTED.MATERIEL_ITEM_IDENTIFIER AS NSN, \
    FUE.TI_ID AS TI_ID, \
    FTED.TID_ID AS TID_ID, \
    FTED.SHPPNG_CNFGRTN_CD, \
    SUBSTRING(FTE.TI_NM,1,30) AS TI_NM, \
    TID_LG_DM AS LENGTH, \
    TID_WDTH_DM AS WIDTH, \
    TID_HT_DM AS HEIGHT, \
    TID_WT AS WEIGHT, \
    TID_VL AS VOLUME, \
    FTED.CGO_TP_CD AS CGO_TP_CD, \
    FTED.CGO_XTNT_CD AS CGO_XTNT_CD, \
    FTED.CGO_CNTZN_CD AS CGO_CNTZN_CD, \
    FTED.MATERIEL_ITEM_IDENTIFIER AS MATERIEL_ITEM_IDENTIFIER, \
    FTED.TYPE_PACK_CODE AS TYPE_PACK_CODE, \
    FTED.TID_EQ_TY_CD AS TID_EQ_TY_CD, \
    TID_FTPRNT_AR AS FOOTPRINT \
FROM \
    FDM_UNIT_EQUIPMENT FUE, \
    FDM_TRANSPORTABLE_ITEM FTE, \
    FDM_TRANSPORTABLE_ITEM_DETAIL FTED, \
    V6_LIB_ORGANIZATION LIBORG \
WHERE \
LIBORG.ORG_ID=:agent \
AND FUE.UNIT_IDENTIFIER = LIBORG.UIC \
AND FUE.TI_ID = FTE.TI_ID \
AND FUE.TI_ID = FTED.TI_ID \
AND FTED.TID_ID = '01' \
AND FTED.CGO_TP_CD IN ('A', 'B', 'C', 'K', 'R') \
AND FTED.MATERIEL_ITEM_IDENTIFIER <> '0000000000000' \
ORDER BY \
    FUE.UNIT_IDENTIFIER, \
    FUE.UNIT_EQUIPMENT_QTY ASC

%SQLMOSAggregateAssetCreator
#query = select ('MOS/' || billet.unfrmd_srvc_occptn_cd) AS MOS_LEVEL, sum(to_strength) AS MOS_QTY, unfrmd_srvc_occptn_tx  \
#	from \ 
#	fdm_unit_billet billet, \
#	fdm_unfrmd_srvc_occptn occ, \
#	v6_lib_organization liborg \
#	where \
#	liborg.org_id=:agent \
#	and billet.unfrmd_srvc_occptn_cd=occ.unfrmd_srvc_occptn_cd \
#	and occ.rank_subcategory_code(+)='E' \
#	and billet.unit_identifier = liborg.uic \
#	group by billet.unfrmd_srvc_occptn_cd, unfrmd_srvc_occptn_tx

query = SELECT 'MOS/11B' AS MOS_LEVEL, NVL(SUM(TO_STRENGTH),0) AS MOS_QTY, 'MOS/11B/INFANTRYMAN'  \
	FROM \ 
	FDM_UNIT_BILLET BILLET, \
	FDM_UNFRMD_SRVC_OCCPTN OCC, \
	V6_LIB_ORGANIZATION LIBORG \
	WHERE \
	LIBORG.ORG_ID=:agent \
	AND BILLET.UNFRMD_SRVC_OCCPTN_CD=OCC.UNFRMD_SRVC_OCCPTN_CD \
	AND OCC.RANK_SUBCATEGORY_CODE(+)='E' \
	AND BILLET.UNIT_IDENTIFIER = LIBORG.UIC


#query.mysql = \
#select concat('MOS/',billet.unfrmd_srvc_occptn_cd) AS MOS_LEVEL, sum(to_strength) AS MOS_QTY, unfrmd_srvc_occptn_tx  \
#  from \ 
#   fdm_unit_billet billet, \
#   v6_lib_organization liborg \
#   left join fdm_unfrmd_srvc_occptn occ on \
#     (billet.unfrmd_srvc_occptn_cd=occ.unfrmd_srvc_occptn_cd \
#       and occ.rank_subcategory_code='E') \
#  where \
#   liborg.org_id=:agent \
#   and billet.unit_identifier = liborg.uic \
#   group by billet.unfrmd_srvc_occptn_cd, unfrmd_srvc_occptn_tx
#

query.mysql = \
SELECT 'MOS/!!B' AS MOS_LEVEL, IFNULL(SUM(TO_STRENGTH),0) AS MOS_QTY,  'MOS/11B/INFANTRYMAN'  \
  FROM \ 
   FDM_UNIT_BILLET BILLET, \
   V6_LIB_ORGANIZATION LIBORG \
   LEFT JOIN FDM_UNFRMD_SRVC_OCCPTN OCC ON \
     (BILLET.UNFRMD_SRVC_OCCPTN_CD=OCC.UNFRMD_SRVC_OCCPTN_CD \
       AND OCC.RANK_SUBCATEGORY_CODE='E') \
  WHERE \
   LIBORG.ORG_ID=:agent \
   AND BILLET.UNIT_IDENTIFIER = LIBORG.UIC

# Then, get the containers and generate an aggregate asset
%SQLAggregateAssetCreator
query = SELECT '8115001682275' AS NSN, DECODE(CONTAINER_20_FT_QTY,NULL,30,CONTAINER_20_FT_QTY) AS QTY_OH, 'CONTAINER' AS \
	NOMENCLATURE \
	FROM UE_SUMMARY_MTMC UES, \
	V6_LIB_ORGANIZATION LIBORG \
	WHERE \
	LIBORG.ORG_ID=:agent \
	AND UES.UIC(+) = LIBORG.UIC


query.mysql = SELECT DISTINCT '8115001682275' AS NSN, \
        30 AS QTY_OH, \
	'CONTAINER' AS 	NOMENCLATURE \
	FROM \
	V6_LIB_ORGANIZATION LIBORG \
	WHERE \
	LIBORG.ORG_ID=:agent


