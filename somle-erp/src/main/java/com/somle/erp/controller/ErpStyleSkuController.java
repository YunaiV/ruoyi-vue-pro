package com.somle.erp.controller;

import com.somle.erp.model.product.ErpStyleSku;
import com.somle.erp.service.ErpProductService;
import com.somle.framework.common.pojo.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

// @Validated
@RestController
@RequestMapping("/admin-api/erp/stylesku")
public class ErpStyleSkuController {

    @Autowired
    ErpProductService erpService;




    @GetMapping("/get")
    public CommonResult<Page<ErpStyleSku>> getStyleSku(
        ErpStyleSku styleSku,
        Pageable pageable
    ) {
        return CommonResult.success(erpService.getStyleSku(styleSku, pageable));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> createStyleSku(
        @RequestBody ErpStyleSku styleSku
    ) {
        return CommonResult.success(erpService.saveStyleSku(styleSku));
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateStyleSku(
        @RequestBody ErpStyleSku styleSku
    ) {
        return CommonResult.success(erpService.saveStyleSku(styleSku));
    }

    @DeleteMapping("/delete")
    public CommonResult<Boolean> deleteStyleSku(
        @RequestBody ErpStyleSku styleSku
    ) {
        return CommonResult.success(erpService.saveStyleSku(styleSku));
    }


}