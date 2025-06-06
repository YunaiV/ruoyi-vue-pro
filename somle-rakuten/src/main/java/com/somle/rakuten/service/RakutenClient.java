package com.somle.rakuten.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.alibaba.fastjson.JSON;
import com.somle.rakuten.enums.RakutenOrderStatusEnum;
import com.somle.rakuten.model.pojo.PaginationRequestModel;
import com.somle.rakuten.model.pojo.RakutenTokenEntityDO;
import com.somle.rakuten.model.reps.RakutenInventoryRepsVO;
import com.somle.rakuten.model.reps.RakutenOrderRepsVO;
import com.somle.rakuten.model.reps.RakutenProductsRepsVO;
import com.somle.rakuten.model.reps.RakutenSearchOrderRepsVO;
import com.somle.rakuten.model.req.RakutenInventoryReqVO;
import com.somle.rakuten.model.req.RakutenOrderReqVO;
import com.somle.rakuten.model.req.RakutenOrderSearchReqVO;
import com.somle.rakuten.util.ZonedDateTimeConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RakutenClient {
    private final String accessToken;
    private static final String BASE_URL = "https://api.rms.rakuten.co.jp";

    public RakutenTokenEntityDO entity;

    public RakutenClient(RakutenTokenEntityDO entity) {
        this.accessToken = generateAuthorization(entity);
        this.entity = entity;
    }

    /**
     * 对密钥进行编码
     */
    private String generateAuthorization(RakutenTokenEntityDO entity) {
        //数据库密钥过期了
        if (entity.getExpires().before(DateUtil.date().toJdkDate())) {
            throw new RuntimeException("Rakuten 密钥过期了");
        }
        String credentials = entity.getServiceSecret() + ":" + entity.getLicenseKey();
        //↓有个空格
        return "ESA" + " " + Base64.encode(credentials.getBytes());
    }


    @SneakyThrows
    public RakutenProductsRepsVO getProduct(String offset) {
        String endpoint = "/es/2.0/items/search?offset=" + offset + "&hits=100&isHiddenItem=false";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .build();
        Response response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        RakutenProductsRepsVO repsVO = JSONUtil.toBean(bodyString, RakutenProductsRepsVO.class);
        return repsVO;
    }

    public List<RakutenProductsRepsVO.Item> getAllProducts() {
        List<RakutenProductsRepsVO.Item> allProducts = new ArrayList<>();
        Integer offset = 0;
        RakutenProductsRepsVO product = getProduct(String.valueOf(offset));
        allProducts.addAll(product.getResults().stream().map(RakutenProductsRepsVO.Result::getItem).toList());
        while (product.getNumFound() > allProducts.size()) {
            offset += 100;
            product = getProduct(String.valueOf(allProducts.size()));
            List<RakutenProductsRepsVO.Item> list = product.getResults().stream().map(RakutenProductsRepsVO.Result::getItem).toList();
            for (RakutenProductsRepsVO.Item item : list) {
                String manageNumber = item.getManageNumber();
                Map<String, RakutenProductsRepsVO.Variant> variants = item.getVariants();
                for (RakutenProductsRepsVO.Variant variant : variants.values()) {
                    //查询设置库存数量
                    List<RakutenInventoryReqVO> inventories = new ArrayList<>();
                    String skuId = variant.getMerchantDefinedSkuId();
                    var vo = RakutenInventoryReqVO.builder().manageNumber(manageNumber).variantId(skuId).build();
                    inventories.add(vo);
                    RakutenInventoryRepsVO inventoryRepsVO = getInventory(inventories);
                    if (ObjectUtil.isNotEmpty(inventoryRepsVO) && CollectionUtil.isNotEmpty(inventoryRepsVO.getInventories())) {
                        RakutenInventoryRepsVO.InventoryItem inventoryItem = inventoryRepsVO.getInventories().get(0);
                        variant.setSellableQuantity(inventoryItem.getQuantity());
                    }
                }

            }

            allProducts.addAll(list);
        }
        return allProducts;
    }

    @SneakyThrows
    public List<RakutenOrderRepsVO.OrderModel> getOrders(RakutenOrderReqVO vo) {
        String endpoint = "/es/2.0/order/getOrder/";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .payload(vo)
            .build();
        Response response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        RakutenOrderRepsVO repsVO = JSON.parseObject(bodyString, RakutenOrderRepsVO.class);
        return repsVO.getOrderModelList();
    }


    @SneakyThrows
    public RakutenInventoryRepsVO getInventory(List<RakutenInventoryReqVO> inventories) {
        String endpoint = "/es/2.0/inventories/bulk-get";
        JSONObject payload = new JSONObject();
        payload.put("inventories", inventories);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .payload(payload)
            .build();
        Response response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        RakutenInventoryRepsVO repsVO = JSON.parseObject(bodyString, RakutenInventoryRepsVO.class);
        return repsVO;
    }


    @SneakyThrows
    public JSONObject getOrder(RakutenOrderReqVO vo) {
        String endpoint = "/es/2.0/order/getOrder/";
        return sendRequestAndParse(endpoint, JsonUtilsX.toJsonString(vo));
    }

    @SneakyThrows
    public RakutenSearchOrderRepsVO searchOrder(RakutenOrderSearchReqVO vo) {
        String endpoint = "/es/2.0/order/searchOrder/";
        isValidDateRange(vo.getStartDatetime().toLocalDateTime(), vo.getEndDatetime().toLocalDateTime());
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .payload(vo)
            .build();
        Response response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        RakutenSearchOrderRepsVO repsVO = JSONUtil.toBean(bodyString, RakutenSearchOrderRepsVO.class);
        return repsVO;
    }

    public List<RakutenOrderRepsVO.OrderModel> getAllOrders(ZonedDateTime startDatetime, ZonedDateTime endDatetime) {

        PaginationRequestModel paginationReqModel = PaginationRequestModel.builder()
            .requestPage(1)
            .requestRecordsAmount(100)
            .build();

        RakutenOrderSearchReqVO vo = RakutenOrderSearchReqVO.builder()
            .dateType(3)
            .startDatetime(startDatetime)
            .endDatetime(endDatetime)
            .paginationRequestModel(paginationReqModel)
            .build();
        List<RakutenOrderRepsVO.OrderModel> result = new ArrayList<>();
        RakutenSearchOrderRepsVO repsVO = searchOrder(vo);
        RakutenOrderReqVO orderReqVO = RakutenOrderReqVO.builder()
            .orderNumberList(repsVO.getOrderNumberList())
            .version(9)
            .build();
        result.addAll(getOrders(orderReqVO));

        while (repsVO.getPaginationResponseModel().getTotalPages() > repsVO.getPaginationResponseModel().getRequestPage()) {
            vo.getPaginationRequestModel().setRequestPage(vo.getPaginationRequestModel().getRequestPage() + 1);
            repsVO = searchOrder(vo);
            orderReqVO = RakutenOrderReqVO.builder()
                .orderNumberList(repsVO.getOrderNumberList())
                .version(9)
                .build();
            result.addAll(getOrders(orderReqVO));
        }
        return result;
    }


    @SneakyThrows
    public JSONObject getEndOrderIds(RakutenOrderSearchReqVO vo) {
        //检验日期范围
        isValidDateRange(vo.getStartDatetime().toLocalDateTime(), vo.getEndDatetime().toLocalDateTime());
        ArrayList<Integer> list = new ArrayList<>();
        list.add(RakutenOrderStatusEnum.SHIPPED.getCode());
        list.add(RakutenOrderStatusEnum.PAYMENT_COMPLETED.getCode());
        vo.setOrderProgressList(list);

        return sendRequestAndParse("/es/2.0/order/searchOrder/", convertToJson(vo).toString());

    }

    private Response sendPostRequest(String endpoint, String requestBody) {
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .payload(JsonUtilsX.parseObject(requestBody, JSONObject.class))
            .build();
        return WebUtils.sendRequest(request);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }


    private void isValidDateRange(LocalDateTime startDatetime, LocalDateTime endDatetime) {
        // 确保 endDatetime 大于 startDatetime
        LocalDateTimeUtils.isValidDateRange(startDatetime, endDatetime);
        // 判断是否在63天内
        LocalDateTimeUtils.isValidDateRange(startDatetime, endDatetime, 63);
        //判断开始日期是否截至今天(日本时间)在730日内
        LocalDateTimeUtils.isValidDateRange(startDatetime, ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).toLocalDateTime(), 730);
    }

    private JSONObject convertToJson(RakutenOrderSearchReqVO vo) {
        // 将 ZonedDateTime 转换成 String 格式
        String startDatetimeStr = vo.getStartDatetime() != null ? ZonedDateTimeConverter.convertToString(vo.getStartDatetime()) : null;
        String endDatetimeStr = vo.getEndDatetime() != null ? ZonedDateTimeConverter.convertToString(vo.getEndDatetime()) : null;
        // 创建一个临时对象来存储转换后的数据
        JSONObject jsonObject = JsonUtilsX.toJSONObject(vo);
        // 手动设置日期字段为字符串格式
        jsonObject.put("startDatetime", startDatetimeStr);
        jsonObject.put("endDatetime", endDatetimeStr);
        return jsonObject;
    }


    private JSONObject sendRequestAndParse(String endpoint, String requestBody) {
        try (Response response = sendPostRequest(endpoint, requestBody)) {
            if (response.body() == null) {
                log.error("Response body is null for endpoint: {}", endpoint);
                throw new RuntimeException("Response body is null");
            }
            String responseBody = response.body().string();
            return JsonUtilsX.parseObject(response.body().string(), JSONObject.class);
        } catch (Exception e) {
            log.error("Error occurred while sending request to endpoint: {}", endpoint, e);
            throw new RuntimeException("Error occurred while sending request", e);
        }
    }
}
