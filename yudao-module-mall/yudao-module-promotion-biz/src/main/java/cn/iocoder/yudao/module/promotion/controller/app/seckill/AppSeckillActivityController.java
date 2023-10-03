package cn.iocoder.yudao.module.promotion.controller.app.seckill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityNowRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityRespVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityService;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "用户 App - 秒杀活动")
@RestController
@RequestMapping("/promotion/seckill-activity")
@Validated
public class AppSeckillActivityController {
    @Resource
    private SeckillActivityService activityService;
    @Resource
    @Lazy
    private SeckillConfigService configService;

    @Resource
    private ProductSpuApi spuApi;

    // TODO 芋艿：需要增加 spring cache
    @GetMapping("/get-now")
    @Operation(summary = "获得当前秒杀活动", description = "获取当前正在进行的活动，提供给首页使用")
    public CommonResult<AppSeckillActivityNowRespVO> getNowSeckillActivity() {
        // 1. 获取当前时间处在哪个秒杀阶段
        SeckillConfigDO configList = configService.getSeckillConfigListByStatusOnCurrentTime(CommonStatusEnum.ENABLE.getStatus());
        if (configList == null) { // 时段不存在直接返回 null
            return success(null);
        }

        // 2. 查询满足当前阶段的活动
        List<SeckillActivityDO> activityList = activityService.getSeckillActivityListByConfigIdAndStatus(configList.getId(), CommonStatusEnum.ENABLE.getStatus());
        // 3 获取 spu 信息
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(activityList, SeckillActivityDO::getSpuId));
        return success(SeckillActivityConvert.INSTANCE.convert(configList, activityList, spuList));
    }

    @GetMapping("/page")
    @Operation(summary = "获得秒杀活动分页")
    public CommonResult<PageResult<AppSeckillActivityRespVO>> getSeckillActivityPage(AppSeckillActivityPageReqVO pageReqVO) {
        // 1. 查询满足当前阶段的活动
        PageResult<SeckillActivityDO> pageResult = activityService.getSeckillActivityAppPageByConfigId(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2. 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(pageResult.getList(), SeckillActivityDO::getSpuId));
        return success(SeckillActivityConvert.INSTANCE.convertPage(pageResult, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得秒杀活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppSeckillActivityDetailRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        // 1. 获取当前时间处在哪个秒杀阶段
        SeckillConfigDO configList = configService.getSeckillConfigListByStatusOnCurrentTime(CommonStatusEnum.ENABLE.getStatus());
        if (configList == null) { // 时段不存在直接返回 null
            return success(null);
        }

        // 2. 获取活动
        SeckillActivityDO seckillActivity = activityService.getSeckillActivity(id);
        if (seckillActivity == null
                || ObjectUtil.equal(seckillActivity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            return success(null);
        }

        // 3. 拼接数据
        List<SeckillProductDO> productList = activityService.getSeckillProductListByActivityId(seckillActivity.getId());
        return success(SeckillActivityConvert.INSTANCE.convert3(seckillActivity, productList, configList));
    }

}
