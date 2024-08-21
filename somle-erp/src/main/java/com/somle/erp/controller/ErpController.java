package com.somle.erp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.somle.erp.model.ErpDepartment;
import com.somle.erp.model.ErpStyleSku;
import com.somle.erp.service.ErpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;

// @Validated
@RestController
@RequestMapping("/api/erp")
public class ErpController {

    @Autowired
    ErpService erpService;

    @Autowired
    private MessageChannel productChannel;

    // @PostMapping("/addProduct")
    // @ResponseBody
    // public JSONObject addProduct( 
    //     // @RequestParam String sku
    //     @NotEmpty(message = "产品列表不能为空")
    //     @RequestBody List<EsbCountrySku> productList
    //     // @RequestBody List<@Valid EsbProduct> productList
    // ) {
    //     if (esbService.addProduct(productList)) {
    //         JSONObject result = new JSONObject();
    //         result.put("status", "200");
    //         return result;
    //     } else {
    //         throw new RuntimeException("Add prouct failed");
    //     }

    // }

    // @PostMapping("/addProduct")
    // @ResponseBody
    // public JSONObject addProduct(
    //     @Validated @RequestBody Product product
    // ) {
    //     productChannel.send(MessageBuilder.withPayload(product).build());
    //     JSONObject result = new JSONObject();
    //     result.put("code", "200");
    //     return result;
    // }
    
    // @PostMapping("/test")
    // public void test() {
    //     esbService.test();
    // }

    @PostMapping("/github-webhook")
    public String webhook( 
    ) {
        return "hello";
    }

    @GetMapping("/departmentTree")
    public ErpDepartment getEsbDepartmentTree() {
        return erpService.getEsbDepartmentTree(53111133l);
    }

    @PostMapping("/getStyleSku")
    public Page<ErpStyleSku> getStyleSku(
        @RequestBody ErpStyleSku styleSku,
        Pageable pageable
    ) {
        return erpService.getStyleSku(styleSku, pageable);
    }

    @PostMapping("/saveStyleSku")
    public JSONObject saveStyleSku(
        @RequestBody ErpStyleSku styleSku
    ) {
        if (erpService.saveStyleSku(styleSku)) {
            JSONObject result = new JSONObject();
            result.put("status", "200");
            return result;
        } else {
            throw new RuntimeException("Add style sku failed");
        }

    }


}