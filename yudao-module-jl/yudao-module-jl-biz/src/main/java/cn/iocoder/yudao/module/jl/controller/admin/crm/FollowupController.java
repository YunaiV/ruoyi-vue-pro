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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.FollowupDO;
import cn.iocoder.yudao.module.jl.convert.crm.FollowupConvert;
import cn.iocoder.yudao.module.jl.service.crm.FollowupService;

@Tag(name = "管理后台 - 销售线索跟进，可以是跟进客户，也可以是跟进线索")
@RestController
@RequestMapping("/jl/followup")
@Validated
public class FollowupController {

    @Resource
    private FollowupService followupService;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索跟进，可以是跟进客户，也可以是跟进线索")
    @PreAuthorize("@ss.hasPermission('jl:followup:create')")
    public CommonResult<Long> createFollowup(@Valid @RequestBody FollowupCreateReqVO createReqVO) {
        return success(followupService.createFollowup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索跟进，可以是跟进客户，也可以是跟进线索")
    @PreAuthorize("@ss.hasPermission('jl:followup:update')")
    public CommonResult<Boolean> updateFollowup(@Valid @RequestBody FollowupUpdateReqVO updateReqVO) {
        followupService.updateFollowup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售线索跟进，可以是跟进客户，也可以是跟进线索")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:followup:delete')")
    public CommonResult<Boolean> deleteFollowup(@RequestParam("id") Long id) {
        followupService.deleteFollowup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售线索跟进，可以是跟进客户，也可以是跟进线索")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:followup:query')")
    public CommonResult<FollowupRespVO> getFollowup(@RequestParam("id") Long id) {
        FollowupDO followup = followupService.getFollowup(id);
        return success(FollowupConvert.INSTANCE.convert(followup));
    }

    @GetMapping("/list")
    @Operation(summary = "获得销售线索跟进，可以是跟进客户，也可以是跟进线索列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:followup:query')")
    public CommonResult<List<FollowupRespVO>> getFollowupList(@RequestParam("ids") Collection<Long> ids) {
        List<FollowupDO> list = followupService.getFollowupList(ids);
        return success(FollowupConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售线索跟进，可以是跟进客户，也可以是跟进线索分页")
    @PreAuthorize("@ss.hasPermission('jl:followup:query')")
    public CommonResult<PageResult<FollowupRespVO>> getFollowupPage(@Valid FollowupPageReqVO pageVO) {
        PageResult<FollowupDO> pageResult = followupService.getFollowupPage(pageVO);
        return success(FollowupConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索跟进，可以是跟进客户，也可以是跟进线索 Excel")
    @PreAuthorize("@ss.hasPermission('jl:followup:export')")
    @OperateLog(type = EXPORT)
    public void exportFollowupExcel(@Valid FollowupExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<FollowupDO> list = followupService.getFollowupList(exportReqVO);
        // 导出 Excel
        List<FollowupExcelVO> datas = FollowupConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "销售线索跟进，可以是跟进客户，也可以是跟进线索.xls", "数据", FollowupExcelVO.class, datas);
    }

}
