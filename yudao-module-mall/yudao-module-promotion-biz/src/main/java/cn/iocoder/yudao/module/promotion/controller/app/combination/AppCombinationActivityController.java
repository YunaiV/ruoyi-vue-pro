package cn.iocoder.yudao.module.promotion.controller.app.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.activity.AppCombinationActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.activity.AppCombinationActivityRespVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-activity")
@Validated
public class AppCombinationActivityController {

    /**
     * {@link AppCombinationActivityRespVO} 缓存，通过它异步刷新 {@link #getCombinationActivityList0(Integer)} 所要的首页数据
     */
    private final LoadingCache<Integer, List<AppCombinationActivityRespVO>> combinationActivityListCache = buildAsyncReloadingCache(Duration.ofSeconds(10L),
            new CacheLoader<Integer, List<AppCombinationActivityRespVO>>() {

                @Override
                public List<AppCombinationActivityRespVO> load(Integer count) {
                    return getCombinationActivityList0(count);
                }

            });

    @Resource
    private CombinationActivityService activityService;

    @Resource
    private ProductSpuApi spuApi;

    @GetMapping("/list")
    @Operation(summary = "获得拼团活动列表", description = "用于小程序首页")
    @Parameter(name = "count", description = "需要展示的数量", example = "6")
    public CommonResult<List<AppCombinationActivityRespVO>> getCombinationActivityList(
            @RequestParam(name = "count", defaultValue = "6") Integer count) {
        return success(combinationActivityListCache.getUnchecked(count));
    }

    private List<AppCombinationActivityRespVO> getCombinationActivityList0(Integer count) {
        List<CombinationActivityDO> activityList = activityService.getCombinationActivityListByCount(count);
        if (CollUtil.isEmpty(activityList)) {
            return Collections.emptyList();
        }
        // 拼接返回
        List<CombinationProductDO> productList = activityService.getCombinationProductListByActivityIds(
                convertList(activityList, CombinationActivityDO::getId));
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(activityList, CombinationActivityDO::getSpuId));
        return CombinationActivityConvert.INSTANCE.convertAppList(activityList, productList, spuList);
    }

    @GetMapping("/page")
    @Operation(summary = "获得拼团活动分页")
    public CommonResult<PageResult<AppCombinationActivityRespVO>> getCombinationActivityPage(PageParam pageParam) {
        PageResult<CombinationActivityDO> pageResult = activityService.getCombinationActivityPage(pageParam);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 拼接返回
        List<CombinationProductDO> productList = activityService.getCombinationProductListByActivityIds(
                convertList(pageResult.getList(), CombinationActivityDO::getId));
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(pageResult.getList(), CombinationActivityDO::getSpuId));
        return success(CombinationActivityConvert.INSTANCE.convertAppPage(pageResult, productList, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppCombinationActivityDetailRespVO> getCombinationActivityDetail(@RequestParam("id") Long id) {
        // 1. 获取活动
        CombinationActivityDO activity = activityService.getCombinationActivity(id);
        if (activity == null
                || ObjectUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            return success(null);
        }

        // 2. 获取活动商品
        List<CombinationProductDO> products = activityService.getCombinationProductsByActivityId(activity.getId());
        return success(CombinationActivityConvert.INSTANCE.convert3(activity, products));
    }

    @GetMapping("/detail-list")
    @Operation(summary = "获得拼团活动明细")
    @Parameter(name = "ids", description = "活动编号列表", required = true, example = "[1024, 1025]")
    public CommonResult<List<AppCombinationActivityDetailRespVO>> getCombinationActivityDetailList(@RequestParam("ids") Collection<Long> ids) {
        // 1. 获取活动
        List<CombinationActivityDO> combinationActivityDOList = activityService.getCombinationActivityListByIds(ids);

        // 过滤掉无效的活动
        List<CombinationActivityDO> validActivities = combinationActivityDOList.stream()
                .filter(combinationActivityDO -> combinationActivityDO != null &&
                        !ObjectUtil.equal(combinationActivityDO.getStatus(), CommonStatusEnum.DISABLE.getStatus()))
                .toList();

        // 如果没有有效的活动，返回空列表
        if (validActivities.isEmpty()) {
            return success(ListUtil.empty());
        }

        // 2. 构建结果列表
        List<AppCombinationActivityDetailRespVO> detailRespVOList = new ArrayList<>();
        for (CombinationActivityDO activity : validActivities) {
            // 获取活动商品
            List<CombinationProductDO> products = activityService.getCombinationProductsByActivityId(activity.getId());

            // 调用转换方法并添加到结果列表
            AppCombinationActivityDetailRespVO detailRespVO = CombinationActivityConvert.INSTANCE.convert3(activity, products);
            detailRespVOList.add(detailRespVO);
        }

        // 3. 返回转换后的结果
        return success(detailRespVOList);
    }


}
