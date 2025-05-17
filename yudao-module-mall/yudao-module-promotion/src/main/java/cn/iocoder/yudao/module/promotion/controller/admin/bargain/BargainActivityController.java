package cn.iocoder.yudao.module.promotion.controller.admin.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.*;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.enums.bargain.BargainRecordStatusEnum;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainActivityService;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainHelpService;
import cn.iocoder.yudao.module.promotion.service.bargain.BargainRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 砍价活动")
@RestController
@RequestMapping("/promotion/bargain-activity")
@Validated
public class BargainActivityController {

    @Resource
    private BargainActivityService bargainActivityService;
    @Resource
    private BargainRecordService bargainRecordService;
    @Resource
    private BargainHelpService bargainHelpService;

    @Resource
    private ProductSpuApi spuApi;

    @PostMapping("/create")
    @Operation(summary = "创建砍价活动")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:create')")
    public CommonResult<Long> createBargainActivity(@Valid @RequestBody BargainActivityCreateReqVO createReqVO) {
        return success(bargainActivityService.createBargainActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新砍价活动")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:update')")
    public CommonResult<Boolean> updateBargainActivity(@Valid @RequestBody BargainActivityUpdateReqVO updateReqVO) {
        bargainActivityService.updateBargainActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭砍价活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:close')")
    public CommonResult<Boolean> closeSeckillActivity(@RequestParam("id") Long id) {
        bargainActivityService.closeBargainActivityById(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除砍价活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:delete')")
    public CommonResult<Boolean> deleteBargainActivity(@RequestParam("id") Long id) {
        bargainActivityService.deleteBargainActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得砍价活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:query')")
    public CommonResult<BargainActivityRespVO> getBargainActivity(@RequestParam("id") Long id) {
        return success(BargainActivityConvert.INSTANCE.convert(bargainActivityService.getBargainActivity(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-activity:query')")
    public CommonResult<PageResult<BargainActivityPageItemRespVO>> getBargainActivityPage(
            @Valid BargainActivityPageReqVO pageVO) {
        // 查询砍价活动
        PageResult<BargainActivityDO> pageResult = bargainActivityService.getBargainActivityPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(pageResult.getList(), BargainActivityDO::getSpuId));
        // 统计数据
        Collection<Long> activityIds = convertList(pageResult.getList(), BargainActivityDO::getId);
        Map<Long, Integer> recordUserCountMap = bargainRecordService.getBargainRecordUserCountMap(activityIds, null);
        Map<Long, Integer> recordSuccessUserCountMap = bargainRecordService.getBargainRecordUserCountMap(activityIds,
                BargainRecordStatusEnum.SUCCESS.getStatus());
        Map<Long, Integer> helpUserCountMap = bargainHelpService.getBargainHelpUserCountMapByActivity(activityIds);
        return success(BargainActivityConvert.INSTANCE.convertPage(pageResult, spuList,
                recordUserCountMap, recordSuccessUserCountMap, helpUserCountMap));
    }

}
