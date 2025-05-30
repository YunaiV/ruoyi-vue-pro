package com.somle.shopify.enums;

/**
 * @author: LeeFJ
 * @date: 2025/2/11 10:02
 * @description:
 */


import cn.iocoder.yudao.framework.common.util.json.JSONArray;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.web.RequestX;

/**
 * @Description Shopify 接口定义
 */
public enum ShopifyAPI {

    GET_SHOP("获取 Shopify 店铺信息", "/admin/api/2024-10/shop.json", RequestX.Method.GET, "shop", JSONObject.class),
    GET_ORDERS("获取 Shopify 订单信息", "/admin/api/2024-10/orders.json", RequestX.Method.GET, "orders", JSONArray.class),
    GET_PRODUCTS("获取 Shopify 商品信息", "/admin/api/2024-10/products.json", RequestX.Method.GET, "products", JSONArray.class),
    GET_PRODUCT_COUNT("获取 Shopify 商品数量", "/admin/api/2024-10/products/count.json", RequestX.Method.GET, "count", JSONArray.class),
    GET_PAYOUTS("获取 Shopify 结算信息", "/admin/api/2024-10/shopify_payments/payouts.json", RequestX.Method.GET, "payouts", JSONArray.class);

    private String action;
    private String url;
    private RequestX.Method method;
    private String jsonField;
    private Class returnType;

    ShopifyAPI(String action, String url, RequestX.Method method, String jsonField, Class returnType) {
        this.action = action;
        this.url = url;
        this.method = method;
        this.jsonField = jsonField;
        this.returnType = returnType;
    }

    public String action() {
        return action;
    }

    public String url() {
        return url;
    }

    public String jsonField() {
        return jsonField;
    }

    public RequestX.Method method() {
        return method;
    }

    public Class returnType() {
        return returnType;
    }

    /**
     * @Description 获取从报文获得有效数据
     **/
    public <T> T getData(JSONObject result, Class<T> type) {
        if (result == null) {
            return null;
        }
        if (JSONObject.class.isAssignableFrom(type)) {
            return (T) result.getJSONObject(this.jsonField());
        } else if (JSONArray.class.isAssignableFrom(type)) {
            return (T) result.getJSONArray(this.jsonField());
        } else {
            return null;
        }

    }

}