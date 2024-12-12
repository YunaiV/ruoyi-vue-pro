package com.somle.rakuten.controller;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.rakuten.model.vo.OrderRequestVO;
import com.somle.rakuten.model.vo.OrderSearchRequestVO;
import com.somle.rakuten.service.RakutenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rakuten")
@RequiredArgsConstructor
public class RakutenController {
    private final RakutenService service;

    /**
     * @param orderRequestVO 订单id+version（默认8）
     * @return JSONObject 订单信息
     */
    @PostMapping("/getOrder")
    public JSONObject getOrder(@RequestBody @Valid OrderRequestVO orderRequestVO) {
        return service.client.getOrder(orderRequestVO);
    }

    @PostMapping("/searchOrder")
    public JSONObject searchOrder(@RequestBody @Valid OrderSearchRequestVO vo) {

        return service.client.searchOrder(vo);
    }

    @PostMapping("/searchEndOrder")
    public JSONObject searchEndOrder(@RequestBody @Valid OrderSearchRequestVO vo) {
        return service.client.getEndOrderIds(vo);
    }
}
