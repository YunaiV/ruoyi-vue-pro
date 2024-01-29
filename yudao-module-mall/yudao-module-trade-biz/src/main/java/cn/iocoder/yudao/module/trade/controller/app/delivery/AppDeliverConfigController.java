package cn.iocoder.yudao.module.trade.controller.app.delivery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.delivery.vo.config.AppDeliveryConfigRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 配送配置")
@RestController
@RequestMapping("/trade/delivery/config")
@Validated
public class AppDeliverConfigController {

    // TODO @芋艿：这里后面干掉，合并到 AppTradeConfigController 中
    @GetMapping("/get")
    @Operation(summary = "获得配送配置")
    public CommonResult<AppDeliveryConfigRespVO> getDeliveryConfig() {
        return success(new AppDeliveryConfigRespVO().setPickUpEnable(true).setTencentLbsKey("123456"));
    }

}
