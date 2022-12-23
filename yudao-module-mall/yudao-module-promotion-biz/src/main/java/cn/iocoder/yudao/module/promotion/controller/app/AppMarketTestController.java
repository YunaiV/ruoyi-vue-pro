package cn.iocoder.yudao.module.promotion.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户 App - 营销")
@RestController
@RequestMapping("/market/test")
@Validated
public class AppMarketTestController {

    @GetMapping("/get")
    @ApiOperation("获取 market 信息")
    public CommonResult<String> get() {
        return success("true");
    }
}
