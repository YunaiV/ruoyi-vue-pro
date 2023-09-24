package cn.iocoder.yudao.module.trade.controller.app.config;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.config.vo.AppTradeConfigRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 交易配置")
@RestController
@RequestMapping("/trade/config")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AppTradeConfigController {
    @Resource
    private TradeConfigService tradeConfigService;

    @GetMapping("/get")
    public CommonResult<AppTradeConfigRespVO> getTradeConfig() {
        TradeConfigDO tradeConfig = ObjUtil.defaultIfNull(tradeConfigService.getTradeConfig(), new TradeConfigDO());

        AppTradeConfigRespVO respVO = new AppTradeConfigRespVO()
                .setBrokeragePosterUrls(tradeConfig.getBrokeragePostUrls())
                .setBrokerageFrozenDays(tradeConfig.getBrokerageFrozenDays())
                .setBrokerageWithdrawMinPrice(tradeConfig.getBrokerageWithdrawMinPrice())
                .setBrokerageWithdrawType(tradeConfig.getBrokerageWithdrawType());
        return success(respVO);
    }

}
