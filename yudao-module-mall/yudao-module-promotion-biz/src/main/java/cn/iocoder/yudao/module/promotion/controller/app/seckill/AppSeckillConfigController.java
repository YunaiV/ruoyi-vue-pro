package cn.iocoder.yudao.module.promotion.controller.app.seckill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.config.AppSeckillConfigRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 秒杀时间段")
@RestController
@RequestMapping("/promotion/seckill-config")
@Validated
public class AppSeckillConfigController {

    @GetMapping("/list")
    @Operation(summary = "获得秒杀时间段列表")
    public CommonResult<List<AppSeckillConfigRespVO>> getConfigList() {
        return success(Arrays.asList(
                new AppSeckillConfigRespVO().setId(1L).setStartTime("00:00").setEndTime("09:59"),
                new AppSeckillConfigRespVO().setId(2L).setStartTime("10:00").setEndTime("12:59"),
                new AppSeckillConfigRespVO().setId(2L).setStartTime("13:00").setEndTime("23:59")
        ));
    }

}
