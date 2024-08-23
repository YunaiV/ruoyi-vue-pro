package com.somle.eccang.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.somle.eccang.model.*;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.somle.eccang.model.EccangResponse.BizContent;
import com.somle.eccang.repository.EccangTokenRepository;
import com.somle.framework.common.util.general.Limiter;
import com.somle.framework.common.util.web.WebUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EccangService {


    private EccangToken token;
    private int pageSize = 100;
    private Limiter limiter = new Limiter(20);

    @Autowired
    EccangTokenRepository tokenRepo;


    @Autowired
    MessageChannel dataChannel;

    @Autowired
    MessageChannel saleChannel;






    @PostConstruct
    public void init() {
        token = tokenRepo.findAll().get(0);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public String concatenateParams(JSONObject postData) {
        String postDataStr = postData.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue().asText())
            .reduce((e1, e2) -> e1 + "&" + e2)
            .orElse("") + token.getUserToken();
        return postDataStr;
    }

    protected JSONObject requestBody(Object reqParams, String ecMethod) {
        long timestamp = System.currentTimeMillis();

        var postData = JsonUtils.newObject();
        postData.put("app_key", token.getUserName());
//        postData.put("biz_content", JSON.toJSONString(reqParams.isEmpty() ? Map.of("page_size", pageSize) : reqParams));
        postData.put("biz_content", JsonUtils.toJsonString(reqParams));
        postData.put("charset", "UTF-8");
        postData.put("interface_method", ecMethod);
        postData.put("nonce_str", "113456");
        postData.put("service_id", "E7HPYV");
        postData.put("sign_type", "MD5");
        postData.put("timestamp", timestamp);
        postData.put("version", "V1.0.0");

        postData.put("sign", md5(concatenateParams(postData)));

        return postData;
    }

    // core network io
    @Retryable(value = RuntimeException.class)
    private EccangResponse getResponse(Object payload, String endpoint){
        String url = "http://openapi-web.eccang.com/openApi/api/unity";

        EccangResponse responseFinal = WebUtils.retryTemplate.execute(ctx -> {
            JSONObject requestBody = requestBody(payload, endpoint);
            EccangResponse eccangResponse = limiter.executeWithLimiter(()->{
                var response = WebUtils.postRequest(url, Map.of(), Map.of(), requestBody);
                switch (response.code()) {
                    case 200:
                        log.info(response.toString());
                        return WebUtils.parseResponse(response, EccangResponse.class);
                    default:
                        throw new RuntimeException("Error in network " + response.toString());
                }
            });
            return eccangResponse;
        });

        return responseFinal;


    }

    private BizContent getBiz(Object payload, String endpoint) {
        EccangResponse response = getResponse(payload, endpoint);
        switch (response.getCode()) {
            case "200":
                return response.getBizContent();
            case "common.error.code.9999":
                throw new RuntimeException("Eccang return invalid response: " + response.getErrors().toString());
            case "300":
                throw new RuntimeException("Error message from eccang: " + response.getErrors().toString());
            default:
                throw new RuntimeException("Error message from eccang: " + response.getErrors().toString());
        }
    }



    private Stream<BizContent> getAllBiz(JSONObject payload, String endpoint) {
        payload.put("page", 1);
        payload.put("page_size", pageSize);
        return Stream.iterate(
            getBiz(payload, endpoint),
            bizContent -> {
                if (bizContent.hasNext()) {
                    log.debug("have next");
                    payload.put("page", bizContent.getPage() + 1);
                    return getBiz(payload, endpoint);
                } else {
                    log.debug("no next page");
                    return null;
                }
            }
        ).takeWhile(n->n!=null);
        // );
    }
    


    // public Stream<BizContent> listPage (String endpoint) {

    //     var payload = JsonUtils.newObject();
    //     return getAllBiz(payload, endpoint);
    // }

    public BizContent list (String endpoint) {

        var payload = JsonUtils.newObject();
        return getBiz(payload, endpoint);
    }

    public <T> Stream<T> list (String endpoint, Class<T> objectClass) {

        var payload = JsonUtils.newObject();
        // log.debug(payload.toString());
        // getAllBiz(payload, endpoint);
        // return Stream.of();
        return getAllBiz(payload, endpoint).flatMap(n->n.getData(objectClass).stream());
    }



    public BizContent post(String endpoint, Object payload) {
        return getBiz(payload, endpoint);
    }

    public <T> List<T> post(String endpoint, Object payload, Class<T> objectClass) {
        return getBiz(payload, endpoint).getData(objectClass);
    }

    public List<String> getPlatforms() {
        return List.of(
            "amazon", "autonomous", "bestbuy", "cdiscount", "coupong", "dsv", "ebay",
            "eono", "esm", "home24", "homedepot", "manomano", "mediamarkt", "newegg",
            "onbuy", "otto", "overstock", "rakuten", "shopify", "shopline", "staples",
            "walmart", "b2c", "yahoo", "wayfairnew", "wayfair", "allegro"
        );
    }

    public List<EccangUserAccount> getUserAccounts(String platform) {
        return post("getUserAccountList", Map.of("platform", platform)).getData(EccangUserAccount.class);
    }


    public Stream<EccangUserAccount> getUserAccounts() {
        return getPlatforms().stream().flatMap(platform->getUserAccounts(platform).stream());
    }

    public List<EccangWarehouse> getWarehouseList () {

        JSONObject params = JsonUtils.newObject();
        return getBiz(params, "getWarehouseList").getData(EccangWarehouse.class);
    }

    @Scheduled(cron = "0 0 * * * *") // Executes every hour
    // @Scheduled(fixedDelay = 999999999, initialDelay = 1000)
    public void uploadOrderShip() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime starTime = endTime.minusHours(3);
        getOrderShip(starTime, endTime)
            .forEach(order->{
                saleChannel.send(MessageBuilder.withPayload(order).build());
            });
    }

    public Stream<EccangOrder> getOrderShip(LocalDateTime startTime, LocalDateTime endTime) {
        return getOrderShipPage(startTime, endTime)
            .map(n->n.getData(EccangOrder.class))
            .flatMap(n->n.stream());
    }

    public Stream<BizContent> getOrderShipPage(LocalDateTime startTime, LocalDateTime endTime) {
        var query = EccangOrderVO.builder()
            .platformShipDateStart(startTime)
            .platformShipDateEnd(endTime)
            .build();
        return getOrderPages(query);
    }

    public Stream<BizContent> getOrderUnShipPage() {
        var query = EccangOrderVO.builder()
            .status("3")
            .build();
        return getOrderPages(query);
    }


    public Stream<BizContent> getOrderPages(EccangOrderVO order) {
        JSONObject params = JsonUtils.newObject();
        params.put("get_detail", "1");
        params.put("get_address", "1");

        params.put("condition", order);
        return getAllBiz(params, "getOrderList");
    }

    public EccangProduct getProduct(String sku) {
        EccangProduct product = EccangProduct.builder()
            .productSku(sku)
            .build();
        // String response = post("getWmsProductList", product, String.class).get(0);
        // log.debug(response);
        // return JsonUtils.parseObject(response, EccangProduct.class);
        return post("getWmsProductList", product, EccangProduct.class).get(0);
    }




    public Stream<BizContent> getInventoryBatchLog(LocalDateTime startTime, LocalDateTime endTime) {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final String START_TIME_ALIAS = "date_from";
        final String END_TIME_ALIAS = "date_to";
        var payload = JsonUtils.newObject();
        payload.put(START_TIME_ALIAS, startTime.format(dateTimeFormatter));
        payload.put(END_TIME_ALIAS, endTime.format(dateTimeFormatter));
        payload.put("warehouse_code", getWarehouseList().stream().map(warehouse -> warehouse.getWarehouseCode()).toList());
        return getAllBiz(payload, "getInventoryBatchLog");
    }





    public BizContent addDepartment(EccangCategory department) {
        log.info("adding department: " + department.toString());
        var result = post("editCategory", department);
        log.debug(result.toString());
        return result;
    }

    public BizContent addProduct(EccangProduct product) {
        return post("syncProduct", product);
    }

    public Stream<EccangCategory> getCategories() {
        return list("categotyList", EccangCategory.class);
    }

    public EccangCategory getCategoryByNameEn(String nameEn) {
        return getCategories().filter(n->n.getPcNameEn().equals(nameEn)).findFirst().get();
    }

    public Stream<EccangOrganization> getOrganizations() {
        return list("getUserOrganizationAll", EccangOrganization.class);
    }

    public EccangOrganization getOrganizationByNameEn(String nameEn) {
        log.debug("searching organization with name_en " + nameEn);
        return getOrganizations().filter(n->n.getNameEn().equals(nameEn)).findFirst().get();
    }

    public Stream<EccangProduct> getProducts() {
        return list("getWmsProductList", EccangProduct.class);
    }



    public String parseCountryCode(String code) {
        switch (code) {
            case "USA":
                code = "US";
                break;
            case "IND":
                code = "IN";
                break;
            case "EU":
                code = "UK";
                break;
            case "CHN":
                code = "CN";
                break;
            case "AVS":
            case "A1":
            case "A2":
            case "A3":
                code = "";
                break;
            default:
                break;
        }
        return code;
    }


}
