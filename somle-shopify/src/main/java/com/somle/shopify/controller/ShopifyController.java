package com.somle.shopify.controller;


import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.shopify.service.ShopifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/shopify")
public class ShopifyController {
    @Autowired
    ShopifyService service;

    @GetMapping("/products")
    public JSONObject products(
    ) {
        return service.client.getProducts();
    }
}