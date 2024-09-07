package com.somle.erp.controller;

import com.somle.erp.model.ErpDepartment;
import com.somle.erp.model.ErpStyleSku;
import com.somle.erp.service.ErpService;

import com.somle.framework.common.pojo.CommonResult;
import com.somle.framework.common.util.json.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;

// @Validated
@RestController
@RequestMapping("/admin-api/erp")
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
    //         JSONObject result = JsonUtils.newObject();
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
    //     JSONObject result = JsonUtils.newObject();
    //     result.put("code", "200");
    //     return result;
    // }
    
    // @PostMapping("/test")
    // public void test() {
    //     esbService.test();
    // }

    @GetMapping("/departmentTree")
    public ErpDepartment getEsbDepartmentTree() {
        return erpService.getEsbDepartmentTree(53111133l);
    }



}