package cn.iocoder.yudao.module.promotion.controller.admin.bargain;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainProductDO;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static cn.hutool.core.collection.CollectionUtil.newArrayList;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 砍价活动")
@RestController
@RequestMapping("/promotion/bargain-activity")
@Validated
public class BargainActivityController {

    @Resource
    private BargainActivityService activityService;

    @Resource
    private ProductSpuApi spuApi;

    @PostMapping("/create")
    @Operation(summary = "创建砍价活动")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:create')")
    public CommonResult<Long> createBargainActivity(@Valid @RequestBody BargainActivityCreateReqVO createReqVO) {
        return success(activityService.createBargainActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新砍价活动")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:update')")
    public CommonResult<Boolean> updateBargainActivity(@Valid @RequestBody BargainActivityUpdateReqVO updateReqVO) {
        activityService.updateBargainActivity(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除砍价活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:delete')")
    public CommonResult<Boolean> deleteBargainActivity(@RequestParam("id") Long id) {
        activityService.deleteBargainActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得砍价活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:query')")
    public CommonResult<BargainActivityRespVO> getBargainActivity(@RequestParam("id") Long id) {
        BargainActivityDO activity = activityService.getBargainActivity(id);
        List<BargainProductDO> products = activityService.getBargainProductsByActivityIds(newArrayList(id));
        return success(BargainActivityConvert.INSTANCE.convert(activity, products));
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:query')")
    public CommonResult<PageResult<BargainActivityRespVO>> getBargainActivityPage(
            @Valid BargainActivityPageReqVO pageVO) {
        // 查询砍价活动
        PageResult<BargainActivityDO> pageResult = activityService.getBargainActivityPage(pageVO);
        // 拼接数据
        Set<Long> activityIds = convertSet(pageResult.getList(), BargainActivityDO::getId);
        Set<Long> spuIds = convertSet(pageResult.getList(), BargainActivityDO::getSpuId);
        return success(BargainActivityConvert.INSTANCE.convertPage(pageResult,
                activityService.getBargainProductsByActivityIds(activityIds),
                spuApi.getSpuList(spuIds)));
    }

}
