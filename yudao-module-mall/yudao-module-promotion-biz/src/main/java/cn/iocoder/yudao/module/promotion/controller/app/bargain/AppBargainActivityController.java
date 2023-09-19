package cn.iocoder.yudao.module.promotion.controller.app.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.activity.AppBargainActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.activity.AppBargainActivityRespVO;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "用户 App - 砍价活动")
@RestController
@RequestMapping("/promotion/bargain-activity")
@Validated
public class AppBargainActivityController {
    @Resource
    private BargainActivityService bargainActivityService;
    @Resource
    private ProductSpuApi spuApi;

    @GetMapping("/page")
    @Operation(summary = "获得砍价活动分页")
    public CommonResult<PageResult<AppBargainActivityRespVO>> getBargainActivityPage(PageParam pageReqVO) {
        PageResult<BargainActivityDO> result = bargainActivityService.getBargainActivityPageForApp(pageReqVO);
        if (CollUtil.isEmpty(result.getList())) {
            return success(PageResult.empty(result.getTotal()));
        }
        // 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(result.getList(), BargainActivityDO::getSpuId));
        return success(BargainActivityConvert.INSTANCE.convertAppPage(result, spuList));
    }

    // TODO 芋艿：增加 Spring Cache
    @GetMapping("/list")
    @Operation(summary = "获得砍价活动列表", description = "用于小程序首页")
    @Parameter(name = "count", description = "需要展示的数量", example = "6")
    public CommonResult<List<AppBargainActivityRespVO>> getBargainActivityList(
            @RequestParam(name = "count", defaultValue = "6") Integer count) {
        List<BargainActivityDO> list = bargainActivityService.getBargainActivityListForApp(count);
        if (CollUtil.isEmpty(list)) {
            return success(BargainActivityConvert.INSTANCE.convertAppList(list));
        }
        // 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(list, BargainActivityDO::getSpuId));
        return success(BargainActivityConvert.INSTANCE.convertAppList(list, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得砍价活动详情")
    @Parameter(name = "id", description = "活动编号", example = "1")
    public CommonResult<AppBargainActivityDetailRespVO> getBargainActivityDetail(@RequestParam("id") Long id) {
        BargainActivityDO activity = bargainActivityService.getBargainActivity(id);
        if (activity == null) {
            return success(null);
        }
        // 拼接数据
        ProductSpuRespDTO spu = spuApi.getSpu(activity.getSpuId());
        return success(BargainActivityConvert.INSTANCE.convert(activity, spu));
    }

}
