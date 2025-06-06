package com.somle.amazon.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.general.CoreUtils;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.amazon.controller.vo.*;
import com.somle.amazon.model.AmazonSpAuthDO;
import com.somle.amazon.model.enums.AmazonException;
import com.somle.amazon.model.enums.AmazonRegion;
import com.somle.amazon.model.reps.AmazonSpListingRepsVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
@Getter
@Setter
public class AmazonSpClient {


    private AmazonSpAuthDO auth;

    private String getEndPoint() {
        return AmazonRegion.findByCode(auth.getRegionCode()).getSpUrl();
    }

    private Map<String, String> generateHeaders(AmazonSpAuthDO auth) {
        var headers = Map.of("x-amz-access-token", auth.getAccessToken());
        return headers;
    }

//    @Transactional(readOnly = true)
//    public JSONObject getAccount(AmazonSeller seller) {
//        String endPoint = seller.getRegion().getSpEndPoint();
//        String partialUrl = "/sellers/v1/account";
//        String fullUrl = endPoint + partialUrl;
//        var request = RequestX.builder()
//            .requestMethod(RequestX.Method.GET)
//            .url(fullUrl)
//            .headers(generateHeaders(seller))
//            .build();
//        return WebUtils.sendRequest(request, JSONObject.class);
//    }

    @SneakyThrows
    public List<AmazonSpMarketplaceParticipationVO> getMarketplaceParticipations() {
        String endPoint = getEndPoint();
        String partialUrl = "/sellers/v1/marketplaceParticipations";
        String fullUrl = endPoint + partialUrl;


        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .headers(generateHeaders(auth))
            .build();
        var response = WebUtils.sendRequest(request, AmazonSpMarketplaceParticipationRespVO.class);
        return response.getPayload().stream().filter(amazonSpMarketplaceParticipationVO -> !amazonSpMarketplaceParticipationVO.getParticipation().isHasSuspendedListings()).toList();
    }


//    @Transactional(readOnly = true)
//    public Stream<AmazonShop> getShops() {
//        return account.getSellers().stream().flatMap(seller->seller.getShops().stream());
//    }

    //    @Transactional(readOnly = true)
//    public AmazonShop getShop(String countryCode) {
//        return getShops().filter(shop->shop.getCountry().getCode().equals(countryCode)).findFirst().get();
//    }
//
    @SneakyThrows
    public AmazonSpListingRepsVO searchListingsItems(AmazonSpListingReqVO reqVO) {
        String endPoint = getEndPoint();
        String partialUrl = "/listings/2021-08-01/items/" + auth.getSellerId();
        String fullUrl = endPoint + partialUrl;


        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(reqVO)
            .headers(generateHeaders(auth))
            .build();
        try (Response response = WebUtils.sendRequest(request)) {
            String bodyString = response.body().string();
            AmazonSpListingRepsVO amazonSpListingRepsVO = JSONUtil.toBean(bodyString, AmazonSpListingRepsVO.class);
            return amazonSpListingRepsVO;
        }
    }


//    public void validateResponse(AmazonSpOrderRespVO response) {
//        if (!CollectionUtils.isEmpty(response.getErrors())) {
//            throw new RuntimeException("Amazon sp error response: " + response);
//        }
//    }

    @SneakyThrows
    public void validateResponse(Response response) {
        switch (response.code()) {
            case 200:
                break;
            case 202:
                break;
            case 403:
                var error = WebUtils.parseResponse(response, AmazonSpErrorListVO.class);
                switch (error.getErrors().get(0).getCode()) {
                    case "Unauthorized":
                        log.error(error.toString());
                        throw new RuntimeException("Error unauthorized");
                    default:
                        break;
                }
                throw new RuntimeException("Error creating report: " + error);
            case 429:
                throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, response.body().string());
            default:
                throw new RuntimeException("Unknown response code: " + response.code() + "Detail: " + response.body().string());
        }
    }


    @SneakyThrows
    public AmazonSpOrderRespVO getOrder(AmazonSpOrderReqVO vo) {
        String endPoint = getEndPoint();
        String partialUrl = "/orders/v0/orders";
        String fullUrl = endPoint + partialUrl;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(vo)
            .headers(generateHeaders(auth))
            .build();
        try (var response = WebUtils.sendRequest(request)) {
            var bodyString = response.body().string();
            AmazonSpOrderRespVO result = JSONUtil.toBean(bodyString, AmazonSpOrderRespVO.class);
            if (result.getPayload() != null) {
                result.getPayload().getOrders().forEach(order -> {
                    AmazonSpOrderItemReqVO amazonSpOrderItemReqVO = AmazonSpOrderItemReqVO.builder().orderId(order.getAmazonOrderId()).build();
                    order.setOrderItems(getAllOrderItems(amazonSpOrderItemReqVO));
                });
            }
            return result;
        }
    }

    @SneakyThrows
    public String getOrderBuyerInfo(String orderId) {
        String endPoint = getEndPoint();
        String partialUrl = "/orders/v0/orders/" + orderId + "/buyerInfo";
        String fullUrl = endPoint + partialUrl;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .headers(generateHeaders(auth))
            .build();
        var response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        return bodyString;
    }

    @SneakyThrows
    public Stream<AmazonSpOrderRespVO> streamOrder(AmazonSpOrderReqVO vo) {
        return StreamX.iterate(
            getOrder(vo),
            page -> page.getPayload() != null
                && !StrUtils.isEmpty(page.getPayload().getNextToken()),
            page -> {
                var nextToken = page.getPayload().getNextToken();
                var reqVO = AmazonSpOrderReqVO.builder()
                    .nextToken(nextToken)
                    .build();
                return getOrder(reqVO);
            }
        );
    }

    public List<AmazonSpOrderRespVO.Order> getAllOrders(AmazonSpOrderReqVO vo) {
        List<AmazonSpOrderRespVO> amazonSpOrderRespVOS = streamOrder(vo).toList();
        if (amazonSpOrderRespVOS.isEmpty()) {
            return Collections.emptyList();
        }

        return amazonSpOrderRespVOS.stream()  // 1. 转为 Stream
            .filter(Objects::nonNull)  // 2. 过滤掉 null 的 page
            .map(AmazonSpOrderRespVO::getPayload)  // 3. 提取 payload
            .filter(Objects::nonNull)  // 4. 过滤掉 null 的 payload
            .map(AmazonSpOrderRespVO.Payload::getOrders)  // 5. 提取 orders
            .filter(Objects::nonNull)  // 6. 过滤掉 null 的 orders
            .flatMap(List::stream)  // 7. 扁平化 orders 流
            .filter(order -> order != null
                && CollectionUtil.isNotEmpty(order.getOrderItems()))  // 8. 过滤 null 订单 + 空 OrderItems
            .toList();  // 9. 收集结果
    }

    public List<AmazonSpOrderItemRespVO.OrderItem> getAllOrderItems(AmazonSpOrderItemReqVO vo) {
        AmazonSpOrderItemRespVO amazonSpOrderItemRespVO = getOrderItem(vo);
        List<AmazonSpOrderItemRespVO.OrderItem> orderItems = Optional.ofNullable(amazonSpOrderItemRespVO.getPayload())
            .map(AmazonSpOrderItemRespVO.Payload::getOrderItems)
            .orElse(Collections.emptyList());
        AmazonSpOrderItemRespVO.Payload payload = amazonSpOrderItemRespVO.getPayload();
        while (payload != null && StrUtil.isNotEmpty(payload.getNextToken())) {
            vo = vo.builder().nextToken(payload.getNextToken()).build();
            amazonSpOrderItemRespVO = getOrderItem(vo);
            payload = amazonSpOrderItemRespVO.getPayload();
            orderItems.addAll(payload.getOrderItems());
        }
        return orderItems;
    }

    @SneakyThrows
    public AmazonSpOrderItemRespVO getOrderItem(AmazonSpOrderItemReqVO vo) {
        String endPoint = getEndPoint();
        String partialUrl = "/orders/v0/orders/" + vo.getOrderId() + "/orderItems";
        String fullUrl = endPoint + partialUrl;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(vo)
            .headers(generateHeaders(auth))
            .build();
        try (var response = WebUtils.sendRequest(request)) {
            var bodyString = response.body().string();
            AmazonSpOrderItemRespVO result = JSONUtil.toBean(bodyString, AmazonSpOrderItemRespVO.class);
            return result;
        }
    }


    public List<AmazonSpReportRespVO> listReports(AmazonSpReportReqVO vo) {
        log.info("get reports");

        String endPoint = getEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(vo)
            .headers(generateHeaders(auth))
            .build();
        try (var response = WebUtils.sendRequest(request)) {
            var reportsString = WebUtils.parseResponse(response, JSONObject.class).get("reports");
            var reportList = JsonUtilsX.parseArray(reportsString, AmazonSpReportRespVO.class);
            return reportList;
        }
    }

    public Stream<String> getReportStream(AmazonSpReportReqVO vo) {
        return listReports(vo).stream().map(report -> waitAndGetReportDocumentString(report.getReportId()));
    }

    public String getReportOrNull(String reportId) {
        String report = null;
        try {
            report = waitAndGetReportDocumentString(reportId);
        } catch (AmazonException.ReportCancelledException e) {
        }
        return report;
    }

    @SneakyThrows
    public AmazonSpReportRespVO getReport(String reportId) {
        log.info("get report");
        int RETENTION_DAYS = 720;
        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String endPoint = getEndPoint();

        // Check report status and get document ID
        String reportStatusUrl = endPoint + "/reports/2021-06-30/reports/" + reportId;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .headers(generateHeaders(auth))
            .url(reportStatusUrl)
            .build();
        try (var response = WebUtils.sendRequest(request)) {
            validateResponse(response);
            return WebUtils.parseResponse(response, AmazonSpReportRespVO.class);
        }
    }

    @SneakyThrows
    public AmazonSpReportDocumentRespVO getReportDocument(String docId) {
        // Use document ID to get download URL
        String documentUrl = getEndPoint() + "/reports/2021-06-30/documents/" + docId;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(documentUrl)
            .headers(generateHeaders(auth))
            .build();
        try (var response = WebUtils.sendRequest(request)) {
            validateResponse(response);
            return JsonUtilsX.parseObject(response.body().string(), AmazonSpReportDocumentRespVO.class);
        }
    }

    @SneakyThrows
    public String waitAndGetReportDocumentString(String reportId) {
        // Check report status and get document ID
        String status = null;
        AmazonSpReportRespVO respVO = null;
        while (!"DONE".equals(status)) {
            respVO = getReport(reportId);
            status = respVO.getProcessingStatus();
            switch (status) {
                case "CANCELLED":
                    throw new AmazonException.ReportCancelledException(reportId);
                case "IN_QUEUE":
                    continue;
                case "IN_PROGRESS":
                    continue;
                case "DONE":
                    break;
                default:
                    throw new RuntimeException("Unknown status code: " + status);
            }
        }

        var docRespVO = getReportDocument(respVO.getReportDocumentId());
        // Use util to process the document URL
        return WebUtils.urlToString(docRespVO.getUrl(), Objects.toString(docRespVO.getCompressionAlgorithm(), null));
    }

    @SneakyThrows
    public String createReport(AmazonSpReportSaveVO vo) {

        String endPoint = getEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;

        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("creating report");
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(fullUrl)
                .headers(generateHeaders(auth))
                .payload(vo)
                .build();
            reportId = CoreUtils.retry(ctx -> {
                // 获取当前重试次数
                int retryCount = ctx.getRetryCount();
                if (retryCount != 0) {
                    // 记录每次重试的日志
                    log.debug("遇到错误: {}", ctx.getLastThrowable().getStackTrace().toString());
                    log.debug("正在请求url= {},第 {} 次重试。", request.getUrl(), retryCount);
                }
                try (var response = WebUtils.sendRequest(request)) {
                    validateResponse(response);
                    var report = WebUtils.parseResponse(response, AmazonSpReportRespVO.class);
                    return report.getReportId();
                }
            });

        }
        log.info("Got report ID: {}", reportId);
        return reportId;
    }

    public String createAndGetReport(AmazonSpReportSaveVO vo) {
        String reportId = createReport(vo);
        var reportString = waitAndGetReportDocumentString(reportId);
        return reportString;
    }

    public String createAndGetReportOrNull(AmazonSpReportSaveVO vo) {
        String reportId = createReport(vo);
        var reportString = getReportOrNull(reportId);
        return reportString;
    }


    public List<AmazonSpListingRepsVO.ProductItem> getProducts(List<String> marketplaceIds) {
        List<AmazonSpListingRepsVO.ProductItem> allProducts = new ArrayList<>();
        collectListingItems(allProducts, marketplaceIds, null, 4);
        return allProducts;
    }

    /**
     * 分页采集 Listing 数据
     **/
    private void collectListingItems(List<AmazonSpListingRepsVO.ProductItem> allProducts, List<String> marketplaceIds, String pageToken, Integer retryTimes) {

        // 控制重试次数
        if (retryTimes > 5) {
            log.error("collectListingItems - mktId=" + marketplaceIds.get(0) + "@" + getAuth().getClientId() + " - pageToken=" + pageToken + " - retryTimes=" + retryTimes + " - 超过最大重试次数，不再继续执行");
            return;
        }

        var reqVO = AmazonSpListingReqVO.builder()
            .sellerId(getAuth().getSellerId())
            .pageSize(20)
            .pageToken(pageToken)
            .marketplaceIds(marketplaceIds)
            .includedData(List.of(AmazonSpListingReqVO.IncludedData.ATTRIBUTES, AmazonSpListingReqVO.IncludedData.SUMMARIES, AmazonSpListingReqVO.IncludedData.OFFERS, AmazonSpListingReqVO.IncludedData.FULFILLMENT_AVAILABILITY, AmazonSpListingReqVO.IncludedData.PROCUREMENT, AmazonSpListingReqVO.IncludedData.RELATIONSHIPS, AmazonSpListingReqVO.IncludedData.PRODUCT_TYPES, AmazonSpListingReqVO.IncludedData.ISSUES))
            .build();
        AmazonSpListingRepsVO amazonSpListingRepsVO = searchListingsItems(reqVO);

        if (CollectionUtil.isNotEmpty(amazonSpListingRepsVO.getItems())) {
            allProducts.addAll(amazonSpListingRepsVO.getItems());
        }

        // 如果有下一页则继续采集下一页
        if (amazonSpListingRepsVO.getPagination() != null && StrUtils.isNonEmpty(amazonSpListingRepsVO.getPagination().getNextToken())) {
            collectListingItems(allProducts, marketplaceIds, amazonSpListingRepsVO.getPagination().getNextToken(), retryTimes);
        }
    }

}

