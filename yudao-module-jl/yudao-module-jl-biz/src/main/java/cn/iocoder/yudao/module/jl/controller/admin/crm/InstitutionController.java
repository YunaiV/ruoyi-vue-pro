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
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;
import cn.iocoder.yudao.module.jl.convert.crm.InstitutionConvert;
import cn.iocoder.yudao.module.jl.service.crm.InstitutionService;

@Tag(name = "管理后台 - CRM 模块的机构/公司")
@RestController
@RequestMapping("/jl/institution")
@Validated
public class InstitutionController {

    @Resource
    private InstitutionService institutionService;

    @PostMapping("/create")
    @Operation(summary = "创建CRM 模块的机构/公司")
    @PreAuthorize("@ss.hasPermission('jl:institution:create')")
    public CommonResult<Long> createInstitution(@Valid @RequestBody InstitutionCreateReq createReqVO) {
        return success(institutionService.createInstitution(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新CRM 模块的机构/公司")
    @PreAuthorize("@ss.hasPermission('jl:institution:update')")
    public CommonResult<Boolean> updateInstitution(@Valid @RequestBody InstitutionDto updateReqVO) {
        institutionService.updateInstitution(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除CRM 模块的机构/公司")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:institution:delete')")
    public CommonResult<Boolean> deleteInstitution(@RequestParam("id") Long id) {
        institutionService.deleteInstitution(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得CRM 模块的机构/公司")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:institution:query')")
    public CommonResult<InstitutionDto> getInstitution(@RequestParam("id") Long id) {
        InstitutionDto institution = institutionService.getInstitution(id);
        return success(institution);
    }

    @GetMapping("/page")
    @Operation(summary = "获得CRM 模块的机构/公司分页")
    @PreAuthorize("@ss.hasPermission('jl:institution:query')")
    public CommonResult<PageResult<InstitutionRespVO>> getInstitutionPage(@Valid InstitutionPageReqVO pageVO) {
//        PageResult<InstitutionDO> pageResult = institutionService.getInstitutionPage(pageVO);
//        return success(InstitutionConvert.INSTANCE.convertPage(pageResult));
        return null;
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出CRM 模块的机构/公司 Excel")
    @PreAuthorize("@ss.hasPermission('jl:institution:export')")
    @OperateLog(type = EXPORT)
    public void exportInstitutionExcel(@Valid InstitutionExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
//        List<InstitutionDO> list = institutionService.getInstitutionList(exportReqVO);
//        // 导出 Excel
//        List<InstitutionExcelVO> datas = InstitutionConvert.INSTANCE.convertList02(list);
//        ExcelUtils.write(response, "CRM 模块的机构/公司.xls", "数据", InstitutionExcelVO.class, datas);

    }

}
