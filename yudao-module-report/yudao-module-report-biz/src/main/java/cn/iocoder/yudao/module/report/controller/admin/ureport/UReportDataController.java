package cn.iocoder.yudao.module.report.controller.admin.ureport;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.*;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;
import cn.iocoder.yudao.module.report.service.ureport.UReportDataService;

@Tag(name = "管理后台 - Ureport2报表")
@RestController
@RequestMapping("/report/ureport-data")
@Validated
public class UReportDataController {

    @Resource
    private UReportDataService uReportDataService;

    @PostMapping("/create")
    @Operation(summary = "创建Ureport2报表")
    @PreAuthorize("@ss.hasPermission('report:ureport-data:create')")
    public CommonResult<Long> createUReportData(@Valid @RequestBody UReportDataSaveReqVO createReqVO) {
        return success(uReportDataService.createUReportData(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Ureport2报表")
    @PreAuthorize("@ss.hasPermission('report:ureport-data:update')")
    public CommonResult<Boolean> updateUReportData(@Valid @RequestBody UReportDataSaveReqVO updateReqVO) {
        uReportDataService.updateUReportData(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Ureport2报表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('report:ureport-data:delete')")
    public CommonResult<Boolean> deleteUReportData(@RequestParam("id") Long id) {
        uReportDataService.deleteUReportData(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Ureport2报表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('report:ureport-data:query')")
    public CommonResult<UReportDataRespVO> getUReportData(@RequestParam("id") Long id) {
        UReportDataDO uReportData = uReportDataService.getUReportData(id);
        return success(BeanUtils.toBean(uReportData, UReportDataRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Ureport2报表分页")
    @PreAuthorize("@ss.hasPermission('report:ureport-data:query')")
    public CommonResult<PageResult<UReportDataRespVO>> getUReportDataPage(@Valid UReportDataPageReqVO pageReqVO) {
        PageResult<UReportDataDO> pageResult = uReportDataService.getUReportDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UReportDataRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Ureport2报表 Excel")
    @PreAuthorize("@ss.hasPermission('report:ureport-data:export')")
    @OperateLog(type = EXPORT)
    public void exportUReportDataExcel(@Valid UReportDataPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UReportDataDO> list = uReportDataService.getUReportDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Ureport2报表.xls", "数据", UReportDataRespVO.class,
                        BeanUtils.toBean(list, UReportDataRespVO.class));
    }

}
