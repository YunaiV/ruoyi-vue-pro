package cn.iocoder.yudao.module.promotion.controller.app.combination;

import cn.hutool.core.collection.CollUtil;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COMBINATION_ACTIVITY_APP_STATUS_DISABLE;

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-activity")
@Validated
public class AppCombinationActivityController {

    @Resource
    private CombinationActivityService activityService;
    @Resource
    private ProductSpuApi spuApi;


    @GetMapping("/list")
    @Operation(summary = "获得拼团活动列表", description = "用于小程序首页")
    @Parameter(name = "count", description = "需要展示的数量", example = "6")
    public CommonResult<List<AppCombinationActivityRespVO>> getCombinationActivityList(
            @RequestParam(name = "count", defaultValue = "6") Integer count) {
        List<CombinationActivityDO> list = activityService.getCombinationActivityAppList(count);
        if (CollUtil.isEmpty(list)) {
            return success(CombinationActivityConvert.INSTANCE.convertAppList(list));
        }

        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(list, CombinationActivityDO::getSpuId));
        // TODO 芋艿：增加 Spring Cache
        return success(CombinationActivityConvert.INSTANCE.convertAppList(list, spuList));
    }

    @GetMapping("/page")
    @Operation(summary = "获得拼团活动分页")
    public CommonResult<PageResult<AppCombinationActivityRespVO>> getCombinationActivityPage(PageParam pageParam) {
        PageResult<CombinationActivityDO> result = activityService.getCombinationActivityAppPage(pageParam);
        if (CollUtil.isEmpty(result.getList())) {
            return success(PageResult.empty(result.getTotal()));
        }

        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(result.getList(), CombinationActivityDO::getSpuId));
        return success(CombinationActivityConvert.INSTANCE.convertAppPage(result, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppCombinationActivityDetailRespVO> getCombinationActivityDetail(@RequestParam("id") Long id) {
        // 1、获取活动
        CombinationActivityDO combinationActivity = activityService.getCombinationActivity(id);
        if (combinationActivity == null) {
            return success(null);
        }
        if (ObjectUtil.equal(combinationActivity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(COMBINATION_ACTIVITY_APP_STATUS_DISABLE);
        }

        // 2、获取活动商品
        List<CombinationProductDO> products = activityService.getCombinationProductsByActivityIds(Arrays.asList(combinationActivity.getId()));
        return success(CombinationActivityConvert.INSTANCE.convert3(combinationActivity, products));
    }

}
