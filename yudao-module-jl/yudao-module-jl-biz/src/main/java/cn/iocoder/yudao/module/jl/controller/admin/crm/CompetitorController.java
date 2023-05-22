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
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CompetitorDO;
import cn.iocoder.yudao.module.jl.convert.crm.CompetitorConvert;
import cn.iocoder.yudao.module.jl.service.crm.CompetitorService;

@Tag(name = "管理后台 - 友商")
@RestController
@RequestMapping("/jl/competitor")
@Validated
public class CompetitorController {

    @Resource
    private CompetitorService competitorService;

    @PostMapping("/create")
    @Operation(summary = "创建友商")
    @PreAuthorize("@ss.hasPermission('jl:competitor:create')")
    public CommonResult<Long> createCompetitor(@Valid @RequestBody CompetitorCreateReqVO createReqVO) {
        return success(competitorService.createCompetitor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新友商")
    @PreAuthorize("@ss.hasPermission('jl:competitor:update')")
    public CommonResult<Boolean> updateCompetitor(@Valid @RequestBody CompetitorUpdateReqVO updateReqVO) {
        competitorService.updateCompetitor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除友商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:competitor:delete')")
    public CommonResult<Boolean> deleteCompetitor(@RequestParam("id") Long id) {
        competitorService.deleteCompetitor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得友商")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:competitor:query')")
    public CommonResult<CompetitorRespVO> getCompetitor(@RequestParam("id") Long id) {
        CompetitorDO competitor = competitorService.getCompetitor(id);
        return success(CompetitorConvert.INSTANCE.convert(competitor));
    }

    @GetMapping("/list")
    @Operation(summary = "获得友商列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:competitor:query')")
    public CommonResult<List<CompetitorRespVO>> getCompetitorList(@RequestParam("ids") Collection<Long> ids) {
        List<CompetitorDO> list = competitorService.getCompetitorList(ids);
        return success(CompetitorConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得友商分页")
    @PreAuthorize("@ss.hasPermission('jl:competitor:query')")
    public CommonResult<PageResult<CompetitorRespVO>> getCompetitorPage(@Valid CompetitorPageReqVO pageVO) {
        PageResult<CompetitorDO> pageResult = competitorService.getCompetitorPage(pageVO);
        return success(CompetitorConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出友商 Excel")
    @PreAuthorize("@ss.hasPermission('jl:competitor:export')")
    @OperateLog(type = EXPORT)
    public void exportCompetitorExcel(@Valid CompetitorExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<CompetitorDO> list = competitorService.getCompetitorList(exportReqVO);
        // 导出 Excel
        List<CompetitorExcelVO> datas = CompetitorConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "友商.xls", "数据", CompetitorExcelVO.class, datas);
    }

}
