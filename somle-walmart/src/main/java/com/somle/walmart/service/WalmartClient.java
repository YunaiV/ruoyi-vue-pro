package com.somle.walmart.service;


import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.io.IoUtils;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.alibaba.fastjson.JSON;
import com.somle.walmart.model.WalmartToken;
import com.somle.walmart.model.reps.WalmartAllProductsRepsVO;
import com.somle.walmart.model.reps.WalmartInventoryResp;
import com.somle.walmart.model.reps.WalmartItemDetailResp;
import com.somle.walmart.model.reps.WalmartOrderResponse;
import com.somle.walmart.model.req.WalmartAllProductsReqVO;
import com.somle.walmart.model.req.WalmartOrderReqVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public abstract class WalmartClient {


    public WalmartToken token;

    private String accessToken;

    public WalmartClient(WalmartToken token) {
        this.token = token;
        this.accessToken = getAccessToken();
    }

    /**
     * 对密钥进行编码
     */
    private String getAuthorization(WalmartToken token) {
        String str = token.getClientId() + ":" + token.getClientSecret();
        return "Basic " + Base64.encodeBase64String(str.getBytes());
    }

    Headers commonHeaders() {
        return new Headers.Builder()
            .add("WM_QOS.CORRELATION_ID", token.getClientId())
            .add("WM_SVC.NAME", token.getSvcName())
            .add("Accept", "application/json")
            .build();

    }

    Headers normalHeaders() {
        return commonHeaders().newBuilder()
            .add("WM_SEC.ACCESS_TOKEN", getAccessToken())
            .build();
    }

    abstract Headers headers();

    abstract HttpUrl url(String endpoint);


    @SneakyThrows
    public String getAccessToken() {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials");
        var url = url("v3/token");
        Request request = new Request.Builder()
            .url(url)
            .method("POST", body)
            .headers(commonHeaders())
            .addHeader("Authorization", getAuthorization(token))
            .build();

        log.info(request.toString());
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result.getString("access_token");
    }


    @Scheduled(cron = "0 */7 * * * *")
    public void refreshAccessToken() {
        accessToken = getAccessToken();
    }

    @SneakyThrows
    public WalmartOrderResponse getOrders(WalmartOrderReqVO vo) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        var url = url("v3/orders");
        var headers = WebUtils.merge(headers(), WebUtils.toHeaders(vo));
        Request request = new Request.Builder()
            .url(url)
            .method("GET", null)
            .headers(headers)
            .build();
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        WalmartOrderResponse walmartOrderResponse = JSON.parseObject(bodyString, WalmartOrderResponse.class);
        return walmartOrderResponse;
    }

    @SneakyThrows
    public List<String> getAvailableReconFileDates() {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        var url = url("v3/report/reconreport/availableReconFiles").newBuilder()
            .addQueryParameter("reportVersion", "v1")
            .build();
        Request request = new Request.Builder()
            .url(url)
            .method("GET", null)
            .headers(headers())
            .build();
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result.getStringList("availableApReportDates");
    }

    @SneakyThrows
    public String getReconFile(String date) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        var url = url("v3/report/reconreport/availableReconFiles").newBuilder()
            .addQueryParameter("reportVersion", "v1")
            .addQueryParameter("reportDate", date)
            .build();
        Request request = new Request.Builder()
            .url(url)
            .method("GET", null)
            .headers(headers())
            .addHeader("Accept", "application/octet-stream")
            .build();
        Response response = client.newCall(request).execute();
        // Execute the request
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        // Get the Content-Disposition header
//        String contentDisposition = response.header("Content-Disposition");
//        if (contentDisposition == null || !contentDisposition.contains("filename=")) {
//            throw new IOException("Filename not found in Content-Disposition header");
//        }
//
//        String zipFileName = contentDisposition.split("filename=")[1];
//        log.info("zip file name: " + zipFileName);

        // Save the response body as a zip file
        InputStream inputStream = response.body().byteStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;
        String result = null;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".csv")) {
                result = IoUtils.readUtf8(zipInputStream, false);
            }
            zipInputStream.closeEntry();
        }

        return result;
    }

    public String getReconFile(LocalDate date) {
        var formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        return getReconFile(date.format(formatter));
    }

    @SneakyThrows
    public JSONObject getPaymentStatement() {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        var url = url("v3/report/payment/statement");
        Request request = new Request.Builder()
            .url(url)
            .method("GET", null)
            .headers(headers())
            .build();
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result;
    }


    /**
     * @param walmartAllItemsReqVO 获取所有商品信息请求体
     * @Author: gumaomao
     * @Date: 2025/03/25
     * @Description: 获取所有商品信息
     */
    @SneakyThrows
    public List<WalmartAllProductsRepsVO.ItemResponseDTO> getAllProducts(WalmartAllProductsReqVO walmartAllItemsReqVO) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        int offset = walmartAllItemsReqVO.getOffset();
        int limit = walmartAllItemsReqVO.getLimit();
        HttpUrl url = url("v3/items");
        List<WalmartAllProductsRepsVO.ItemResponseDTO> allProducts = new ArrayList<>();
        while (true) {
            HttpUrl.Builder urlBuilder = url.newBuilder();
            urlBuilder.addQueryParameter("offset", String.valueOf(offset));
            urlBuilder.addQueryParameter("limit", String.valueOf(limit));
            urlBuilder.addQueryParameter("publishedStatus", "PUBLISHED");
            Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .method("GET", null)
                .headers(headers())
                .build();
            Response response = client.newCall(request).execute();
            var bodyString = response.body().string();
            WalmartAllProductsRepsVO walmartAllItemsResVO = JSON.parseObject(bodyString, WalmartAllProductsRepsVO.class);
            if (CollUtil.isEmpty(walmartAllItemsResVO.getItemResponse())) {
                break;
            }
            offset += limit;
            //防止限流
            TimeUnit.SECONDS.sleep(2);
            walmartAllItemsResVO.getItemResponse().stream().forEach(item -> {
                WalmartInventoryResp inventory = getInventory(item.getSku());
                if (inventory.getQuantity() != null) {
                    item.setSellableQty(inventory.getQuantity().getAmount());
                }
            });
            allProducts.addAll(walmartAllItemsResVO.getItemResponse());
        }
        return allProducts;
    }


    public List<WalmartItemDetailResp> getAllProductDetails(WalmartAllProductsReqVO vo) {
        List<WalmartAllProductsRepsVO.ItemResponseDTO> products = getAllProducts(vo);
        Map<String, WalmartAllProductsRepsVO.ItemResponseDTO> quantityMap = products.stream()
            .collect(Collectors.toMap(
                WalmartAllProductsRepsVO.ItemResponseDTO::getSku,
                product -> product,
                (existing, replacement) -> existing
            ));
        List<String> skus = products.stream().map(WalmartAllProductsRepsVO.ItemResponseDTO::getSku).distinct().toList();
        List<WalmartItemDetailResp> list = skus.stream().map(sku -> retrieveSingleItemFullDetail(sku)).toList();
        list.forEach(item -> {
            MapUtils.findAndThen(
                quantityMap,
                item.getItemResponse().getSku(),
                itemResponseDTO -> {
                    item.getItemResponse().setSellableQty(itemResponseDTO.getSellableQty());
                    item.getItemResponse().setSalePrice(itemResponseDTO.getPrice().getAmount());
                    item.getItemResponse().setCurrency(itemResponseDTO.getPrice().getCurrency());
                }
            );
        });
        return list;
    }

    @SneakyThrows
    public WalmartItemDetailResp retrieveSingleItemFullDetail(String sku) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        HttpUrl url = url("v4/items/" + sku);
        HttpUrl.Builder urlBuilder = url.newBuilder();
        urlBuilder.addQueryParameter("productIdType", "SKU");
        urlBuilder.addQueryParameter("includeFullItemDetails", "YES");
        Request request = new Request.Builder()
            .url(urlBuilder.build().toString())
            .method("GET", null)
            .headers(headers())
            .build();

        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        //防止限流
        TimeUnit.SECONDS.sleep(2);
        WalmartItemDetailResp walmartItemDetailResp = JSON.parseObject(bodyString, WalmartItemDetailResp.class);
        return walmartItemDetailResp;
    }


    /**
     * @param sku sku
     * @Description: 获取库存
     * @return:
     */
    @SneakyThrows
    public WalmartInventoryResp getInventory(String sku) {
        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        HttpUrl url = url("/v3/inventory");
        HttpUrl.Builder urlBuilder = url.newBuilder();
        urlBuilder.addQueryParameter("sku", sku);
        Request request = new Request.Builder()
            .url(urlBuilder.build().toString())
            .method("GET", null)
            .headers(headers())
            .build();

        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        //防止限流
        TimeUnit.SECONDS.sleep(2);
        WalmartInventoryResp walmartInventoryResp = JSON.parseObject(bodyString, WalmartInventoryResp.class);
        return walmartInventoryResp;
    }
}