package com.somle.shopify.controller;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.shopify.service.ShopifyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;

@RestController
@RequestMapping("/api/shopify")
public class ShopifyController {
//    @Autowired
//    ShopifyClient client;
//
//    @GetMapping("/products")
//    public JSONObject products() {
//        return client.getRawProducts(new HashMap<>());
//    }
}