package cn.iocoder.yudao.module.promotion.controller.admin.combination;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.*;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationActivityService;
import cn.iocoder.yudao.module.promotion.service.combination.CombinationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollectionUtil.newArrayList;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-activity")
@Validated
public class CombinationActivityController {

    @Resource
    private CombinationActivityService combinationActivityService;
    @Resource
    private CombinationRecordService combinationRecordService;

    @Resource
    private ProductSpuApi productSpuApi;

    @PostMapping("/create")
    @Operation(summary = "创建拼团活动")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:create')")
    public CommonResult<Long> createCombinationActivity(@Valid @RequestBody CombinationActivityCreateReqVO createReqVO) {
        return success(combinationActivityService.createCombinationActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新拼团活动")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:update')")
    public CommonResult<Boolean> updateCombinationActivity(@Valid @RequestBody CombinationActivityUpdateReqVO updateReqVO) {
        combinationActivityService.updateCombinationActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭拼团活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:close')")
    public CommonResult<Boolean> closeCombinationActivity(@RequestParam("id") Long id) {
        combinationActivityService.closeCombinationActivityById(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除拼团活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:delete')")
    public CommonResult<Boolean> deleteCombinationActivity(@RequestParam("id") Long id) {
        combinationActivityService.deleteCombinationActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得拼团活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:query')")
    public CommonResult<CombinationActivityRespVO> getCombinationActivity(@RequestParam("id") Long id) {
        CombinationActivityDO activity = combinationActivityService.getCombinationActivity(id);
        List<CombinationProductDO> products = combinationActivityService.getCombinationProductListByActivityIds(newArrayList(id));
        return success(CombinationActivityConvert.INSTANCE.convert(activity, products));
    }

    @GetMapping("/detail-list")
    @Operation(summary = "获得拼团活动详情列表")
    @Parameter(name = "ids", description = "拼团活动编号列表", required = true, example = "[1,2,3]")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<List<CombinationActivityRespVO>> getCombinationActivityDetailList(@RequestParam("ids") Collection<Long> ids) {
        // 查询拼团活动列表
        List<CombinationActivityDO> activities = combinationActivityService.getCombinationActivityListByIds(ids);

        // 转换活动列表
        List<CombinationActivityRespVO> activityVOs = CombinationActivityConvert.INSTANCE.convertList(activities);

        // 获取商品SPU列表和拼团产品列表
        Set<Long> spuIds = activities.stream().map(CombinationActivityDO::getSpuId).collect(Collectors.toSet());
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(spuIds);

        Set<Long> activityIds = activities.stream().map(CombinationActivityDO::getId).collect(Collectors.toSet());
        List<CombinationProductDO> productList = combinationActivityService.getCombinationProductListByActivityIds(activityIds);

        // 创建SPU和产品的映射
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<CombinationProductDO>> productMap = convertMultiMap(productList, CombinationProductDO::getActivityId);

        // 更新VO列表
        activityVOs.forEach(vo -> {
            ProductSpuRespDTO spu = spuMap.get(vo.getSpuId());
            if (spu != null) {
                vo.setSpuName(spu.getName())
                        .setPicUrl(spu.getPicUrl())
                        .setMarketPrice(spu.getMarketPrice());
            }
            vo.setProducts(CombinationActivityConvert.INSTANCE.convertList2(productMap.get(vo.getId())));
        });

        return success(activityVOs);
    }


    @GetMapping("/page")
    @Operation(summary = "获得拼团活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:combination-activity:query')")
    public CommonResult<PageResult<CombinationActivityPageItemRespVO>> getCombinationActivityPage(
            @Valid CombinationActivityPageReqVO pageVO) {
        // 查询拼团活动
        PageResult<CombinationActivityDO> pageResult = combinationActivityService.getCombinationActivityPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 统计数据
        Set<Long> activityIds = convertSet(pageResult.getList(), CombinationActivityDO::getId);
        Map<Long, Integer> groupCountMap = combinationRecordService.getCombinationRecordCountMapByActivity(
                activityIds, null, CombinationRecordDO.HEAD_ID_GROUP);
        Map<Long, Integer> groupSuccessCountMap = combinationRecordService.getCombinationRecordCountMapByActivity(
                activityIds, CombinationRecordStatusEnum.SUCCESS.getStatus(), CombinationRecordDO.HEAD_ID_GROUP);
        Map<Long, Integer> recordCountMap = combinationRecordService.getCombinationRecordCountMapByActivity(
                activityIds, null, null);
        // 拼接数据
        List<CombinationProductDO> products = combinationActivityService.getCombinationProductListByActivityIds(
                convertSet(pageResult.getList(), CombinationActivityDO::getId));
        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(
                convertSet(pageResult.getList(), CombinationActivityDO::getSpuId));
        return success(CombinationActivityConvert.INSTANCE.convertPage(pageResult, products,
                groupCountMap, groupSuccessCountMap, recordCountMap, spus));
    }

}
