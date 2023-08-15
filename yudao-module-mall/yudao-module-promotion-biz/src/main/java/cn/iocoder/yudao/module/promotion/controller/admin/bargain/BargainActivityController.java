package cn.iocoder.yudao.module.promotion.controller.admin.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 砍价活动")
@RestController
@RequestMapping("/promotion/bargain-activity")
@Validated
public class BargainActivityController {

    @Resource
    private BargainActivityService activityService;

    @Resource
    private ProductSpuApi productSpuApi;

    @PostMapping("/create")
    @Operation(summary = "创建砍价活动")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:create')")
    public CommonResult<Long> createBargainActivity(@Valid @RequestBody BargainActivityBaseVO createReqVO) {
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
        return success(BargainActivityConvert.INSTANCE.convert(activityService.getBargainActivity(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:query')")
    public CommonResult<PageResult<BargainActivityRespVO>> getBargainActivityPage(
            @Valid BargainActivityPageReqVO pageVO) {
        // 查询砍价活动
        PageResult<BargainActivityDO> pageResult = activityService.getBargainActivityPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        return success(BargainActivityConvert.INSTANCE.convertPage(activityService.getBargainActivityPage(pageVO)));
    }

}
