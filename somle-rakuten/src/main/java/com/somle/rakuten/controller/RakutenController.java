package com.somle.rakuten.controller;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.rakuten.model.vo.RakutenOrderReqVO;
import com.somle.rakuten.model.vo.RakutenOrderSearchReqVO;
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
     * @param rakutenOrderReqVO 订单id+version（默认8）
     * @return JSONObject 订单信息
     */
    @PostMapping("/getOrder")
    public JSONObject getOrder(@RequestBody @Valid RakutenOrderReqVO rakutenOrderReqVO) {
        return service.client.getOrder(rakutenOrderReqVO);
    }

    @PostMapping("/searchOrder")
    public JSONObject searchOrder(@RequestBody @Valid RakutenOrderSearchReqVO vo) {
        return service.client.searchOrder(vo);
    }

    @PostMapping("/searchEndOrder")
    public JSONObject searchEndOrder(@RequestBody @Valid RakutenOrderSearchReqVO vo) {
        return service.client.getEndOrderIds(vo);
    }
}
