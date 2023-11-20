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
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import cn.iocoder.yudao.module.report.service.ureport.UreportFileService;

@Tag(name = "管理后台 - Ureport2报表")
@RestController
@RequestMapping("/report/ureport-file")
@Validated
public class UreportFileController {

    @Resource
    private UreportFileService ureportFileService;

    @PostMapping("/create")
    @Operation(summary = "创建Ureport2报表")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:create')")
    public CommonResult<Long> createUreportFile(@Valid @RequestBody UreportFileSaveReqVO createReqVO) {
        return success(ureportFileService.createUreportFile(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Ureport2报表")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:update')")
    public CommonResult<Boolean> updateUreportFile(@Valid @RequestBody UreportFileSaveReqVO updateReqVO) {
        ureportFileService.updateUreportFile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Ureport2报表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('report:ureport-file:delete')")
    public CommonResult<Boolean> deleteUreportFile(@RequestParam("id") Long id) {
        ureportFileService.deleteUreportFile(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Ureport2报表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:query')")
    public CommonResult<UreportFileRespVO> getUreportFile(@RequestParam("id") Long id) {
        UreportFileDO ureportFile = ureportFileService.getUreportFile(id);
        return success(BeanUtils.toBean(ureportFile, UreportFileRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Ureport2报表分页")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:query')")
    public CommonResult<PageResult<UreportFileRespVO>> getUreportFilePage(@Valid UreportFilePageReqVO pageReqVO) {
        PageResult<UreportFileDO> pageResult = ureportFileService.getUreportFilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UreportFileRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Ureport2报表 Excel")
    @PreAuthorize("@ss.hasPermission('report:ureport-file:export')")
    @OperateLog(type = EXPORT)
    public void exportUreportFileExcel(@Valid UreportFilePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UreportFileDO> list = ureportFileService.getUreportFilePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Ureport2报表.xls", "数据", UreportFileRespVO.class,
                        BeanUtils.toBean(list, UreportFileRespVO.class));
    }

}