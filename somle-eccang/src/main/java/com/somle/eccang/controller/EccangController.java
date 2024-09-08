package com.somle.eccang.controller;

import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.model.EccangResponse.BizContent;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/eccang")
public class EccangController {
    @Autowired 
    EccangService eccangService;

    @GetMapping("/getInventoryBatchLog")
    public List<BizContent> getInventoryBatchLog( 
        @RequestParam String startTime,
        @RequestParam String endTime
    ) {
        return eccangService.getInventoryBatchLog(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime)).toList();
    }

//    @GetMapping("/getOrderShip")
//    public List<BizContent> getOrderShip(
//            @RequestParam String startTime,
//            @RequestParam String endTime
//    ) {
//        return eccangService.getOrderShipPage(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime)).toList();
//    }

    @GetMapping("/getOrder")
    public List<BizContent> getOrder(
        @RequestParam EccangOrderVO order
    ) {
        return eccangService.getOrderPages(order).toList();
    }

    @GetMapping("/getProducts")
    public List<EccangProduct> getProducts(
    ) {
        return eccangService.getProducts().toList();
    }



    @GetMapping("/list")
    public BizContent list(
        @RequestParam String endpoint
    ) {
        return eccangService.list(endpoint);
    }

    // @GetMapping("/listPage")
    // @ResponseBody
    // public List<BizContent> listPage(
    //     @RequestParam String endpoint
    // ) {
    //     return eccangService.listPage(endpoint).toList();
    // }

    @GetMapping("/post")
    public BizContent post(
        @RequestParam String endpoint,
        @RequestBody Object payload
    ) {
        return eccangService.post(endpoint, payload);
    }

}