package cn.iocoder.yudao.module.promotion.controller.admin.point;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivitySaveReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointActivityDO;
import cn.iocoder.yudao.module.promotion.service.point.PointActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 积分商城活动")
@RestController
@RequestMapping("/promotion/point-activity")
@Validated
public class PointActivityController {

    @Resource
    private PointActivityService pointActivityService;

    @PostMapping("/create")
    @Operation(summary = "创建积分商城活动")
    @PreAuthorize("@ss.hasPermission('promotion:point-activity:create')")
    public CommonResult<Long> createPointActivity(@Valid @RequestBody PointActivitySaveReqVO createReqVO) {
        return success(pointActivityService.createPointActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新积分商城活动")
    @PreAuthorize("@ss.hasPermission('promotion:point-activity:update')")
    public CommonResult<Boolean> updatePointActivity(@Valid @RequestBody PointActivitySaveReqVO updateReqVO) {
        pointActivityService.updatePointActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭积分商城活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:point-activity:close')")
    public CommonResult<Boolean> closeSeckillActivity(@RequestParam("id") Long id) {
        pointActivityService.closePointActivity(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除积分商城活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:point-activity:delete')")
    public CommonResult<Boolean> deletePointActivity(@RequestParam("id") Long id) {
        pointActivityService.deletePointActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得积分商城活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:point-activity:query')")
    public CommonResult<PointActivityRespVO> getPointActivity(@RequestParam("id") Long id) {
        PointActivityDO pointActivity = pointActivityService.getPointActivity(id);
        return success(BeanUtils.toBean(pointActivity, PointActivityRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得积分商城活动分页")
    @PreAuthorize("@ss.hasPermission('promotion:point-activity:query')")
    public CommonResult<PageResult<PointActivityRespVO>> getPointActivityPage(@Valid PointActivityPageReqVO pageReqVO) {
        PageResult<PointActivityDO> pageResult = pointActivityService.getPointActivityPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PointActivityRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出积分商城活动 Excel")
    @PreAuthorize("@ss.hasPermission('promotion:point-activity:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPointActivityExcel(@Valid PointActivityPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PointActivityDO> list = pointActivityService.getPointActivityPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "积分商城活动.xls", "数据", PointActivityRespVO.class,
                BeanUtils.toBean(list, PointActivityRespVO.class));
    }

}