package com.somle.kingdee.service;

// import com.smecloud.apigw.ApigwConfig;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.model.KingdeeAuxInfoTypeDetail;
import com.somle.kingdee.model.KingdeeCustomField;
import com.somle.kingdee.model.KingdeeProduct;
import com.somle.kingdee.model.KingdeeResponse;
import com.somle.kingdee.model.KingdeeToken;
import com.somle.kingdee.model.KingdeeUnit;
import com.somle.framework.common.util.web.WebUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// import org.apache.commons.codec.binary.Base16;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// https://open.jdy.com/#/files/api/detail?index=2&categrayId=3cc8ee9a663e11eda5c84b5d383a2b93&id=adfe4a24712711eda0b307c6992ee459
@Slf4j
@Data
public class KingdeeClient {

    // private final String clientId = "280604";
    // private final String clientSecret = "4fef7aeaefcfbc1cc454af95b6e1683d";
    private static final String clientId = "240474";
    private static final String clientSecret = "b5639a677545e611a297d6537f2b444c";
    // private final String clientId = "200421";
    // private final String clientSecret = "f2adcfef73369bfc4e1384677d38a0ff";
    private static final String baseHost = "https://api.kingdee.com";
    private KingdeeToken token;



    // private List<KingdeeAuxInfoDetail> auxInfos;

    // private List<KingdeeAuxInfoTypeDetail> auxInfoTypes;

    public KingdeeClient(KingdeeToken token) {
        this.token = token;

        // product.auxInfos = list("/jdy/v2/bd/aux_info")
        //     .map(page -> page.getData().getJSONArray("rows"))
        //     .flatMap(array -> array.stream())
        //     .map(info->JsonUtils.parseObject(info.toString(), KingdeeAuxInfoDetail.class))
        //     .toList();


        // product.auxInfoTypes = list("/jdy/v2/bd/aux_info_type")
        //     .map(page -> page.getData().getJSONArray("rows"))
        //     .flatMap(array -> array.stream())
        //     .map(info->JsonUtils.parseObject(info.toString(), KingdeeAuxInfoTypeDetail.class))
        //     .toList();

        // log.debug(auxInfoTypes.stream().filter(type -> type.getNumber().equals("BM")).findFirst().get().getId());
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



    public static byte[] hmac256(String secret, String data) {
        try {
            // Create a new SecretKeySpec
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

            // Get an instance of Mac and initialize with the secret key
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            // Compute the HMAC SHA-256
            byte[] hmacSha256 = mac.doFinal(data.getBytes());

            return hmacSha256;
        } catch (Exception e) {
            log.error("secret " + secret);
            log.error("data " + data);
            throw new RuntimeException("Failed to generate HMACSHA256", e);
        }
    }

    public static String urlEncode(String str) {
        // log.debug(str);
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static String getApiString(String reqMtd, String urlPath, TreeMap<String, String> params, String nonce,
            String timestamp) {
        String paramsStr = params.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + urlEncode(urlEncode(entry.getValue())))
                .collect(Collectors.joining("&"));
        String urlPathStr;
        urlPathStr = URLEncoder.encode(urlPath, StandardCharsets.UTF_8);

        return String.format("%s\n%s\n%s\nx-api-nonce:%s\nx-api-timestamp:%s\n",
                reqMtd, urlPathStr, paramsStr, nonce, timestamp);
    }

    public static String getApiSignature(String reqMtd, String urlPath, TreeMap<String, String> params, String timestamp) {
        return getApiSignature(reqMtd, urlPath, params, timestamp, timestamp);
    }

    public static String getApiSignature(String reqMtd, String urlPath, TreeMap<String, String> params, String nonce,
            String timestamp) {
        String apiString = getApiString(reqMtd, urlPath, params, nonce, timestamp);
        String apiStringToHmac256EnHex = Hex.encodeHexString(hmac256(clientSecret, apiString));
        String apiSignature = Base64.encodeBase64String(apiStringToHmac256EnHex.getBytes());
        log.trace("apiString " + apiString);
        log.trace("clientSecret " + clientSecret);
        log.trace("apiStringToHmac256EnHex " + apiStringToHmac256EnHex);
        log.trace("apiSignature " + apiSignature);
        return apiSignature;
    }

    public String getAppSignature(KingdeeToken token) {
        String appSecret = token.getAppSecret();
        String appKey = token.getAppKey();
        String appKeyToHmac256EnHex = Hex.encodeHexString(hmac256(appSecret, appKey));
        String appSignature = Base64.encodeBase64String(appKeyToHmac256EnHex.getBytes());
        log.trace("appKey " + appKey);
        log.trace("appSecret " + appSecret);
        log.trace("appKeyToHmac256EnHex " + appKeyToHmac256EnHex);
        log.trace("appSignature " + appSignature);
        return appSignature;
    }



    public String getAppToken(KingdeeToken token) {
        log.info("preparing app token");
        String appKey = token.getAppKey();
        String appSignature = token.getAppSignature();
        String reqMtd = "GET";
        String ctime = String.valueOf(System.currentTimeMillis());
        // String ctime = "1719213104265";
        String endUrl = "/jdyconnector/app_management/kingdee_auth_token";
        String fullUrl = baseHost + endUrl;

        TreeMap<String, String> params = new TreeMap<>();
        params.put("app_key", appKey);
        params.put("app_signature", appSignature);

        String apiSignature = getApiSignature(reqMtd, endUrl, params, ctime);

        
        // HttpUrl.Builder urlBuilder = HttpUrl.parse(fullUrl).newBuilder();
        // urlBuilder.addQueryParameter("app_key", appKey);
        // urlBuilder.addQueryParameter("app_signature", appSignature);
        // Request request = new Request.Builder()
        //         .url(urlBuilder.build().toString())
        //         .get() // Use .get() for GET requests
        //         .addHeader("Content-Type", "application/json")
        //         .addHeader("X-Api-Auth-Version", "2.0")
        //         .addHeader("X-Api-ClientID", clientId)
        //         .addHeader("X-Api-Nonce", ctime)
        //         .addHeader("X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce")
        //         .addHeader("X-Api-Signature", apiSignature)
        //         .addHeader("X-Api-TimeStamp", ctime)
        //         .build();
        // log.info("sending okhttp request");
        
        // try {
        //     KingdeeResponse response = client.newCall(request).execute();
        //     return parseBody(response, KingdeeResponse.class).getData().getString("app-token");
        // } catch (Exception e) {
        //     throw new RuntimeException(e);
        // }


        Map<String, String> headers = Map.of(
            "Content-Type", "application/json",
            "X-Api-Auth-Version", "2.0",
            "X-Api-ClientID", clientId,
            "X-Api-Nonce", ctime,
            "X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce",
            "X-Api-Signature", apiSignature,
            "X-Api-TimeStamp", ctime
        );

        KingdeeResponse response = WebUtils.getRequest(fullUrl, params, headers, KingdeeResponse.class);
        return response.getData().getString("app-token");

    }

    public KingdeeToken pushAuth(KingdeeToken token) {
        String outerInstanceId = token.getOuterInstanceId();
        log.info("preparing for auth push");
        String reqMtd = "POST";
        String ctime = String.valueOf(System.currentTimeMillis());
        // String ctime = "1719213104265";
        String endUrl = "/jdyconnector/app_management/push_app_authorize";
        String fullUrl = baseHost + endUrl;

        TreeMap<String, String> params = new TreeMap<>();
        params.put("outerInstanceId", outerInstanceId);

        String apiSignature = getApiSignature(reqMtd, endUrl, params, ctime);

        
        // HttpUrl.Builder urlBuilder = HttpUrl.parse(fullUrl).newBuilder();
        // urlBuilder.addQueryParameter("outerInstanceId", outerInstanceId);
        // RequestBody body = RequestBody.create(JSON.toJSONString(new JSONObject()), MediaType.parse("application/json; charset=utf-8"));
        // Request request = new Request.Builder()
        //         .url(urlBuilder.build().toString())
        //         .post(body) // Use .get() for GET requests
        //         .addHeader("Content-Type", "application/json")
        //         .addHeader("X-Api-Auth-Version", "2.0")
        //         .addHeader("X-Api-ClientID", clientId)
        //         .addHeader("X-Api-Nonce", ctime)
        //         .addHeader("X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce")
        //         .addHeader("X-Api-Signature", apiSignature)
        //         .addHeader("X-Api-TimeStamp", ctime)
        //         .build();
        // log.info("sending okhttp request");
        // try {
        //     KingdeeResponse response = client.newCall(request).execute();
        //     return JsonUtils.parseArray(parseBody(response, JSONObject.class).getString("data"), KingdeeToken.class).get(0);
        // } catch (Exception e) {
        //     throw new RuntimeException(e);
        // }

        Map<String, String> headers = Map.of(
            "Content-Type", "application/json",
            "X-Api-Auth-Version", "2.0",
            "X-Api-ClientID", clientId,
            "X-Api-Nonce", ctime,
            "X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce",
            "X-Api-Signature", apiSignature,
            "X-Api-TimeStamp", ctime
        );

        KingdeeResponse response = WebUtils.postRequest(fullUrl, params, headers, null, KingdeeResponse.class);
        return response.getDataList(KingdeeToken.class).get(0);

    }



    public KingdeeResponse getSupplier() {
        log.debug("fetching suppliers");
        String endUrl = "/jdy/v2/bd/supplier";
        TreeMap<String, String> params = new TreeMap<>();

        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    // https://open.jdy.com/#/files/api/detail?index=3&categrayId=3cc8ee9a663e11eda5c84b5d383a2b93&id=9d1d8798712511eda0b3cd6fb29bfcf5
    public KingdeeResponse getVoucher(LocalDate date) {
        log.debug("fetching vouchers");
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
        log.debug("fetching voucher details");
        String endUrl = "/jdy/v2/fi/voucher_detail";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("id", id);

        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    public KingdeeUnit getMeasureUnitByNumber(String number) {
        log.debug("fetching measure unit");
        String endUrl = "/jdy/v2/bd/measure_unit_detail";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("number", number);
        return getResponse(endUrl, params).getData(KingdeeUnit.class);
    }

    public KingdeeResponse getMaterial(String number) {
        log.debug("fetching materials");
        String endUrl = "/jdy/v2/bd/material_detail";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("number", number);

        KingdeeResponse response = getResponse(endUrl, params);
        return response;
    }

    // public KingdeeUnit getMaterialUnitByNumber(String number) {
    //     log.debug("fetching material unit");
    //     String endUrl = "/jdy/v2/bd/material_unit_detail";
    //     TreeMap<String, String>  params = new TreeMap<>();
    //     params.put("number", number);
    //     log.debug("material unit requesting: " + number);
    //     return getResponse(endUrl, params, KingdeeResponse.class).getData().to(KingdeeUnit.class);
    // }




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




    public void addDepartment(KingdeeAuxInfoDetail department) {
        String groupId = getAuxInfoTypeByNumber("BM").getId();
        try {
            String id = getAuxInfoByNumber(department.getNumber()).getId();
            department.setId(id);
        } catch (Exception e) {
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
        return response.getData().getJSONArray("rows").stream()
            .map(n->JsonUtils.parseObject(n.toString(), KingdeeAuxInfoDetail.class))
            .filter(n->n.getNumber().equals(number))
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

    public KingdeeAuxInfoDetail auxInfoDetail(String number) {
        String endUrl = "/jdy/v2/bd/aux_info_detail";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("number", number);

        return getResponse(endUrl, params).getData(KingdeeAuxInfoDetail.class);
    }

    public KingdeeAuxInfoTypeDetail auxInfoTypeDetail(String number) {
        String endUrl = "/jdy/v2/bd/aux_info_type_detail";
        TreeMap<String, String>  params = new TreeMap<>();
        params.put("number", number);

        return getResponse(endUrl, params).getData(KingdeeAuxInfoTypeDetail.class);
    }
    


    private KingdeeResponse fetchResponse(String requestMethod, String endUrl, TreeMap<String, String>  params, Object body) {
        String cts = String.valueOf(System.currentTimeMillis());
        String signature = getApiSignature(requestMethod, endUrl, params, cts);

        // HttpUrl.Builder urlBuilder = HttpUrl.parse(baseHost + endUrl).newBuilder();
        // for (Map.Entry<String, String> param : params.entrySet()) {
        //     urlBuilder.addQueryParameter(param.getKey(), param.getValue());
        // }
        // String fullUrl = urlBuilder.build().toString();

        // Builder requestBuilder = new Request.Builder()
        //     .url(fullUrl)
        //     .addHeader("Content-Type", "application/json;charset=utf-8")
        //     .addHeader("X-Api-ClientID", clientId)
        //     .addHeader("X-Api-Auth-Version", "2.0")
        //     .addHeader("X-Api-TimeStamp", cts)
        //     .addHeader("X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce")
        //     .addHeader("X-Api-Nonce", cts)
        //     .addHeader("X-Api-Signature", signature)
        //     .addHeader("app-token", token.getAppToken())
        //     .addHeader("X-GW-Router-Addr", "https://tf.jdy.com");

        // Request request = null;
        // switch (requestMethod) {
        //     case "POST":
        //         request = requestBuilder.post(body).build();
        //         break;
        
        //     default:
        //         request = requestBuilder.build();
        //         break;
        // }


        // log.info("Sending request to Kingdee");

        // try {
        //     KingdeeResponse response = client.newCall(request).execute();
        //     return response;
        // } catch (Exception e) {
        //     log.error(e.toString());
        //     throw new RuntimeException(e);
        // }



        Map<String, String> headers = Map.of(
            "Content-Type", "application/json;charset=utf-8",
            "X-Api-ClientID", clientId,
            "X-Api-Auth-Version", "2.0",
            "X-Api-TimeStamp", cts,
            "X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce",
            "X-Api-Nonce", cts,
            "X-Api-Signature", signature,
            "app-token", token.getAppToken(),
            "X-GW-Router-Addr", "https://tf.jdy.com"
        );

        KingdeeResponse response = null;
        switch (requestMethod) {
            case "POST":
                response = WebUtils.postRequest(baseHost + endUrl, params, headers, body, KingdeeResponse.class);
                break;
        
            default:
                response = WebUtils.getRequest(baseHost + endUrl, params, headers, KingdeeResponse.class);
                break;
        }

        return response;
    }



    // private <T> T getResponse(String endUrl, TreeMap<String, String>  params, Class<T> objectClass) {
    //     return fetchResponse("GET", endUrl, params, null);
    // }

    // private <T> T postResponse(String endUrl, TreeMap<String, String>  params, Object payload, Class<T> objectClass) {
    //     RequestBody body = RequestBody.create(JSON.toJSONString(payload), MediaType.parse("application/json; charset=utf-8"));
    //     return fetchResponse("POST", endUrl, params, body);
    // }

    private KingdeeResponse getResponse(String endUrl, TreeMap<String, String>  params) {
        return fetchResponse("GET", endUrl, params, null);
    }

    private KingdeeResponse postResponse(String endUrl, TreeMap<String, String>  params, Object payload) {
        return fetchResponse("POST", endUrl, params, payload);
    }

}
