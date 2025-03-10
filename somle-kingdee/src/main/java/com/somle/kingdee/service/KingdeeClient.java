package com.somle.kingdee.service;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.kingdee.model.*;
import com.somle.kingdee.model.supplier.KingdeeSupplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

    /**
     * 根据OuterInstanceId覆盖内存的KingdeeToken
     *
     * @return KingdeeToken
     */
    protected KingdeeToken refreshAuth() {
        return fillAuth(pushAuth(token.getOuterInstanceId()));
    }

    private KingdeeToken fillAuth(KingdeeToken newToken) {
        String signature = getAppSignature(newToken);
        token.setAppSignature(signature);
        newToken.setAppSignature(signature);//响应返回值没有签名->需要计算

        token.setAppToken(getAppToken(newToken));
        log.info("tokens filled successfully");
        return token;
    }

    private String getAppToken(KingdeeToken token) {
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
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(params)
            .headers(getAuthHeaders(ctime, apiSignature))
            .build();
        KingdeeResponse response = WebUtils.sendRequest(request, KingdeeResponse.class);
        return response.getData(JSONObject.class).getString("app-token");
    }

    private KingdeeToken pushAuth(String outerInstanceId) {
        String reqMtd = "POST";
        String ctime = String.valueOf(System.currentTimeMillis());
        String endUrl = "/jdyconnector/app_management/push_app_authorize";
        String fullUrl = BASE_HOST + endUrl;
        TreeMap<String, String> params = new TreeMap<>();
        params.put("outerInstanceId", outerInstanceId);
        String apiSignature = getApiSignature(reqMtd, endUrl, params, ctime);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(fullUrl)
            .queryParams(params)
            .headers(getAuthHeaders(ctime, apiSignature))
            .build();
        KingdeeResponse response = WebUtils.sendRequest(request, KingdeeResponse.class);
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
        TreeMap<String, String> params = new TreeMap<>();
        params.put("id", id);
        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    /**
     * 安全设置单位id，如果它存在。
     * @param unitName 单位名称
     * @param setter 回调函数
     */
    private void setUnitId(String unitName, Consumer<KingdeeUnit> setter) {
        getMeasureUnitByNumber(unitName, (kingdeeUnit, e) -> {
            if (e == null) {
                setter.accept(kingdeeUnit);
            }
        });
    }
    public void getMeasureUnitByNumber(String number, BiConsumer<KingdeeUnit, Exception> callback) {
        try {
            String endUrl = "/jdy/v2/bd/measure_unit_detail";
            TreeMap<String, String> params = new TreeMap<>();
            params.put("number", number);
            KingdeeUnit kingdeeUnit = getResponse(endUrl, params).getData(KingdeeUnit.class);
            callback.accept(kingdeeUnit, null);
        } catch (Exception e) {
            log.debug("getMeasureUnitByNumber error,当前产品的单位也许不存在。", e);
            callback.accept(null, e);
        }
    }

    public KingdeeResponse getMaterial(String number) {
        String endUrl = "/jdy/v2/bd/material_detail";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", number);
        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    public KingdeeResponse addProduct(KingdeeProductSaveReqVO product) {
        KingdeeProductSaveReqVO reqVO = new KingdeeProductSaveReqVO();
        BeanUtils.copyProperties(product, reqVO);
        try {
            String id = getMaterial(reqVO.getNumber()).getData(JSONObject.class).getString("id");
            reqVO.setId(id);
        } catch (Exception e) {
            log.debug("id not found for ({}) adding new", reqVO.getNumber());
        }
        setUnitId("立方厘米", kingdeeUnit -> reqVO.setVolumeUnitId(kingdeeUnit.getId()));
        setUnitId("kg", kingdeeUnit -> reqVO.setWeightUnitId(kingdeeUnit.getId()));
        setUnitId("套", kingdeeUnit -> reqVO.setBaseUnitId(kingdeeUnit.getId()));
        try {
            Optional.ofNullable(getAuxInfoByNumber(reqVO.getSaleDepartmentId().toString()))
                .ifPresent(kingdeeUnit ->
                    setCustomFieldSafely(reqVO, "部门", kingdeeUnit.getId())
                );
        } catch (Exception e) {
            log.debug("getAuxInfoByNumber error for sale department ID: {}", reqVO.getSaleDepartmentId(), e);
        }
        setCustomFieldSafely(reqVO, "部门", reqVO.getDeclaredTypeZh());
        setCustomFieldSafely(reqVO, "报关品名", reqVO.getDeclaredTypeZh());
        setCustomFieldSafely(reqVO, "报关品名(英文)", reqVO.getDeclaredTypeEn());
        reqVO.setIgnoreWarn(true);//保存覆盖已存在产品
        log.debug("adding product");
        String endUrl = "/jdy/v2/bd/material";
        TreeMap<String, String> params = new TreeMap<>();
        return postResponse(endUrl, params, reqVO);
    }



    /**
     * 根绝字段名称获取id，如果有该字段、则设置value，没有就日志记录
     *
     * @param reqVO       对象
     * @param displayName 属性名称
     * @param fieldValue  属性值
     */
    private void setCustomFieldSafely(KingdeeProductSaveReqVO reqVO, String displayName, String fieldValue) {
        try {
            KingdeeCustomField customField = getCustomFieldByDisplayName("bd_material", displayName);
            if (customField != null) {
                reqVO.setCustomField(customField, fieldValue);
            }
        } catch (Exception e) {
            log.debug("custom field " + displayName + " skipped for " + token.getAccountName(), e);
        }
    }

    public KingdeeResponse addSupplier(KingdeeSupplier kingdeeSupplier) {
        KingdeeSupplier supplierCopy = new KingdeeSupplier();
        BeanUtils.copyProperties(kingdeeSupplier, supplierCopy);
        try {
            String id = getSupplier(supplierCopy.getNumber()).getData(JSONObject.class).getString("id");
            supplierCopy.setId(id);
        } catch (Exception e) {
            log.debug("id not found for " + supplierCopy.getNumber() + "adding new");
        }
        String endUrl = "/jdy/v2/bd/supplier";
        TreeMap<String, String> params = new TreeMap<>();
        KingdeeResponse response = postResponse(endUrl, params, supplierCopy);
        return response;
    }

    public KingdeeResponse getSupplier(String number) {
        String endUrl = "/jdy/v2/bd/supplier";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", number);
        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    public void addDepartment(KingdeeAuxInfoDetail department) {
        //拷贝数据，避免并发下产生的线程安全问题
        KingdeeAuxInfoDetail departmentCopy = new KingdeeAuxInfoDetail();
        BeanUtils.copyProperties(department, departmentCopy);
        //id不为空则为修改，反之新增
        String groupId = getAuxInfoTypeByNumber("BM").getId();
        //根据number查找金蝶辅助资料中是否存在，number对应erp中的deptId
        KingdeeAuxInfo auxInfoByNumber = getAuxInfoByNumber(departmentCopy.getNumber());
        if (ObjUtil.isNotEmpty(auxInfoByNumber)) {
            departmentCopy.setId(auxInfoByNumber.getId());
        } else {
            //不存在则根据名称去查找是否存在
            KingdeeAuxInfo auxInfoByName = getAuxInfoByName(departmentCopy.getName());
            if (ObjUtil.isNotEmpty(auxInfoByName)) {
                departmentCopy.setId(auxInfoByName.getId());
            }
        }
        departmentCopy.setGroupId(groupId);

        KingdeeResponse response = postResponse("/jdy/v2/bd/aux_info", new TreeMap<>(), departmentCopy);
    }

    public Stream<KingdeeResponse> list(String endpoint) {
        log.debug("kingdee listing");
        return Stream.iterate(1, n -> n + 1)
            .map(n -> {
                String endUrl = endpoint;
                TreeMap<String, String> params = new TreeMap<>();
                params.put("page_size", "100"); //max 100
                params.put("page", String.valueOf(n));
                return getResponse(endUrl, params);
            })
            .takeWhile(n -> n.getData(KingdeePage.class).getPage() <= n.getData(KingdeePage.class).getTotalPage());
    }

    public KingdeeResponse post(String endpoint, JSONObject payload) {
        log.debug("kingdee posting");
        String endUrl = endpoint;
        TreeMap<String, String> params = new TreeMap<>();
        KingdeeResponse response = postResponse(endUrl, params, payload);
        return response;
    }

    public KingdeeAuxInfo getAuxInfoByNumber(String number) {
        log.debug("fetching aux info");
        String endUrl = "/jdy/v2/bd/aux_info";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", number);
        KingdeeResponse response = getResponse(endUrl, params);
        Optional<KingdeeAuxInfo> first = response.getData(KingdeePage.class).getRowsList(KingdeeAuxInfo.class).stream()
            .filter(n -> n.getNumber().equals(number))
            .findFirst();
        return first.orElse(null);
    }

    public KingdeeAuxInfo getAuxInfoByName(String name) {
        log.debug("fetching aux info");
        String endUrl = "/jdy/v2/bd/aux_info";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("name", name);
        KingdeeResponse response = getResponse(endUrl, params);
        Optional<KingdeeAuxInfo> first = response.getData(KingdeePage.class).getRowsList(KingdeeAuxInfo.class).stream()
            .filter(n -> n.getName().equals(name))
            .findFirst();
        return first.orElse(null);
    }

    public KingdeeAuxInfoType getAuxInfoTypeByNumber(String number) {
        log.debug("fetching aux info");
        String endUrl = "/jdy/v2/bd/aux_info_type";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", number);
        KingdeeResponse response = getResponse(endUrl, params);
        return response.getData(KingdeePage.class).getRowsList(KingdeeAuxInfoType.class).stream()
            .filter(n -> n.getNumber().equals(number))
            .findFirst().get();
    }

    public Stream<KingdeeCustomField> getCustomField(String entity_number) {
        log.debug("fetching custom field");
        String endUrl = "/jdy/v2/sys/custom_field";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("entity_number", entity_number);
        KingdeeResponse response = getResponse(endUrl, params);
        var data = response.getData(KingdeeCustomFieldRespVO.class);
        return data.getHead().stream();
    }

    public KingdeeCustomField getCustomFieldByDisplayName(String entity_number, String displayName) {
        return getCustomField(entity_number)
            .filter(n -> n.getDisplayName().equals(displayName))
            .findFirst().get();
    }

    public List<KingdeePurRequest> getPurRequest(KingdeePurRequestReqVO vo) {
        log.debug("fetching purchase request");
        String endUrl = "/jdy/v2/scm/pur_request";
        KingdeeResponse response = getResponse(endUrl, vo);
        return response.getData(KingdeePage.class).getRowsList(KingdeePurRequest.class).stream().toList();
    }

    public List<KingdeePurOrder> getPurOrder(KingdeePurOrderReqVO vo) {
        log.debug("fetching purchase order");
        String endUrl = "/jdy/v2/scm/pur_order";
        KingdeeResponse response = getResponse(endUrl, vo);
        return response.getData(KingdeePage.class).getRowsList(KingdeePurOrder.class).stream().toList();
    }

    /**
     * 获取采购单入库列表
     * @param vo 请求参数
     * @return List<KingdeePurInbound> 金蝶采购单入库列表
     * 2025.03.07 gumaomao
     *
     * */
    public List<KingdeePurInbound> getPurInbound(KingdeePurInboundReqVO vo) {
        log.debug("fetching purchase inbound");
        String endUrl = "/jdy/v2/scm/pur_inbound";
        KingdeeResponse response = getResponse(endUrl, vo);
        return response.getData(KingdeePage.class).getRowsList(KingdeePurInbound.class).stream().toList();
    }
    public KingdeePurOrderDetail getPurOrderDetail(String purOrderNumber) {
        String endUrl = "/jdy/v2/scm/pur_order_detail";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", purOrderNumber);
        KingdeeResponse response = getResponse(endUrl, params);
        return response.getData(KingdeePurOrderDetail.class);
    }

    public KingdeePurRequestDetail getPurRequestDetail(String purOrderNumber) {
        String endUrl = "/jdy/v2/scm/pur_request_detail";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", purOrderNumber);
        KingdeeResponse response = getResponse(endUrl, params);
        return response.getData(KingdeePurRequestDetail.class);
    }


    private KingdeeResponse fetchResponse(String requestMethod, String endUrl, TreeMap<String, String> params, Object body) {
        String cts = String.valueOf(System.currentTimeMillis());
        String signature = getApiSignature(requestMethod, endUrl, params, cts);
        Map<String, String> headers = getApiHeaders(cts, signature, token.getAppToken());
        KingdeeResponse response;
        if ("POST".equals(requestMethod)) {
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(BASE_HOST + endUrl)
                .queryParams(params)
                .headers(headers)
                .payload(body)
                .build();
            response = WebUtils.sendRequest(request, KingdeeResponse.class);
        } else {
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .url(BASE_HOST + endUrl)
                .queryParams(params)
                .headers(headers)
                .build();
            response = WebUtils.sendRequest(request, KingdeeResponse.class);
        }

        validateResponse(response);

        return response;
    }

    private void validateResponse(KingdeeResponse response) {
        if (!response.getErrcode().equals("0")) {
            throw new RuntimeException("Kingdee error response: " + response);
        }
    }

    public KingdeeResponse getResponse(String endUrl, TreeMap<String, String> params) {
        return fetchResponse("GET", endUrl, params, null);

    }

    public KingdeeResponse postResponse(String endUrl, TreeMap<String, String> params, Object payload) {
        return fetchResponse("POST", endUrl, params, payload);
    }

    public KingdeeResponse getResponse(String endUrl, Object params) {
        return fetchResponse("GET", endUrl, new TreeMap<>(JsonUtilsX.toStringMap(params)), null);
    }

    public KingdeeResponse postResponse(String endUrl, Object params, Object payload) {
        return fetchResponse("POST", endUrl, new TreeMap<>(JsonUtilsX.toStringMap(params)), payload);
    }

}
