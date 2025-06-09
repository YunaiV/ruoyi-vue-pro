/*
package cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.log.TmsVesselTrackingLogDO;
import cn.iocoder.yudao.module.tms.service.vessel.tracking.TmsVesselTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 出运跟踪信息表（由外部API更新）")
@RestController
@RequestMapping("/tms/vessel-tracking")
@Validated
public class TmsVesselTrackingController {

    @Resource
    private TmsVesselTrackingService vesselTrackingService;

    @PostMapping("/create")
    @Operation(summary = "创建出运跟踪信息表（由外部API更新）")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:create')")
    public CommonResult<Long> createVesselTracking(@Valid @RequestBody TmsVesselTrackingSaveReqVO createReqVO) {
        return success(vesselTrackingService.createVesselTracking(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出运跟踪信息表（由外部API更新）")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:update')")
    public CommonResult<Boolean> updateVesselTracking(@Valid @RequestBody TmsVesselTrackingSaveReqVO updateReqVO) {
        vesselTrackingService.updateVesselTracking(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出运跟踪信息表（由外部API更新）")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:delete')")
    public CommonResult<Boolean> deleteVesselTracking(@RequestParam("id") Long id) {
        vesselTrackingService.deleteVesselTracking(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出运跟踪信息表（由外部API更新）")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:query')")
    public CommonResult<TmsVesselTrackingRespVO> getVesselTracking(@RequestParam("id") Long id) {
        TmsVesselTrackingDO vesselTracking = vesselTrackingService.getVesselTracking(id);
        return success(BeanUtils.toBean(vesselTracking, TmsVesselTrackingRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出运跟踪信息表（由外部API更新）分页")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:query')")
    public CommonResult<PageResult<TmsVesselTrackingRespVO>> getVesselTrackingPage(@Valid TmsVesselTrackingPageReqVO pageReqVO) {
        PageResult<TmsVesselTrackingDO> pageResult = vesselTrackingService.getVesselTrackingPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TmsVesselTrackingRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出运跟踪信息表（由外部API更新） Excel")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVesselTrackingExcel(@Valid TmsVesselTrackingPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TmsVesselTrackingDO> list = vesselTrackingService.getVesselTrackingPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出运跟踪信息表（由外部API更新）.xls", "数据", TmsVesselTrackingRespVO.class,
            BeanUtils.toBean(list, TmsVesselTrackingRespVO.class));
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入出运跟踪信息表（由外部API更新） Excel")
    @ApiAccessLog(operateType = IMPORT)
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:import')")
    public CommonResult<Boolean> importVesselTrackingExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TmsVesselTrackingSaveReqVO> list = ExcelUtils.read(file, TmsVesselTrackingSaveReqVO.class);
        // 可根据业务需要批量保存或校验
        return success(true);
    }

    // ==================== 子表（出运轨迹日志表（记录多次事件节点）） ====================

    @GetMapping("/vessel-tracking-log/page")
    @Operation(summary = "获得出运轨迹日志表（记录多次事件节点）分页")
    @Parameter(name = "trackingId", description = "关联跟踪主表ID")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:query')")
    public CommonResult<PageResult<TmsVesselTrackingLogDO>> getVesselTrackingLogPage(PageParam pageReqVO, @RequestParam("trackingId") Long trackingId) {
        return success(vesselTrackingService.getVesselTrackingLogPage(pageReqVO, trackingId));
    }

    @PostMapping("/vessel-tracking-log/create")
    @Operation(summary = "创建出运轨迹日志表（记录多次事件节点）")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:create')")
    public CommonResult<Long> createVesselTrackingLog(@Valid @RequestBody TmsVesselTrackingLogDO vesselTrackingLog) {
        return success(vesselTrackingService.createVesselTrackingLog(vesselTrackingLog));
    }

    @PutMapping("/vessel-tracking-log/update")
    @Operation(summary = "更新出运轨迹日志表（记录多次事件节点）")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:update')")
    public CommonResult<Boolean> updateVesselTrackingLog(@Valid @RequestBody TmsVesselTrackingLogDO vesselTrackingLog) {
        vesselTrackingService.updateVesselTrackingLog(vesselTrackingLog);
        return success(true);
    }

    @DeleteMapping("/vessel-tracking-log/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除出运轨迹日志表（记录多次事件节点）")
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:delete')")
    public CommonResult<Boolean> deleteVesselTrackingLog(@RequestParam("id") Long id) {
        vesselTrackingService.deleteVesselTrackingLog(id);
        return success(true);
    }

    @GetMapping("/vessel-tracking-log/get")
    @Operation(summary = "获得出运轨迹日志表（记录多次事件节点）")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:vessel-tracking:query')")
    public CommonResult<TmsVesselTrackingLogDO> getVesselTrackingLog(@RequestParam("id") Long id) {
        return success(vesselTrackingService.getVesselTrackingLog(id));
    }

}*/
