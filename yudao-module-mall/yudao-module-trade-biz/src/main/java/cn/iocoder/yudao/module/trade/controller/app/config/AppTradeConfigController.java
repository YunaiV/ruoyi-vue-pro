package cn.iocoder.yudao.module.trade.controller.app.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.config.vo.AppTradeConfigRespVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static java.util.Arrays.asList;

@Tag(name = "用户 App - 交易配置")
@RestController
@RequestMapping("/trade/config")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AppTradeConfigController {

    @GetMapping("/get")
    public CommonResult<AppTradeConfigRespVO> getTradeConfig() {
        AppTradeConfigRespVO respVO = new AppTradeConfigRespVO();
        respVO.setBrokeragePosterUrls(asList(
                "https://api.java.crmeb.net/crmebimage/product/2020/08/03/755bf516b1ca4b6db3bfeaa4dd5901cdh71kob20re.jpg",
                "https://api.java.crmeb.net/crmebimage/maintain/2021/03/01/406d729b84ed4ec9a2171bfcf6fd0634ughzbz9kfi.jpg",
                "https://api.java.crmeb.net/crmebimage/maintain/2021/03/01/efb1e4e7fe604fe1988b4213ce08cb11tdsyijtd2r.jpg"
        ));
        respVO.setBrokerageFrozenDays(10);
        respVO.setBrokerageWithdrawMinPrice(100);
        return success(respVO);
    }

}
