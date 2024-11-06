package com.somle.kingdee.service;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.kingdee.model.*;
import com.somle.framework.common.util.web.WebUtils;
import com.somle.kingdee.model.supplier.KingdeeSupplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;
import static com.somle.kingdee.util.SignatureUtils.*;

/**
 * @author Administrator
 */
@Slf4j
@Data
public class KingdeeClient {

    private KingdeeToken token;


    public KingdeeClient(KingdeeToken token) {
        this.token = token;
    }

    public KingdeeToken refreshAuth() {
        return fillAuth(pushAuth(token));
    }

    public KingdeeToken fillAuth(KingdeeToken token) {
        token.setAppSignature(getAppSignature(token));
        token.setAppToken(getAppToken(token));
        log.info("tokens filled successfully");
        return token;
    }

    public String getAppToken(KingdeeToken token) {
        log.info("preparing app token");
        String appKey = token.getAppKey();
        String appSignature = token.getAppSignature();
        String reqMtd = "GET";
        String ctime = String.valueOf(System.currentTimeMillis());
        String endUrl = "/jdyconnector/app_management/kingdee_auth_token";
        String fullUrl = BASE_HOST + endUrl;
        TreeMap<String, String> params = new TreeMap<>();
        params.put("app_key", appKey);
        params.put("app_signature", appSignature);
        String apiSignature = getApiSignature(reqMtd, endUrl, params, ctime);
        KingdeeResponse response = WebUtils.getRequest(fullUrl, params,  getAuthHeaders(ctime,apiSignature), KingdeeResponse.class);
        return response.getData().getString("app-token");
    }

    public Object getAppToken1(KingdeeToken token) {
        RestTemplate restTemplate = new RestTemplate();
        log.info("preparing app token");
        String appKey = token.getAppKey();
        String appSignature = token.getAppSignature();
        String reqMtd = "GET";
        String ctime = String.valueOf(System.currentTimeMillis());
        String endUrl = "/jdyconnector/app_management/kingdee_auth_token";
        String fullUrl = BASE_HOST + endUrl;
        Map<String, String> params = new HashMap<>();
        params.put("app_key", appKey);
        params.put("app_signature", appSignature);
        String apiSignature = getApiSignature(reqMtd, endUrl, params, ctime);
        //KingdeeResponse response = WebUtils.getRequest(fullUrl, params,  getAuthHeaders(ctime,apiSignature), KingdeeResponse.class);
        //return response.getData().getString("app-token");
        //封装请求头
        HttpEntity<MultiValueMap<String, Object>> formEntity = new HttpEntity<>(getAuthHeaders1(ctime, apiSignature));
        return restTemplate.exchange(fullUrl, HttpMethod.GET, formEntity,Map.class,params);
    }

    public KingdeeToken pushAuth(KingdeeToken token) {
        String outerInstanceId = token.getOuterInstanceId();
        String reqMtd = "POST";
        String ctime = String.valueOf(System.currentTimeMillis());
        String endUrl = "/jdyconnector/app_management/push_app_authorize";
        String fullUrl = BASE_HOST + endUrl;
        TreeMap<String, String> params = new TreeMap<>();
        params.put("outerInstanceId", outerInstanceId);
        String apiSignature = getApiSignature(reqMtd, endUrl, params, ctime);
        KingdeeResponse response = WebUtils.postRequest(fullUrl, params,  getAuthHeaders(ctime,apiSignature), null, KingdeeResponse.class);
        return response.getDataList(KingdeeToken.class).get(0);

    }



    public KingdeeResponse getSupplier() {
        String endUrl = "/jdy/v2/bd/supplier";
        TreeMap<String, String> params = new TreeMap<>();
        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    public KingdeeResponse getVoucher(LocalDate date) {
        String endUrl = "/jdy/v2/fi/voucher";
        TreeMap<String, String> params = new TreeMap<>();
        LocalDateTime starDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
        params.put("create_end_time", String.valueOf(endDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
        params.put("create_start_time", String.valueOf(starDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()));
        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    public KingdeeResponse getVoucherDetail(String id) {
        String endUrl = "/jdy/v2/fi/voucher_detail";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("id", id);
        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    public KingdeeUnit getMeasureUnitByNumber(String number) {
        String endUrl = "/jdy/v2/bd/measure_unit_detail";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("number", number);
        return getResponse(endUrl, params).getData(KingdeeUnit.class);
    }

    public KingdeeResponse getMaterial(String number) {
        String endUrl = "/jdy/v2/bd/material_detail";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", number);
        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    public KingdeeResponse addProduct(KingdeeProduct product) {

        try {
            String id = getMaterial(product.getNumber()).getData().getString("id");
            product.setId(id);
        } catch (Exception e) {
            log.debug("id not found for " + product.getNumber() + "adding new");
        }

        product.setVolumeUnitId(getMeasureUnitByNumber("立方厘米").getId());
        product.setWeightUnitId(getMeasureUnitByNumber("kg").getId());
        product.setBaseUnitId(getMeasureUnitByNumber("套").getId());
        product.setCustomField(
            getCustomFieldByDisplayName("bd_material", "部门"),
            getAuxInfoByNumber(product.getSaleDepartmentId().toString()).getId()
        );
        try {
            product.setCustomField(getCustomFieldByDisplayName("bd_material", "报关品名"), product.getDeclaredTypeZh());
        } catch (Exception e) {
            log.debug("custom field 报关品名 skipped for " + token.getAccountName());
        }
        log.debug("adding product");
        String endUrl = "/jdy/v2/bd/material";
        TreeMap<String, String>  params = new TreeMap<>();
        KingdeeResponse response = postResponse(endUrl, params, product);
        return response;
    }

    public KingdeeResponse addSupplier(KingdeeSupplier kingdeeSupplier) {
        String endUrl = "/jdy/v2/bd/supplier";
        TreeMap<String, String>  params = new TreeMap<>();
        KingdeeResponse response = postResponse(endUrl, params, kingdeeSupplier);
        return response;
    }

    public void addDepartment(KingdeeAuxInfoDetail department) {
        String groupId = getAuxInfoTypeByNumber("BM").getId();
        try {
            String id = getAuxInfoByNumber(department.getNumber()).getId();
            department.setId(id);
        } catch (Exception e) {
            //判断是否存在相同的辅助资料名称
            try {
                String id = getAuxInfoByName(department.getName()).getId();
                department.setId(id);
            } catch (Exception e1) {
            }
        }
        department.setGroupId(groupId);
        KingdeeResponse response = postResponse("/jdy/v2/bd/aux_info",  new TreeMap<>(), department);
    }

    public Stream<KingdeeResponse> list(String endpoint) {
        log.debug("listing");
        return Stream.iterate(1, n -> n + 1)
            .map(n->{
                String endUrl = endpoint;
                TreeMap<String, String>  params = new TreeMap<>();
                params.put("page_size", "100"); //max 100
                params.put("page", String.valueOf(n));
                return getResponse(endUrl, params);
            })
            .takeWhile(n->n.getData().getInteger("page") <= n.getData().getInteger("total_page"));
    }

    public KingdeeResponse post(String endpoint, JSONObject payload) {
        log.debug("posting");
        String endUrl = endpoint;
        TreeMap<String, String>  params = new TreeMap<>();
        KingdeeResponse response = postResponse(endUrl, params, payload);
        return response;
    }

    public KingdeeAuxInfoDetail getAuxInfoByNumber(String number) {
        log.debug("fetching aux info");
        String endUrl = "/jdy/v2/bd/aux_info";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("number", number);
        KingdeeResponse response = getResponse(endUrl, params);
        Optional<KingdeeAuxInfoDetail> first = response.getData().getJSONArray("rows").stream()
                .map(n -> JsonUtils.parseObject(n.toString(), KingdeeAuxInfoDetail.class))
                .filter(n -> n.getNumber().equals(number))
                .findFirst();
        if (first.isPresent()){
            return first.get();
        }
        throw new RuntimeException("您传入的辅助资料信息不存在于kingdee信息库中，请确保erp中的id和辅助资料中的编码一致");
    }

    public KingdeeAuxInfoDetail getAuxInfoByName(String name) {
        log.debug("fetching aux info");
        String endUrl = "/jdy/v2/bd/aux_info";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("name", name);
        KingdeeResponse response = getResponse(endUrl, params);
        return response.getData().getJSONArray("rows").stream()
                .map(n->JsonUtils.parseObject(n.toString(), KingdeeAuxInfoDetail.class))
                .filter(n->n.getName().equals(name))
                .findFirst().get();
    }

    public KingdeeAuxInfoTypeDetail getAuxInfoTypeByNumber(String number) {
        log.debug("fetching aux info");
        String endUrl = "/jdy/v2/bd/aux_info_type";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("number", number);
        KingdeeResponse response = getResponse(endUrl, params);
        return response.getData().getJSONArray("rows").stream()
            .map(n-> JsonUtils.parseObject(n.toString(), KingdeeAuxInfoTypeDetail.class))
            .filter(n->n.getNumber().equals(number))
            .findFirst().get();
    }

    public Stream<KingdeeCustomField> getCustomField(String entity_number) {
        log.debug("fetching custom field");
        String endUrl = "/jdy/v2/sys/custom_field";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("entity_number", entity_number);
        KingdeeResponse response = getResponse(endUrl, params);
        return response.getData().getJSONArray("head").stream().map(n->JsonUtils.parseObject(n.toString(), KingdeeCustomField.class));
    }

    public KingdeeCustomField getCustomFieldByDisplayName(String entity_number, String displayName) {
        return getCustomField(entity_number)
            .filter(n->n.getDisplayName().equals(displayName))
            .findFirst().get();
    }





    private KingdeeResponse fetchResponse(String requestMethod, String endUrl, TreeMap<String, String>  params, Object body) {
        String cts = String.valueOf(System.currentTimeMillis());
        String signature = getApiSignature(requestMethod, endUrl, params, cts);
        Map<String, String> headers = getApiHeaders(cts, signature, token.getAppToken());
        KingdeeResponse response;
        if ("POST".equals(requestMethod)) {
            response = WebUtils.postRequest(BASE_HOST + endUrl, params, headers, body, KingdeeResponse.class);
        } else {
            response = WebUtils.getRequest(BASE_HOST + endUrl, params, headers, KingdeeResponse.class);
        }
        //判断resp的code是否为0，如果不为0，抛出异常，并且异常为信息为description
        String errcode = response.getErrcode();
        if (!Objects.equals(errcode, "0")){
            throw new RuntimeException(response.getDescription());
        }
        return response;
    }

    private KingdeeResponse getResponse(String endUrl, TreeMap<String, String>  params) {
        return fetchResponse("GET", endUrl, params, null);
    }

    private KingdeeResponse postResponse(String endUrl, TreeMap<String, String>  params, Object payload) {
        return fetchResponse("POST", endUrl, params, payload);
    }
}
