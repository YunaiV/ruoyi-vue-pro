package com.somle.rakuten.controller;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.rakuten.model.vo.OrderRequestVO;
import com.somle.rakuten.service.RakutenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rakuten")
@RequiredArgsConstructor
public class RakutenController {
    private final RakutenService service;

    @PostMapping("/orders")
    public Object getOrders(@RequestBody @Valid OrderRequestVO orderRequestVO) {
        return service.client.getOrders(orderRequestVO);
    }
}
