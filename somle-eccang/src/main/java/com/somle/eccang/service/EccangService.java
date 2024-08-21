package com.somle.eccang.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangOrder;
import com.somle.eccang.model.EccangOrganization;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.model.EccangResponse.BizContent;
import com.somle.eccang.model.EccangToken;
import com.somle.eccang.model.EccangUserAccount;
import com.somle.eccang.model.EccangWarehouse;
import com.somle.eccang.repository.EccangTokenRepository;
import com.somle.util.Limiter;
import com.somle.util.Util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EccangService {


    private String username;
    private String usertoken;
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
        EccangToken defaultToken = tokenRepo.findAll().get(0);
        this.username = defaultToken.getUserName();
        this.usertoken = defaultToken.getUserToken();
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

    private JSONObject concateParams(JSONObject reqParams, String ecMethod) {
        long timestamp = System.currentTimeMillis();

        JSONObject postData = new JSONObject();
        postData.put("app_key", username);
        postData.put("biz_content", JSON.toJSONString(reqParams.isEmpty() ? Map.of("page_size", pageSize) : reqParams));
        postData.put("charset", "UTF-8");
        postData.put("interface_method", ecMethod);
        postData.put("nonce_str", "113456");
        postData.put("service_id", "E7HPYV");
        postData.put("sign_type", "MD5");
        postData.put("timestamp", timestamp);
        postData.put("version", "v1.0.0");

        String postDataStr = postData.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((e1, e2) -> e1 + "&" + e2)
                .orElse("") + usertoken;

        // log.info(postDataStr);

        postData.put("sign", md5(postDataStr));

        return postData;
    }

    // core network io
    @Retryable(value = RuntimeException.class)
    private EccangResponse getResponse(JSONObject payload, String endpoint){
        String url = "http://openapi-web.eccang.com/openApi/api/unity";

        EccangResponse responseFinal = Util.retryTemplate.execute(ctx -> {
            JSONObject concatedParams = concateParams(payload, endpoint);
            EccangResponse response = limiter.executeWithLimiter(()->{
                return Util.postRequest(url, Map.of(), Map.of(), concatedParams, EccangResponse.class);
            });
            String code = response.getCode();
            // if (true) {
            if (code.equals("common.error.code.9999")) {
                throw new RuntimeException("Eccang return invalid response: " + response.toString());
            }
            return response;
        });

        return responseFinal;


    }

    private BizContent getBiz(JSONObject payload, String endpoint) {
        EccangResponse response = getResponse(payload, endpoint);
        return response.getBizContent();
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

    //     JSONObject payload = new JSONObject();
    //     return getAllBiz(payload, endpoint);
    // }

    public BizContent list (String endpoint) {

        JSONObject payload = new JSONObject();
        return getBiz(payload, endpoint);
    }

    public <T> Stream<T> list (String endpoint, Class<T> objectClass) {

        JSONObject payload = new JSONObject();
        // log.debug(payload.toString());
        // getAllBiz(payload, endpoint);
        // return Stream.of();
        return getAllBiz(payload, endpoint).flatMap(n->n.getData(objectClass).stream());
    }



    public BizContent post(String endpoint, Object payload) {
        return getBiz((JSONObject) JSON.toJSON(payload), endpoint);
    }

    public <T> List<T> post(String endpoint, Object payload, Class<T> objectClass) {
        return getBiz((JSONObject) JSON.toJSON(payload), endpoint).getData(objectClass);
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

        JSONObject params = new JSONObject();
        return getBiz(params, "getWarehouseList").getData(EccangWarehouse.class);
    }


    public Stream<BizContent> getOrderShipPage(LocalDateTime startTime, LocalDateTime endTime) {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final String START_TIME_ALIAS = "platform_ship_date_start";
        final String END_TIME_ALIAS = "platform_ship_date_end";

        JSONObject options = new JSONObject();
        options.put("get_detail", "1");
        options.put("get_address", "1");

        JSONObject params = new JSONObject(options);
        JSONObject condition = new JSONObject();
        condition.put(START_TIME_ALIAS, startTime.format(dateTimeFormatter));
        condition.put(END_TIME_ALIAS, endTime.format(dateTimeFormatter));
        params.put("condition", condition);
        return getAllBiz(params, "getOrderList");
    }

    public Stream<EccangOrder> getOrderShip(LocalDateTime startTime, LocalDateTime endTime) {
        return getOrderShipPage(startTime, endTime)
            .map(n->n.getData(EccangOrder.class))
            .flatMap(n->n.stream());
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

    // @Scheduled(fixedDelay = 999999999, initialDelay = 1000)
    // public void getOrderShip() {
    //     LocalDateTime endTime = LocalDate.of(2024, 7, 13).atStartOfDay();
    //     LocalDateTime starTime = LocalDate.of(2024, 7, 11).atStartOfDay();
    //     getOrderShip(starTime, endTime)
    //         .forEach(order->{
    //             saleChannel.send(MessageBuilder.withPayload(toEsb(order)).build());
    //         });
    // }

    public EccangProduct getProduct(String sku) {
        EccangProduct product = EccangProduct.builder()
            .productSku(sku)
            .build();
        // String response = post("getWmsProductList", product, String.class).get(0);
        // log.debug(response);
        // return JSON.parseObject(response, EccangProduct.class);
        return post("getWmsProductList", product, EccangProduct.class).get(0);
    }




    public Stream<BizContent> getInventoryBatchLog(LocalDateTime startTime, LocalDateTime endTime) {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final String START_TIME_ALIAS = "date_from";
        final String END_TIME_ALIAS = "date_to";
        JSONObject payload = new JSONObject();
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
