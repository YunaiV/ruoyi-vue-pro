package cn.iocoder.yudao.module.promotion.controller.app.seckill;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.config.AppSeckillConfigRespVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillconfig.SeckillConfigConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillconfig.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 秒杀时间段")
@RestController
@RequestMapping("/promotion/seckill-config")
@Validated
public class AppSeckillConfigController {
    @Resource
    private SeckillConfigService configService;

    @GetMapping("/list")
    @Operation(summary = "获得秒杀时间段列表")
    public CommonResult<List<AppSeckillConfigRespVO>> getSeckillConfigList() {
        List<SeckillConfigDO> list = configService.getSeckillConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollectionUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }

        return success(SeckillConfigConvert.INSTANCE.convertList2(list));
        //return success(Arrays.asList(
        //        new AppSeckillConfigRespVO().setId(1L).setStartTime("00:00").setEndTime("09:59")
        //                .setSliderPicUrls(Arrays.asList("https://static.iocoder.cn/mall/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg",
        //                        "https://static.iocoder.cn/mall/132.jpeg")),
        //        new AppSeckillConfigRespVO().setId(2L).setStartTime("10:00").setEndTime("12:59"),
        //        new AppSeckillConfigRespVO().setId(2L).setStartTime("13:00").setEndTime("22:59"),
        //        new AppSeckillConfigRespVO().setId(2L).setStartTime("23:00").setEndTime("23:59")
        //));
    }

}
