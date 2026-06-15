package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseHandler {

    private static final String URL = "jdbc:postgresql://localhost:5432/scraper_database";
    private static final String USER = "etl";
    private static final String PASSWORD = "etl";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final List<String> COLUMNS = Arrays.asList(
            "lastUpdateFrom", "lastUpdateTo", "id",
            // RubpEntryInfo
            "regNum", "code", "divisionParentName", "divisionParentCode", "ogrn", "fullName", "shortName", "inn", "kpp", "regDate",
            "okopfName", "okopfCode", "okfsName", "okfsCode", "postIndex", "cityType", "cityName", "streetType", "streetName", "house",
            "oktmoName", "oktmoCode", "orfkName", "orfkCode", "oksmName", "oksmCode", "location", "kbkName", "kbkCode", "okoguName",
            "okoguCode", "okpoCode", "orgTypeName", "orgTypeCode", "establishmentKindName", "establishmentKindCode", "legalPersonKindName",
            "legalPersonKindCode", "ougvName", "ougvCode", "uoName", "uoCode", "creatorKindName", "creatorKindCode", "creatorPlaceName",
            "creatorPlaceCode", "founderKindName", "founderKindCode", "founderPlaceName", "founderPlaceCode", "budgetLvlName",
            "budgetLvlCode", "budgetName", "budgetCode", "statusCode", "statusName", "regionType", "regionName", "isOGV", "isObosob",
            "orgStatus", "recordNum", "parentCode", "parentName", "okatoCode", "okatoName", "guid", "status", "controlNum",
            "bidNum", "firstRegDate", "firstRegGuid", "lastRegGuid", "lastRegDate", "lastRegNum", "updateReason", "updateNum",
            "inclusionDate", "exclusionDate", "pubpCode", "rubpCode", "nubpCode", "cpzCode", "pgmyCode", "firmName", "kofkCode",
            "nameDocs", "accMgmt", "naibznachuch", "regionCode", "areaCode", "areaType", "areaName", "cityCode", "localCode",
            "localName", "localType", "streetCode", "building", "apartment", "reformationDocument", "reformationDocumentNum",
            "reformationDocumentDate", "reformationName", "reformationCode", "reformationStartDate", "reformationEndDate", "dateUpdate",
            "isExcluded", "isReorg", "startDate", "endDate", "loadDate", "regionKladrCode", "egrulnotincluded", "parentrecordnum",
            "planningstructuretype", "planningstructurename", "contourTypeCode", "specEventCode", "speceventcodedop1",
            "speceventcodedop2", "speceventcodedop3", "dsp", "isUch", "reforfkCode",

            // auth_ (RubpEntryAuthority)
            "auth_authorityCode", "auth_authorityName", "auth_perm_permissionCode", "auth_perm_permissionName",
            // act_ (RubpEntryActivity)
            "act_activityCode", "act_activityName", "act_activityKind",
            // head_ (RubpEntryHead)
            "head_fio", "head_post", "head_docName", "head_docNum", "head_docDate", "head_headMain",
            // succ_ (RubpEntrySuccession)
            "succ_parentName", "succ_parentCode", "succ_ogrn", "succ_docname", "succ_numberdoc", "succ_documentdate", "succ_datasource",
            // fa_ (RubpEntryAccountFK)
            "fa_kindName", "fa_kindCode", "fa_num", "fa_createDate", "fa_closeDate", "fa_status", "fa_openUfkCode", "fa_openUfkName",
            "fa_openTofkName", "fa_srvUfkCode", "fa_srvUfkName", "fa_accountorgcode", "fa_accountorgfullname", "fa_ppocode",
            "fa_pponame", "fa_refsrvUfkCode", "fa_refopenUfkCode",
            // fo_ (RubpEntryAccountFO)
            "fo_foName", "fo_foCode", "fo_accountTypeName", "fo_num",
            // part_perm_ (RubpEntryParticipantPermission)
            "part_perm_name", "part_perm_code", "part_perm_startDate", "part_perm_endDate",
            // npart_perm_ (RubpEntryNonParticipantPermission)
            "npart_perm_name", "npart_perm_registryNum", "npart_perm_code", "npart_perm_startDate", "npart_perm_endDate", "npart_perm_authBudgName",
            "npart_perm_authBudgCode", "npart_perm_authPPOName", "npart_perm_authPPOCode", "npart_perm_authKBKGlavaName", "npart_perm_authKBKGlavaCode",
            // proc_perm_ (RubpEntryProcurementPermission)
            "proc_perm_name", "proc_perm_code", "proc_perm_startDate", "proc_perm_endDate",
            // cont_ (RubpEntryContact)
            "cont_phone", "cont_site", "cont_mail",
            // ac_auth_ (RubpEntryAcceptAuth)
            "ac_auth_name", "ac_auth_code", "ac_auth_startDate", "ac_auth_endDate", "ac_auth_authBudgName", "ac_auth_authBudgCode",
            "ac_auth_authPPOName", "ac_auth_authPPOCode", "ac_auth_authKBKGlavaName", "ac_auth_authKBKGlavaCode", "ac_auth_authGiverCode",
            "ac_auth_authGiverName", "ac_auth_userArea", "ac_auth_authRegNum",
            // tr_auth_ (RubpEntryTransfAuth)
            "tr_auth_authfovillagescode", "tr_auth_authfovillagesname", "tr_auth_authfomunicipalcode", "tr_auth_authfomunicipalname",
            "tr_auth_authstartdate", "tr_auth_authenddate", "tr_auth_kbkglavacode", "tr_auth_budgetcode", "tr_auth_authregnum",
            "tr_auth_authfovillagesppocode", "tr_auth_authfovillagespponame",
            // attach_ (RubpEntryAttachment)
            "attach_id", "attach_name", "attach_mime",
            // contr_ (RubpEntryContract)
            "contr_contractnumber", "contr_signdate", "contr_orgcodecontract", "contr_orgnamecontract",
            // ubp_bp_ (RubpEntryUbpTransfAuthbp)
            "ubp_bp_ppocode", "ubp_bp_pponame", "ubp_bp_budlevelnsiname", "ubp_bp_budlevelnsicode", "ubp_bp_budgetnsiname",
            "ubp_bp_budgetnsicode", "ubp_bp_codebk", "ubp_bp_headname",
            // ubp_bu_ (RubpEntryUbpTransfAuthbu)
            "ubp_bu_authbuauthorgcode", "ubp_bu_authbuauthname", "ubp_bu_authbustartdate", "ubp_bu_authbuenddate",
            // fin_ (RubpEntryUbpfin)
            "fin_findocname", "fin_findocnum", "fin_findocdate",
            // ks_ (RubpEntryKsAccount)
            "ks_num", "ks_opendate", "ks_closedate", "ks_opentofkcode", "ks_opentofkname", "ks_refsrvufkcode", "ks_accountvidname", "ks_ppocode"
    );


    public void savePageEntries(Connection connection, JsonNode dataArray, String fromDate, String toDate) throws SQLException {
        if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) return;

        StringJoiner colJoiner = new StringJoiner("\", \"", "\"", "\"");
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        for (String col : COLUMNS) {
            colJoiner.add(col);
            placeholderJoiner.add("?");
        }
        String insertSql = "INSERT INTO epbs_registry (" + colJoiner + ") VALUES (" + placeholderJoiner + ")";

        Timestamp startKey = Timestamp.valueOf(java.time.LocalDate.parse(fromDate, inputFormatter).atStartOfDay());
        Timestamp endKey = Timestamp.valueOf(java.time.LocalDate.parse(toDate, inputFormatter).atStartOfDay());

        boolean originalAutoCommit = connection.getAutoCommit();
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);

            for (JsonNode item : dataArray) {
                JsonNode info = item.get("info");
                if (info == null) continue;

                Map<String, Object> baseFields = new HashMap<>();
                baseFields.put("lastUpdateFrom", startKey);
                baseFields.put("lastUpdateTo", endKey);
                baseFields.put("id", getStr(item, "id"));

                Iterator<Map.Entry<String, JsonNode>> fields = info.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String key = field.getKey();
                    JsonNode val = field.getValue();

                    if (val.isNull()) continue;

                    String textValue = val.asText().trim();

                    if (textValue.isEmpty()) {
                        baseFields.put(key, null);
                        continue;
                    }

                    if (key.equals("exclusionDate") || key.equals("inclusionDate") || key.equals("dateUpdate")
                            || (key.toLowerCase().contains("date") && !key.equals("updateNum") && !key.equals("updateReason"))) {

                        baseFields.put(key, getTimestamp(info, key));

                    } else {
                        baseFields.put(key, textValue);
                    }
                }

                List<Map<String, Object>> authFlatList = new ArrayList<>();
                JsonNode authorities = item.get("authorities");
                if (authorities != null && authorities.isArray()) {
                    for (JsonNode auth : authorities) {
                        JsonNode perms = auth.get("permissions");
                        if (perms != null && perms.isArray() && perms.size() > 0) {
                            for (JsonNode perm : perms) {
                                Map<String, Object> m = new HashMap<>();
                                m.put("auth_authorityCode", getStr(auth, "authorityCode"));
                                m.put("auth_authorityName", getStr(auth, "authorityName"));
                                m.put("auth_perm_permissionCode", getStr(perm, "permissionCode"));
                                m.put("auth_perm_permissionName", getStr(perm, "permissionName"));
                                authFlatList.add(m);
                            }
                        } else {
                            Map<String, Object> m = new HashMap<>();
                            m.put("auth_authorityCode", getStr(auth, "authorityCode"));
                            m.put("auth_authorityName", getStr(auth, "authorityName"));
                            authFlatList.add(m);
                        }
                    }
                }

                JsonNode activities = item.get("activities");
                JsonNode heads = item.get("heads");
                JsonNode successions = item.get("successions");
                JsonNode facialAccounts = item.get("facialAccounts");
                JsonNode foAccounts = item.get("foAccounts");
                JsonNode partPerms = item.get("participantPermissions");
                JsonNode npartPerms = item.get("nonParticipantPermissions");
                JsonNode procPerms = item.get("procurementPermissions");
                JsonNode contacts = item.get("contacts");
                JsonNode acceptAuths = item.get("acceptAuths");
                JsonNode transfauth = item.get("transfauth");
                JsonNode attachment = item.get("attachment");
                JsonNode contracts = item.get("contracts");
                JsonNode ubptransfauthbp = item.get("ubptransfauthbp");
                JsonNode ubptransfauthbu = item.get("ubptransfauthbu");
                JsonNode ubpfin = item.get("ubpfin");
                JsonNode ksaccounts = item.get("ksaccounts");

                int maxRows = 1;
                maxRows = Math.max(maxRows, !authFlatList.isEmpty() ? authFlatList.size() : 1);
                maxRows = Math.max(maxRows, (activities != null && activities.isArray()) ? activities.size() : 1);
                maxRows = Math.max(maxRows, (heads != null && heads.isArray()) ? heads.size() : 1);
                maxRows = Math.max(maxRows, (successions != null && successions.isArray()) ? successions.size() : 1);
                maxRows = Math.max(maxRows, (facialAccounts != null && facialAccounts.isArray()) ? facialAccounts.size() : 1);
                maxRows = Math.max(maxRows, (foAccounts != null && foAccounts.isArray()) ? foAccounts.size() : 1);
                maxRows = Math.max(maxRows, (partPerms != null && partPerms.isArray()) ? partPerms.size() : 1);
                maxRows = Math.max(maxRows, (npartPerms != null && npartPerms.isArray()) ? npartPerms.size() : 1);
                maxRows = Math.max(maxRows, (procPerms != null && procPerms.isArray()) ? procPerms.size() : 1);
                maxRows = Math.max(maxRows, (contacts != null && contacts.isArray()) ? contacts.size() : 1);
                maxRows = Math.max(maxRows, (acceptAuths != null && acceptAuths.isArray()) ? acceptAuths.size() : 1);
                maxRows = Math.max(maxRows, (transfauth != null && transfauth.isArray()) ? transfauth.size() : 1);
                maxRows = Math.max(maxRows, (attachment != null && attachment.isArray()) ? attachment.size() : 1);
                maxRows = Math.max(maxRows, (contracts != null && contracts.isArray()) ? contracts.size() : 1);
                maxRows = Math.max(maxRows, (ubptransfauthbp != null && ubptransfauthbp.isArray()) ? ubptransfauthbp.size() : 1);
                maxRows = Math.max(maxRows, (ubptransfauthbu != null && ubptransfauthbu.isArray()) ? ubptransfauthbu.size() : 1);
                maxRows = Math.max(maxRows, (ubpfin != null && ubpfin.isArray()) ? ubpfin.size() : 1);
                maxRows = Math.max(maxRows, (ksaccounts != null && ksaccounts.isArray()) ? ksaccounts.size() : 1);

                for (int i = 0; i < maxRows; i++) {
                    Map<String, Object> flatRow = new HashMap<>(baseFields);

                    if (!authFlatList.isEmpty() && i < authFlatList.size()) flatRow.putAll(authFlatList.get(i));

                    if (activities != null && activities.isArray() && i < activities.size()) {
                        JsonNode n = activities.get(i);
                        flatRow.put("act_activityCode", getStr(n, "activityCode"));
                        flatRow.put("act_activityName", getStr(n, "activityName"));
                        flatRow.put("act_activityKind", getStr(n, "activityKind"));
                    }
                    if (heads != null && heads.isArray() && i < heads.size()) {
                        JsonNode n = heads.get(i);
                        flatRow.put("head_fio", getStr(n, "fio"));
                        flatRow.put("head_post", getStr(n, "post"));
                        flatRow.put("head_docName", getStr(n, "docName"));
                        flatRow.put("head_docNum", getStr(n, "docNum"));
                        flatRow.put("head_docDate", getTimestamp(n, "docDate"));
                        flatRow.put("head_headMain", getStr(n, "headMain"));
                    }
                    if (successions != null && successions.isArray() && i < successions.size()) {
                        JsonNode n = successions.get(i);
                        flatRow.put("succ_parentName", getStr(n, "parentName"));
                        flatRow.put("succ_parentCode", getStr(n, "parentCode"));
                        flatRow.put("succ_ogrn", getStr(n, "ogrn"));
                        flatRow.put("succ_docname", getStr(n, "docname"));
                        flatRow.put("succ_numberdoc", getStr(n, "numberdoc"));
                        flatRow.put("succ_documentdate", getTimestamp(n, "documentdate"));
                        flatRow.put("succ_datasource", getStr(n, "datasource"));
                    }
                    if (facialAccounts != null && facialAccounts.isArray() && i < facialAccounts.size()) {
                        JsonNode n = facialAccounts.get(i);
                        flatRow.put("fa_kindName", getStr(n, "kindName"));
                        flatRow.put("fa_kindCode", getStr(n, "kindCode"));
                        flatRow.put("fa_num", getStr(n, "num"));
                        flatRow.put("fa_createDate", getTimestamp(n, "createDate"));
                        flatRow.put("fa_closeDate", getTimestamp(n, "closeDate"));
                        flatRow.put("fa_status", getStr(n, "status"));
                        flatRow.put("fa_openUfkCode", getStr(n, "openUfkCode"));
                        flatRow.put("fa_openUfkName", getStr(n, "openUfkName"));
                        flatRow.put("fa_openTofkName", getStr(n, "openTofkName"));
                        flatRow.put("fa_srvUfkCode", getStr(n, "srvUfkCode"));
                        flatRow.put("fa_srvUfkName", getStr(n, "srvUfkName"));
                        flatRow.put("fa_accountorgcode", getStr(n, "accountorgcode"));
                        flatRow.put("fa_accountorgfullname", getStr(n, "accountorgfullname"));
                        flatRow.put("fa_ppocode", getStr(n, "ppocode"));
                        flatRow.put("fa_pponame", getStr(n, "pponame"));
                        flatRow.put("fa_refsrvUfkCode", getStr(n, "refsrvUfkCode"));
                        flatRow.put("fa_refopenUfkCode", getStr(n, "refopenUfkCode"));
                    }
                    if (foAccounts != null && foAccounts.isArray() && i < foAccounts.size()) {
                        JsonNode n = foAccounts.get(i);
                        flatRow.put("fo_foName", getStr(n, "foName"));
                        flatRow.put("fo_foCode", getStr(n, "foCode"));
                        flatRow.put("fo_accountTypeName", getStr(n, "accountTypeName"));
                        flatRow.put("fo_num", getStr(n, "num"));
                    }
                    if (partPerms != null && partPerms.isArray() && i < partPerms.size()) {
                        JsonNode n = partPerms.get(i);
                        flatRow.put("part_perm_name", getStr(n, "name"));
                        flatRow.put("part_perm_code", getStr(n, "code"));
                        flatRow.put("part_perm_startDate", getTimestamp(n, "startDate"));
                        flatRow.put("part_perm_endDate", getTimestamp(n, "endDate"));
                    }
                    if (npartPerms != null && npartPerms.isArray() && i < npartPerms.size()) {
                        JsonNode n = npartPerms.get(i);
                        flatRow.put("npart_perm_name", getStr(n, "name"));
                        flatRow.put("npart_perm_registryNum", getStr(n, "registryNum"));
                        flatRow.put("npart_perm_code", getStr(n, "code"));
                        flatRow.put("npart_perm_startDate", getTimestamp(n, "startDate"));
                        flatRow.put("npart_perm_endDate", getTimestamp(n, "endDate"));
                        flatRow.put("npart_perm_authBudgName", getStr(n, "authBudgName"));
                        flatRow.put("npart_perm_authBudgCode", getStr(n, "authBudgCode"));
                        flatRow.put("npart_perm_authPPOName", getStr(n, "authPPOName"));
                        flatRow.put("npart_perm_authPPOCode", getStr(n, "authPPOCode"));
                        flatRow.put("npart_perm_authKBKGlavaName", getStr(n, "authKBKGlavaName"));
                        flatRow.put("npart_perm_authKBKGlavaCode", getStr(n, "authKBKGlavaCode"));
                    }
                    if (procPerms != null && procPerms.isArray() && i < procPerms.size()) {
                        JsonNode n = procPerms.get(i);
                        flatRow.put("proc_perm_name", getStr(n, "name"));
                        flatRow.put("proc_perm_code", getStr(n, "code"));
                        flatRow.put("proc_perm_startDate", getTimestamp(n, "startDate"));
                        flatRow.put("proc_perm_endDate", getTimestamp(n, "endDate"));
                    }
                    if (contacts != null && contacts.isArray() && i < contacts.size()) {
                        JsonNode n = contacts.get(i);
                        flatRow.put("cont_phone", getStr(n, "phone"));
                        flatRow.put("cont_site", getStr(n, "site"));
                        flatRow.put("cont_mail", getStr(n, "mail"));
                    }
                    if (acceptAuths != null && acceptAuths.isArray() && i < acceptAuths.size()) {
                        JsonNode n = acceptAuths.get(i);
                        flatRow.put("ac_auth_name", getStr(n, "name"));
                        flatRow.put("ac_auth_code", getStr(n, "code"));
                        flatRow.put("ac_auth_startDate", getTimestamp(n, "startDate"));
                        flatRow.put("ac_auth_endDate", getTimestamp(n, "endDate"));
                        flatRow.put("ac_auth_authBudgName", getStr(n, "authBudgName"));
                        flatRow.put("ac_auth_authBudgCode", getStr(n, "authBudgCode"));
                        flatRow.put("ac_auth_authPPOName", getStr(n, "authPPOName"));
                        flatRow.put("ac_auth_authPPOCode", getStr(n, "authPPOCode"));
                        flatRow.put("ac_auth_authKBKGlavaName", getStr(n, "authKBKGlavaName"));
                        flatRow.put("ac_auth_authKBKGlavaCode", getStr(n, "authKBKGlavaCode"));
                        flatRow.put("ac_auth_authGiverCode", getStr(n, "authGiverCode"));
                        flatRow.put("ac_auth_authGiverName", getStr(n, "authGiverName"));
                        flatRow.put("ac_auth_userArea", getStr(n, "userArea"));
                        flatRow.put("ac_auth_authRegNum", getStr(n, "authRegNum"));
                    }
                    if (transfauth != null && transfauth.isArray() && i < transfauth.size()) {
                        JsonNode n = transfauth.get(i);
                        flatRow.put("tr_auth_authfovillagescode", getStr(n, "authfovillagescode"));
                        flatRow.put("tr_auth_authfovillagesname", getStr(n, "authfovillagesname"));
                        flatRow.put("tr_auth_authfomunicipalcode", getStr(n, "authfomunicipalcode"));
                        flatRow.put("tr_auth_authfomunicipalname", getStr(n, "authfomunicipalname"));
                        flatRow.put("tr_auth_authstartdate", getTimestamp(n, "authstartdate"));
                        flatRow.put("tr_auth_authenddate", getTimestamp(n, "authenddate"));
                        flatRow.put("tr_auth_kbkglavacode", getStr(n, "kbkglavacode"));
                        flatRow.put("tr_auth_budgetcode", getStr(n, "budgetcode"));
                        flatRow.put("tr_auth_authregnum", getStr(n, "authregnum"));
                        flatRow.put("tr_auth_authfovillagesppocode", getStr(n, "authfovillagesppocode"));
                        flatRow.put("tr_auth_authfovillagespponame", getStr(n, "authfovillagespponame"));
                    }
                    if (attachment != null && attachment.isArray() && i < attachment.size()) {
                        JsonNode n = attachment.get(i);
                        flatRow.put("attach_id", getStr(n, "id"));
                        flatRow.put("attach_name", getStr(n, "name"));
                        flatRow.put("attach_mime", getStr(n, "mime"));
                    }
                    if (contracts != null && contracts.isArray() && i < contracts.size()) {
                        JsonNode n = contracts.get(i);
                        flatRow.put("contr_contractnumber", getStr(n, "contractnumber"));
                        flatRow.put("contr_signdate", getTimestamp(n, "signdate"));
                        flatRow.put("contr_orgcodecontract", getStr(n, "orgcodecontract"));
                        flatRow.put("contr_orgnamecontract", getStr(n, "orgnamecontract"));
                    }
                    if (ubptransfauthbp != null && ubptransfauthbp.isArray() && i < ubptransfauthbp.size()) {
                        JsonNode n = ubptransfauthbp.get(i);
                        flatRow.put("ubp_bp_ppocode", getStr(n, "ppocode"));
                        flatRow.put("ubp_bp_pponame", getStr(n, "pponame"));
                        flatRow.put("ubp_bp_budlevelnsiname", getStr(n, "budlevelnsiname"));
                        flatRow.put("ubp_bp_budlevelnsicode", getStr(n, "budlevelnsicode"));
                        flatRow.put("ubp_bp_budgetnsiname", getStr(n, "budgetnsiname"));
                        flatRow.put("ubp_bp_budgetnsicode", getStr(n, "budgetnsicode"));
                        flatRow.put("ubp_bp_codebk", getStr(n, "codebk"));
                        flatRow.put("ubp_bp_headname", getStr(n, "headname"));
                    }
                    if (ubptransfauthbu != null && ubptransfauthbu.isArray() && i < ubptransfauthbu.size()) {
                        JsonNode n = ubptransfauthbu.get(i);
                        flatRow.put("ubp_bu_authbuauthorgcode", getStr(n, "authbuauthorgcode"));
                        flatRow.put("ubp_bu_authbuauthname", getStr(n, "authbuauthname"));
                        flatRow.put("ubp_bu_authbustartdate", getTimestamp(n, "authbustartdate"));
                        flatRow.put("ubp_bu_authbuenddate", getTimestamp(n, "authbuenddate"));
                    }
                    if (ubpfin != null && ubpfin.isArray() && i < ubpfin.size()) {
                        JsonNode n = ubpfin.get(i);
                        flatRow.put("fin_findocname", getStr(n, "findocname"));
                        flatRow.put("fin_findocnum", getStr(n, "findocnum"));
                        flatRow.put("fin_findocdate", getTimestamp(n, "findocdate"));
                    }
                    if (ksaccounts != null && ksaccounts.isArray() && i < ksaccounts.size()) {
                        JsonNode n = ksaccounts.get(i);
                        flatRow.put("ks_num", getStr(n, "num"));
                        flatRow.put("ks_opendate", getTimestamp(n, "opendate"));
                        flatRow.put("ks_closedate", getTimestamp(n, "closedate"));
                        flatRow.put("ks_opentofkcode", getStr(n, "opentofkcode"));
                        flatRow.put("ks_opentofkname", getStr(n, "opentofkname"));
                        flatRow.put("ks_refsrvufkcode", getStr(n, "refsrvufkcode"));
                        flatRow.put("ks_accountvidname", getStr(n, "accountvidname"));
                        flatRow.put("ks_ppocode", getStr(n, "ppocode"));
                    }

                    int p = 1;
                    for (String colName : COLUMNS) {
                        Object value = flatRow.get(colName);

                        if (value == null) {
                            if (colName.toLowerCase().contains("date") || colName.equals("dateUpdate")) {
                                insertStmt.setNull(p++, Types.TIMESTAMP);
                            } else {
                                insertStmt.setNull(p++, Types.VARCHAR);
                            }
                        } else if (value instanceof Timestamp) {
                            insertStmt.setTimestamp(p++, (Timestamp) value);
                        } else {
                            insertStmt.setString(p++, value.toString());
                        }
                    }
                    insertStmt.addBatch();
                }
            }

            insertStmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public String getStr(JsonNode node, String key) {
        if (node == null || !node.has(key) || node.get(key).isNull()) {
            return null;
        }
        String val = node.get(key).asText().trim();
        return val.isEmpty() ? null : val;
    }

    public Timestamp getTimestamp(JsonNode node, String key) {
        if (node == null || !node.has(key) || node.get(key).isNull()) {
            return null;
        }
        String dateStr = node.get(key).asText().trim();
        if (dateStr.isEmpty()) {
            return null;
        }

        try {
            return Timestamp.valueOf(dateStr);
        } catch (Exception e) {
            try {
                return Timestamp.from(java.time.Instant.parse(dateStr));
            } catch (Exception ex) {
                System.err.println("Не удалось распарсить дату для ключа " + key + " со значением: " + dateStr);
                return null;
            }
        }
    }
}