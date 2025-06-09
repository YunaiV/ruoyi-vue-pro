package com.somle.eccang.controller;

import com.somle.eccang.model.EccangInventoryBatchLogVO;
import com.somle.eccang.model.EccangOrder;
import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.model.EccangResponse.EccangPage;
import com.somle.eccang.service.EccangService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "eccang-controller")
@RestController
@RequestMapping("/api/eccang")
public class EccangController {
    @Autowired
    EccangService eccangService;

    @GetMapping("/getInventory")
    public List<EccangPage> getInventory(
    ) {
        return eccangService.getInventory().toList();
    }

    @GetMapping("/getInventoryBatchLog")
    public List<EccangPage> getInventoryBatchLog(
        @RequestParam String startTime,
        @RequestParam String endTime
    ) {
        EccangInventoryBatchLogVO vo = new EccangInventoryBatchLogVO();
        vo.setDateFrom(LocalDateTime.parse(startTime));
        vo.setDateTo(LocalDateTime.parse(endTime));
        return eccangService.getInventoryBatchLog(vo).toList();
    }

    @GetMapping("/getOrderShip")
    public List<EccangOrder> getOrderShip(
        @RequestParam String startTime,
        @RequestParam String endTime
    ) {
        var vo = EccangOrderVO.builder()
            .condition(EccangOrderVO.Condition.builder()
                .platformShipDateStart(LocalDateTime.parse(startTime))
                .platformShipDateEnd(LocalDateTime.parse(endTime))
                .build())
            .build();
        return eccangService.getOrderUnarchive(vo).toList();
    }

    @PostMapping("/getOrder")
    public List<EccangPage> getOrder(
        @RequestBody EccangOrderVO order
    ) {
        return eccangService.getOrderUnarchivePages(order).toList();
    }

    @GetMapping("/getProducts")
    public List<EccangProduct> getProducts(
    ) {
        return eccangService.getProducts().toList();
    }


    @GetMapping("/list")
    public EccangPage list(
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
    public EccangPage post(
        @RequestParam String endpoint,
        @RequestBody Object payload
    ) {
        return eccangService.post(endpoint, payload);
    }

}