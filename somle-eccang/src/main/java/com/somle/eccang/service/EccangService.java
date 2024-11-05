package com.somle.eccang.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import cn.hutool.core.collection.BoundedPriorityQueue;
import cn.hutool.core.collection.CollUtil;
import com.somle.eccang.model.*;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.json.JSONObject;

import lombok.SneakyThrows;
import com.somle.framework.common.util.object.ObjectUtils;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.somle.eccang.model.EccangResponse.EccangPage;
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
    @SneakyThrows
    @Retryable(value = RuntimeException.class)
    private EccangResponse getResponse(Object payload, String endpoint) {
        String url = "http://openapi-web.eccang.com/openApi/api/unity";
        EccangResponse responseFinal = CoreUtils.retry(ctx -> {
            // 限流器限制在生成签名前，防止签名过期
            JSONObject requestBody = limiter.executeWithLimiter(()->requestBody(payload, endpoint));
            Response response = WebUtils.postRequest(url, Map.of(), Map.of(), requestBody);
            switch (response.code()) {
                case 200:
                    var responseBody = response.body().string();
                    log.info(responseBody);
                    return JsonUtils.parseObject(responseBody, EccangResponse.class);
                default:
                    throw new RuntimeException("Unknown response code " + response);
            }
        });
        return responseFinal;
    }

    private void validateResponse(EccangResponse response ) {
        //判断resp的code是否为null，为null则返回message直接作为异常信息
        String code = response.getCode();
        if (code == null){
            throw new RuntimeException(response.getMessage());
        }
        switch (code) {
            case "200":
                return;
            case "saas.api.error.code.0049":
                throw new RuntimeException("签名过期：时间戳必须在一分钟以内，超出1分钟则过期失效，且只能用一次。 时间戳重新生成后，需要重新生成签名");
            case "saas.api.error.code.0082":
                throw new RuntimeException("Eccang error, full response: " + response);
            case "common.error.code.9999":
                throw new RuntimeException("Eccang return invalid response: " + response.getBizContent(EccangResponse.EccangError.class));
            case "300":
                throw new RuntimeException("Error message from eccang: " + response.getBizContentList(EccangResponse.EccangError.class));
            case "429":
                throw new RuntimeException("Too many requests");
            default:
                throw new RuntimeException("Unknown eccang-specific response code: " + response.getCode() + " " + "message: " + response.getMessage());
        }
    }

    private EccangPage getPage(Object payload, String endpoint) {
        EccangResponse response = getResponse(payload, endpoint);
        validateResponse(response);
        return response.getBizContent(EccangPage.class);
    }



    private Stream<EccangPage> getAllPage(JSONObject payload, String endpoint) {
        payload.put("page", 1);
        payload.put("page_size", pageSize);
        return Stream.iterate(
            getPage(payload, endpoint),
            bizContent -> {
                if (bizContent.hasNext()) {
                    log.debug("have next");
                    payload.put("page", bizContent.getPage() + 1);
                    return getPage(payload, endpoint);
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

    public EccangPage list (String endpoint) {

        var payload = JsonUtils.newObject();
        return getPage(payload, endpoint);
    }

    public <T> Stream<T> list (String endpoint, Class<T> objectClass) {

        var payload = JsonUtils.newObject();
        // log.debug(payload.toString());
        // getAllBiz(payload, endpoint);
        // return Stream.of();
        return getAllPage(payload, endpoint).flatMap(n->n.getData(objectClass).stream());
    }



    public EccangPage post(String endpoint, Object payload) {
        return getPage(payload, endpoint);
    }


    public <T> List<T> post(String endpoint, Object payload, Class<T> objectClass) {
        return getPage(payload, endpoint).getData(objectClass);
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
        return getPage(params, "getWarehouseList").getData(EccangWarehouse.class);
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
        var vo = EccangOrderVO.builder()
                .platformShipDateStart(startTime)
                .platformShipDateEnd(endTime)
                .build();
        return getOrder(vo);
    }

    public Stream<EccangOrder> getOrder(EccangOrderVO vo) {
        return getOrderPages(vo)
                .map(n->n.getData(EccangOrder.class))
                .flatMap(n->n.stream());
    }

    public Stream<EccangPage> getOrderPlusArchivePages(EccangOrderVO orderParams, String year) {
        return Stream.concat(getOrderPages(orderParams), getOrderArchivePages(orderParams, year));
    }


    public Stream<EccangPage> getOrderArchivePages(EccangOrderVO orderParams, String year) {
        JSONObject params = JsonUtils.newObject();
        params.put("get_detail", "1");
        params.put("get_address", "1");
        params.put("year", year);

        params.put("condition", orderParams);
        return getAllPage(params, "getOrderList");
    }


    public Stream<EccangPage> getOrderPages(EccangOrderVO orderParams) {
        JSONObject params = JsonUtils.newObject();
        params.put("get_detail", "1");
        params.put("get_address", "1");

        params.put("condition", orderParams);
        return getAllPage(params, "getOrderList");
    }

    public EccangProduct getProduct(String sku) {
        //需要返回箱规信息
        EccangProduct product = EccangProduct.builder()
            .productSku(sku).getProductBox(1)
            .build();
        // String response = post("getWmsProductList", product, String.class).get(0);
        // log.debug(response);
        // return JsonUtils.parseObject(response, EccangProduct.class);
        List<EccangProduct> getWmsProductList = post("getWmsProductList", product, EccangProduct.class);
        if (CollUtil.isNotEmpty(getWmsProductList)){
            return getWmsProductList.get(0);
        }
        return null;
    }

    public Stream<EccangPage> getInventory() {
        var payload = JsonUtils.newObject();
        return getAllPage(payload, "getProductInventory");
    }


    public Stream<EccangPage> getInventoryBatchLog(LocalDateTime startTime, LocalDateTime endTime) {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final String START_TIME_ALIAS = "date_from";
        final String END_TIME_ALIAS = "date_to";
        var payload = JsonUtils.newObject();
        payload.put(START_TIME_ALIAS, startTime.format(dateTimeFormatter));
        payload.put(END_TIME_ALIAS, endTime.format(dateTimeFormatter));
        List<EccangWarehouse> warehouseList = getWarehouseList();
        EccangWarehouse first = warehouseList.get(0);
        log.info(first.toString());
        String code = first.getWarehouseCode();
        log.info(code);
        var codeList = warehouseList.stream().map(EccangWarehouse::getWarehouseCode).toList();
        payload.put("warehouse_code", warehouseList);
        return getAllPage(payload, "getInventoryBatchLog");
    }





    public EccangPage addDepartment(EccangCategory department) {
        log.info("adding department: " + department.toString());
        var result = post("editCategory", department);
        log.debug(result.toString());
        return result;
    }

    public EccangPage addProduct(EccangProduct product) {
        return post("syncProduct", product);
    }

    public EccangPage addProductBoxes(EccangProductBoxes boxes) {
        return post("syncProductBoxes", boxes);
    }

    public void addBatchProduct(List<EccangProduct> products) {
        EccangResponse syncBatchProduct = getResponse(products, "syncBatchProduct");
        String code = syncBatchProduct.getCode();
        if (!Objects.equals(code,"200")){
            throw new RuntimeException("批量添加商品失败,原因："+ syncBatchProduct.getBizContentString());
        }
    }

    public Stream<EccangCategory> getCategories() {
        return list("categotyList", EccangCategory.class);
    }

    public EccangCategory getCategoryByName(String name) {
        return getCategories().filter(n->n.getPcName().equals(name)).findFirst().get();
    }

    public EccangCategory getCategoryByNameEn(String nameEn) {
        return getCategories().filter(n->n.getPcNameEn().equals(nameEn)).findFirst().get();
    }

    public Stream<EccangOrganization> getOrganizations() {
        return list("getUserOrganizationAll", EccangOrganization.class);
    }

    public EccangOrganization getOrganizationByNameEn(String nameEn) {
        log.debug("searching organization with name_en " + nameEn);
        Optional<EccangOrganization> first = getOrganizations().filter(n -> n.getName().equals(nameEn)).findFirst();
        if (first.isPresent()){
            return first.get();
        }
        throw new RuntimeException("您传入的部门信息不存在于eccang信息库中");
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
