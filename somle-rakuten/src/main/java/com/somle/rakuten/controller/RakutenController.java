package com.somle.rakuten.controller;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.rakuten.model.reps.RakutenSearchOrderRepsVO;
import com.somle.rakuten.model.req.RakutenOrderReqVO;
import com.somle.rakuten.model.req.RakutenOrderSearchReqVO;
import com.somle.rakuten.service.RakutenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return service.rakutenClients.get(0).getOrder(rakutenOrderReqVO);
    }

    @PostMapping("/searchOrder")
    public RakutenSearchOrderRepsVO searchOrder(@RequestBody @Valid RakutenOrderSearchReqVO vo) {
        return service.rakutenClients.get(0).searchOrder(vo);
    }

    @PostMapping("/searchEndOrder")
    public JSONObject searchEndOrder(@RequestBody @Valid RakutenOrderSearchReqVO vo) {
        return service.rakutenClients.get(0).getEndOrderIds(vo);
    }
}
