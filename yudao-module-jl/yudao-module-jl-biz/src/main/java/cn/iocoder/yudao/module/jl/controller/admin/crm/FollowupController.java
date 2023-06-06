package cn.iocoder.yudao.module.jl.controller.admin.crm;

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

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Followup;
import cn.iocoder.yudao.module.jl.mapper.crm.FollowupMapper;
import cn.iocoder.yudao.module.jl.service.crm.FollowupService;

@Tag(name = "管理后台 - 销售跟进")
@RestController
@RequestMapping("/jl/followup")
@Validated
public class FollowupController {

    @Resource
    private FollowupService followupService;

    @Resource
    private FollowupMapper followupMapper;

    @PostMapping("/create")
    @Operation(summary = "创建销售跟进")
    @PreAuthorize("@ss.hasPermission('jl:followup:create')")
    public CommonResult<Long> createFollowup(@Valid @RequestBody FollowupCreateReqVO createReqVO) {
        return success(followupService.createFollowup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售跟进")
    @PreAuthorize("@ss.hasPermission('jl:followup:update')")
    public CommonResult<Boolean> updateFollowup(@Valid @RequestBody FollowupUpdateReqVO updateReqVO) {
        followupService.updateFollowup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除销售跟进")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:followup:delete')")
    public CommonResult<Boolean> deleteFollowup(@RequestParam("id") Long id) {
        followupService.deleteFollowup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得销售跟进")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:followup:query')")
    public CommonResult<FollowupRespVO> getFollowup(@RequestParam("id") Long id) {
            Optional<Followup> followup = followupService.getFollowup(id);
        return success(followup.map(followupMapper::toDto).orElseThrow(() -> exception(FOLLOWUP_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得销售跟进列表")
    @PreAuthorize("@ss.hasPermission('jl:followup:query')")
    public CommonResult<PageResult<FollowupRespVO>> getFollowupPage(@Valid FollowupPageReqVO pageVO, @Valid FollowupPageOrder orderV0) {
        PageResult<Followup> pageResult = followupService.getFollowupPage(pageVO, orderV0);
        return success(followupMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售跟进 Excel")
    @PreAuthorize("@ss.hasPermission('jl:followup:export')")
    @OperateLog(type = EXPORT)
    public void exportFollowupExcel(@Valid FollowupExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<Followup> list = followupService.getFollowupList(exportReqVO);
        // 导出 Excel
        List<FollowupExcelVO> excelData = followupMapper.toExcelList(list);
        ExcelUtils.write(response, "销售跟进.xls", "数据", FollowupExcelVO.class, excelData);
    }

}
