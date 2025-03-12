package com.somle.eccang.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.general.CoreUtils;
import cn.iocoder.yudao.framework.common.util.general.Limiter;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.eccang.model.*;
import com.somle.eccang.model.EccangResponse.EccangPage;
import com.somle.eccang.model.exception.EccangResponseException;
import com.somle.eccang.model.req.EccangInventoryBatchReqVO;
import com.somle.eccang.model.req.EccangRmaReturnReqVO;
import com.somle.eccang.repository.EccangTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class EccangService {


    private EccangToken token;
    private final int pageSize = 100;
    private Limiter limiter = new Limiter(20);

    @Autowired
    EccangTokenRepository tokenRepo;

    @Autowired
    MessageChannel eccangSaleOutputChannel;


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

        var postData = JsonUtilsX.newObject();
        postData.put("app_key", token.getUserName());
//        postData.put("biz_content", JSON.toJSONString(reqParams.isEmpty() ? Map.of("page_size", pageSize) : reqParams));
        postData.put("biz_content", JsonUtilsX.toJsonString(reqParams));
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
    private EccangResponse getResponse(Object payload, String endpoint) {

        String url = "http://openapi-web.eccang.com/openApi/api/unity";


        EccangResponse responseFinal = CoreUtils.retry(ctx -> {
            var requestBody = requestBody(payload, endpoint);
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(url)
                .payload(requestBody)
                .build();
            // 获取当前重试次数
            int retryCount = ctx.getRetryCount();
            // 记录每次重试的日志
            if (ctx.getRetryCount() != 0) {
                log.debug("正在请求url= {},第 {} 次重试。endpoint = {}", request.getUrl(), retryCount, endpoint);
                log.debug("重试原因：{}", ctx.getLastThrowable().toString());
            }
            try (var response = WebUtils.sendRequest(request)) {
                switch (response.code()) {
                    case 200:
                        var responseBody = response.body().string();
                        var responseOriginal = JsonUtilsX.parseObject(responseBody, EccangResponse.class);
                        validateResponse(responseOriginal);
                        return responseOriginal;
                    case 429:
                        throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later.");
                    default:
                        throw new RuntimeException("Unknown response code " + response);
                }
            }
        });
        return responseFinal;
    }

    private void validateResponse(EccangResponse response) {
        //判断resp的code是否为null，为null则返回message直接作为异常信息
        String code = response.getCode();
        if (code == null) {
            throw new RuntimeException(response.getMessage());
        }
        switch (code) {
            // 请检查requestBody生成时间和实际请求发送时间是否相隔太久
            case "200":
                return;
            case "300":
                List<EccangResponse.EccangError> errors = response.getBizContentList(EccangResponse.EccangError.class);
                if (errors.isEmpty()) {
                    throw new RuntimeException("response code is 300 but errors is empty,Error message from response: " + response);
                }else {
                    throw new EccangResponseException(errors);
                }
            case "429":
                throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later.");
//            case "500", "saas.api.error.code.0082":
            case "saas.api.error.code.0049":
                throw new RuntimeException("签名过期：时间戳必须在一分钟以内，超出1分钟则过期失效，且只能用一次。 时间戳重新生成后，需要重新生成签名");
            case "saas.api.error.code.0061": //达到限流时-继续重试
                throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "(同一客户每秒请求接口次数不能超过10次)请求受限，继续重试");
            case "common.error.code.9999":
                throw new RuntimeException("Eccang return invalid response: " + response);
            default:
                throw new RuntimeException("Unknown eccang-specific full response:" + response);
        }
    }

    private EccangPage getPage(Object payload, String endpoint) {
        EccangResponse response = getResponse(payload, endpoint);
        return response.getBizContent(EccangPage.class);
    }


    private Stream<EccangPage> getAllPage(JSONObject payload, String endpoint) {
        payload.put("page", 1);
        payload.put("page_size", pageSize);
        return StreamX.iterate(
            getPage(payload, endpoint),
            EccangPage::hasNext,
            eccangPage -> {
                log.debug("have next,endpoint:{}当前进度：{}/{}", endpoint, (eccangPage.getPage() - 1) * pageSize + eccangPage.getData().size(), eccangPage.getTotal());
                payload.put("page", eccangPage.getPage() + 1);
                return getPage(payload, endpoint);
            }
        );
    }


    // public Stream<BizContent> listPage (String endpoint) {

    //     var payload = JsonUtilsSomle.newObject();
    //     return getAllBiz(payload, endpoint);
    // }

    public EccangPage list(String endpoint) {

        var payload = JsonUtilsX.newObject();
        return getPage(payload, endpoint);
    }

    public <T> Stream<T> list(String endpoint, Class<T> objectClass) {

        var payload = JsonUtilsX.newObject();
        // log.debug(payload.toString());
        // getAllBiz(payload, endpoint);
        // return Stream.of();
        return getAllPage(payload, endpoint).flatMap(n -> n.getData(objectClass).stream());
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
        return getPlatforms().stream().flatMap(platform -> getUserAccounts(platform).stream());
    }

    public List<EccangWarehouse> getWarehouseList() {

        JSONObject params = JsonUtilsX.newObject();
        return getPage(params, "getWarehouseList").getData(EccangWarehouse.class);
    }

    @Scheduled(cron = "0 0 * * * *") // Executes every hour
    // @Scheduled(fixedDelay = 999999999, initialDelay = 1000)
    public void uploadRealtimeOrder() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(3);
        var vo = EccangOrderVO.builder()
            .condition(EccangOrderVO.Condition.builder()
                .platformPaidDateStart(startTime)
                .platformPaidDateEnd(endTime)
                .build())
            .build();
        getOrderUnarchive(vo)
            .forEach(order -> {
                eccangSaleOutputChannel.send(MessageBuilder.withPayload(order).build());
            });
    }

    public Stream<EccangOrder> getOrderUnarchive(EccangOrderVO vo) {
        return getOrderUnarchivePages(vo)
            .map(n -> n.getData(EccangOrder.class))
            .flatMap(n -> n.stream());
    }

    public Stream<EccangOrder> getOrderPlusArchiveSince(EccangOrderVO vo, Integer startYear) {
        int currentYear = Year.now().getValue();

        return IntStream.rangeClosed(startYear, currentYear).boxed()
            .flatMap(year ->
                getOrderPlusArchivePages(vo, year)
                    .map(n -> n.getData(EccangOrder.class))
                    .flatMap(n -> n.stream())
            );
    }

    public Stream<EccangPage> getOrderPlusArchivePages(EccangOrderVO orderParams, Integer year) {
        return Stream.concat(getOrderUnarchivePages(orderParams), getOrderArchivePages(orderParams, year));
    }


    public Stream<EccangPage> getOrderArchivePages(EccangOrderVO orderParams, Integer year) {
        orderParams.setYear(year);
        Stream<EccangPage> stream;
        try {
            stream = getOrderUnarchivePages(orderParams);
        } catch (EccangResponseException e) {
            for (EccangResponse.EccangError eccangError : e.getEccangError()) {
                if (eccangError.getErrorCode().equals("10001")){
                    log.info("当前{}年不存在归档信息,跳过",year);
                    return Stream.empty();//跳过
                }
            }
            throw e;
        }
        return stream;
    }


    public Stream<EccangPage> getOrderUnarchivePages(EccangOrderVO orderParams) {
        orderParams.setGetAddress(1);
        orderParams.setGetDetail(1);
        return getAllPage(JsonUtilsX.toJSONObject(orderParams), "getOrderList");
    }

    public EccangProduct getProduct(String sku) {
        //需要返回箱规信息
        EccangProduct product = EccangProduct.builder()
            .productSku(sku).getProductBox(1)
            .build();
        // String response = post("getWmsProductList", product, String.class).get(0);
        // log.debug(response);
        // return JsonUtilsSomle.parseObject(response, EccangProduct.class);
        List<EccangProduct> getWmsProductList = post("getWmsProductList", product, EccangProduct.class);
        if (CollUtil.isNotEmpty(getWmsProductList)) {
            return getWmsProductList.get(0);
        }
        return null;
    }

    public Stream<EccangPage> getInventory() {
        var payload = JsonUtilsX.newObject();
        return getAllPage(payload, "getProductInventory");
    }


    public Stream<EccangPage> getInventoryBatchLog(EccangInventoryBatchLogVO eccangInventoryBatchLogVO) {
        List<EccangWarehouse> warehouseList = getWarehouseList();
        var codeList = warehouseList.stream().map(EccangWarehouse::getWarehouseCode).toList();
        eccangInventoryBatchLogVO.setWarehouseCode(codeList);
        return getAllPage(JsonUtilsX.toJSONObject(eccangInventoryBatchLogVO), "getInventoryBatchLog");
    }

    public Stream<EccangPage> getInventoryBatch(EccangInventoryBatchReqVO eccangInventoryBatchVO) {
        return getAllPage(JsonUtilsX.toJSONObject(eccangInventoryBatchVO), "getInventoryBatch");
    }

    public EccangPage addDepartment(EccangCategory department) {
        try {
            log.info("adding department: " + department.toString());
            var result = post("editCategory", department);
            log.debug(result.toString());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("同步" + department.getPcName() + "时出现异常：" + e.getMessage());
        }
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
        if (!Objects.equals(code, "200")) {
            throw new RuntimeException("批量添加商品失败,原因：" + syncBatchProduct.getBizContentString());
        }
    }

    public Stream<EccangCategory> getCategories() {
        return list("categotyList", EccangCategory.class);
    }

    public EccangCategory getCategoryByName(String name) {
        Stream<EccangCategory> eccangCategoryStream = getCategories().filter(n -> n.getPcName().equals(name));
        Optional<EccangCategory> first = eccangCategoryStream.findFirst();
        return first.orElse(null);
    }

    /**
     * @return com.somle.eccang.model.EccangCategory
     * @Author Wqh
     * @Description 这里的deptId在易仓中对应的是英文品名，对应erp中的部门id
     * @Date 9:30 2024/11/26
     * @Param [name]
     **/
    public EccangCategory getCategoryByErpDeptId(String deptId) {
        List<EccangCategory> eccangCategories = getCategories().filter(n -> n.getPcNameEn().equals(deptId)).toList();
        if (eccangCategories.size() > 1) {
            throw new RuntimeException("erp部门id在易仓中存在多个，请检查");
        }
        return !eccangCategories.isEmpty() ? eccangCategories.get(0) : null;
    }

    public EccangCategory getCategoryByNameEn(String nameEn) {
        return getCategories().filter(n -> n.getPcNameEn().equals(nameEn)).findFirst().get();
    }

    public Stream<EccangOrganization> getOrganizations() {
        return list("getUserOrganizationAll", EccangOrganization.class);
    }

    public EccangOrganization getOrganizationByNameEn(String nameEn) {
        log.debug("searching organization with name_en " + nameEn);
        Optional<EccangOrganization> first = getOrganizations().filter(n -> n.getNameEn().equals(nameEn)).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        throw new RuntimeException("您传入的部门信息不存在于eccang信息库中");
    }

    public Stream<EccangProduct> getProducts() {
        return list("getWmsProductList", EccangProduct.class);
    }

    /**
     * @return java.util.stream.Stream<com.somle.eccang.model.EccangUserAccount>
     * @Author Wqh
     * @Description 获取运输方式
     * @Date 15:44 2024/11/29
     * @Param []
     **/
    public Stream<EccangShippingMethod> getShippingMethod() {
        return list("getShippingMethod", EccangShippingMethod.class);
    }

    /**
     * @return java.util.stream.Stream<com.somle.eccang.model.EccangResponse.EccangPage>
     * @Author Wqh
     * @Description ram管理——退款订单列表
     * @Date 17:18 2024/12/17
     * @Param []
     **/
    public Stream<EccangPage> getRmaRefundList(EccangRmaRefundVO eccangRmaRefundVO) {
        return getAllPage(JsonUtilsX.toJSONObject(eccangRmaRefundVO), "getRmaRefundList");
    }

    //退件列表
    public Stream<EccangPage> getRmaReturnList(EccangRmaReturnReqVO eccangRmaReturnReqVO){
        return getAllPage(JsonUtilsX.toJSONObject(eccangRmaReturnReqVO), "getRmaReturnList");
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
