package cn.iocoder.yudao.module.promotion.controller.app.point;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.point.vo.AppPointActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.point.vo.AppPointActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.point.vo.AppPointActivityRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointProductDO;
import cn.iocoder.yudao.module.promotion.service.point.PointActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "用户 App - 积分商城活动")
@RestController
@RequestMapping("/promotion/point-activity")
@Validated
public class AppPointActivityController {

    @Resource
    private PointActivityService pointActivityService;

    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/page")
    @Operation(summary = "获得积分商城活动分页")
    public CommonResult<PageResult<AppPointActivityRespVO>> getPointActivityPage(AppPointActivityPageReqVO pageReqVO) {
        // 1. 查询满足当前阶段的活动
        PageResult<PointActivityDO> pageResult = pointActivityService.getPointActivityPage(
                BeanUtils.toBean(pageReqVO, PointActivityPageReqVO.class));
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 2. 拼接数据
        List<AppPointActivityRespVO> resultList = buildAppPointActivityRespVOList(pageResult.getList());
        return success(new PageResult<>(resultList, pageResult.getTotal()));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得积分商城活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppPointActivityDetailRespVO> getPointActivity(@RequestParam("id") Long id) {
        // 1. 获取活动
        PointActivityDO activity = pointActivityService.getPointActivity(id);
        if (activity == null
                || ObjUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            return success(null);
        }

        // 2. 拼接数据
        List<PointProductDO> products = pointActivityService.getPointProductListByActivityIds(Collections.singletonList(id));
        AppPointActivityDetailRespVO respVO = BeanUtils.toBean(activity, AppPointActivityDetailRespVO.class);
        // 设置 product 信息
        respVO.setProducts(BeanUtils.toBean(products, AppPointActivityDetailRespVO.Product.class));
        PointProductDO minProduct = getMinPropertyObj(products, PointProductDO::getPoint);
        assert minProduct != null;
        respVO.setPoint(minProduct.getPoint()).setPrice(minProduct.getPrice());
        return success(respVO);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得积分商城活动列表，基于活动编号数组")
    @Parameter(name = "ids", description = "活动编号数组", required = true, example = "[1024, 1025]")
    public CommonResult<List<AppPointActivityRespVO>> getCombinationActivityListByIds(@RequestParam("ids") List<Long> ids) {
        // 1. 获得开启的活动列表
        List<PointActivityDO> activityList = pointActivityService.getPointActivityListByIds(ids);
        activityList.removeIf(activity -> CommonStatusEnum.isDisable(activity.getStatus()));
        if (CollUtil.isEmpty(activityList)) {
            return success(Collections.emptyList());
        }
        // 2. 拼接返回
        List<AppPointActivityRespVO> result = buildAppPointActivityRespVOList(activityList);
        return success(result);
    }

    private List<AppPointActivityRespVO> buildAppPointActivityRespVOList(List<PointActivityDO> activityList) {
        List<PointProductDO> products = pointActivityService.getPointProductListByActivityIds(
                convertSet(activityList, PointActivityDO::getId));
        Map<Long, List<PointProductDO>> productsMap = convertMultiMap(products, PointProductDO::getActivityId);
        Map<Long, ProductSpuRespDTO> spuMap = productSpuApi.getSpusMap(
                convertSet(activityList, PointActivityDO::getSpuId));
        List<AppPointActivityRespVO> result = BeanUtils.toBean(activityList, AppPointActivityRespVO.class);
        result.forEach(activity -> {
            // 设置 product 信息
            PointProductDO minProduct = getMinObject(productsMap.get(activity.getId()), PointProductDO::getPoint);
            assert minProduct != null;
            activity.setPoint(minProduct.getPoint()).setPrice(minProduct.getPrice());
            findAndThen(spuMap, activity.getSpuId(),
                    spu -> activity.setSpuName(spu.getName()).setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
        });
        return result;
    }

}
