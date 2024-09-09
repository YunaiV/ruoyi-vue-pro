package cn.iocoder.yudao.module.promotion.controller.app.seckill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityNowRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityRespVO;
import cn.iocoder.yudao.module.promotion.convert.seckill.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityService;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillConfigService;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.findFirst;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isBetween;

@Tag(name = "用户 App - 秒杀活动")
@RestController
@RequestMapping("/promotion/seckill-activity")
@Validated
public class AppSeckillActivityController {

    /**
     * {@link AppSeckillActivityNowRespVO} 缓存，通过它异步刷新 {@link #getNowSeckillActivity()} 所要的首页数据
     */
    private final LoadingCache<String, AppSeckillActivityNowRespVO> nowSeckillActivityCache = buildAsyncReloadingCache(Duration.ofSeconds(10L),
            new CacheLoader<String, AppSeckillActivityNowRespVO>() {

                @Override
                public AppSeckillActivityNowRespVO load(String key) {
                     return getNowSeckillActivity0();
                }

            });

    @Resource
    private SeckillActivityService activityService;
    @Resource
    @Lazy
    private SeckillConfigService configService;

    @Resource
    private ProductSpuApi spuApi;

    @GetMapping("/get-now")
    @Operation(summary = "获得当前秒杀活动", description = "获取当前正在进行的活动，提供给首页使用")
    public CommonResult<AppSeckillActivityNowRespVO> getNowSeckillActivity() {
        return success(nowSeckillActivityCache.getUnchecked("")); // 缓存
    }

    private AppSeckillActivityNowRespVO getNowSeckillActivity0() {
        // 1. 获取当前时间处在哪个秒杀阶段
        SeckillConfigDO config = configService.getCurrentSeckillConfig();
        if (config == null) { // 时段不存在直接返回 null
            return new AppSeckillActivityNowRespVO();
        }

        // 2.1 查询满足当前阶段的活动
        List<SeckillActivityDO> activityList = activityService.getSeckillActivityListByConfigIdAndStatus(config.getId(), CommonStatusEnum.ENABLE.getStatus());
        List<SeckillProductDO> productList = activityService.getSeckillProductListByActivityIds(
                convertList(activityList, SeckillActivityDO::getId));
        // 2.2 获取 spu 信息
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(activityList, SeckillActivityDO::getSpuId));
        return SeckillActivityConvert.INSTANCE.convert(config, activityList, productList, spuList);
    }

    @GetMapping("/page")
    @Operation(summary = "获得秒杀活动分页")
    public CommonResult<PageResult<AppSeckillActivityRespVO>> getSeckillActivityPage(AppSeckillActivityPageReqVO pageReqVO) {
        // 1. 查询满足当前阶段的活动
        PageResult<SeckillActivityDO> pageResult = activityService.getSeckillActivityAppPageByConfigId(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        List<SeckillProductDO> productList = activityService.getSeckillProductListByActivityIds(
                convertList(pageResult.getList(), SeckillActivityDO::getId));

        // 2. 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(pageResult.getList(), SeckillActivityDO::getSpuId));
        return success(SeckillActivityConvert.INSTANCE.convertPage02(pageResult, productList, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得秒杀活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppSeckillActivityDetailRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        // 1. 获取活动
        SeckillActivityDO activity = activityService.getSeckillActivity(id);
        if (activity == null
                || ObjectUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            return success(null);
        }

        // 2. 获取时间段
        List<SeckillConfigDO> configs = configService.getSeckillConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        configs.removeIf(config -> !CollUtil.contains(activity.getConfigIds(), config.getId()));
        // 2.1 优先使用当前时间段
        SeckillConfigDO config = findFirst(configs, config0 -> isBetween(config0.getStartTime(), config0.getEndTime()));
        // 2.2 如果没有，则获取最后一个，因为倾向优先展示“未开始” > “已结束”
        if (config == null) {
            config = CollUtil.getLast(configs);
        }
        if (config == null) {
            return null;
        }
        // 3. 计算开始时间、结束时间
        LocalDate nowDate;
        // 3.1 如果在活动日期范围内，则以今天为 nowDate
        if (LocalDateTimeUtils.isBetween(activity.getStartTime(), activity.getEndTime())) {
            nowDate = LocalDate.now();
        } else {
            // 3.2 如果不在活动时间范围内，则直接以活动的 endTime 作为 nowDate，因为还是倾向优先展示“未开始” > “已结束”
            nowDate = activity.getEndTime().toLocalDate();
        }
        LocalDateTime startTime = LocalDateTime.of(nowDate, LocalTime.parse(config.getStartTime()));
        LocalDateTime endTime = LocalDateTime.of(nowDate, LocalTime.parse(config.getEndTime()));

        // 4. 拼接数据
        List<SeckillProductDO> productList = activityService.getSeckillProductListByActivityId(activity.getId());
        return success(SeckillActivityConvert.INSTANCE.convert3(activity, productList, startTime, endTime));
    }

}
