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
import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import cn.iocoder.yudao.module.jl.mapper.crm.InstitutionMapper;
import cn.iocoder.yudao.module.jl.service.crm.InstitutionService;

@Tag(name = "管理后台 - 机构/公司")
@RestController
@RequestMapping("/jl/institution")
@Validated
public class InstitutionController {

    @Resource
    private InstitutionService institutionService;

    @Resource
    private InstitutionMapper institutionMapper;

    @PostMapping("/create")
    @Operation(summary = "创建机构/公司")
    @PreAuthorize("@ss.hasPermission('jl:institution:create')")
    public CommonResult<Long> createInstitution(@Valid @RequestBody InstitutionCreateReqVO createReqVO) {
        return success(institutionService.createInstitution(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机构/公司")
    @PreAuthorize("@ss.hasPermission('jl:institution:update')")
    public CommonResult<Boolean> updateInstitution(@Valid @RequestBody InstitutionUpdateReqVO updateReqVO) {
        institutionService.updateInstitution(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除机构/公司")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:institution:delete')")
    public CommonResult<Boolean> deleteInstitution(@RequestParam("id") Long id) {
        institutionService.deleteInstitution(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得机构/公司")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:institution:query')")
    public CommonResult<InstitutionRespVO> getInstitution(@RequestParam("id") Long id) {
            Optional<Institution> institution = institutionService.getInstitution(id);
        return success(institution.map(institutionMapper::toDto).orElseThrow(() -> exception(INSTITUTION_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得机构/公司列表")
    @PreAuthorize("@ss.hasPermission('jl:institution:query')")
    public CommonResult<PageResult<InstitutionRespVO>> getInstitutionPage(@Valid InstitutionPageReqVO pageVO) {
        PageResult<Institution> pageResult = institutionService.getInstitutionPage(pageVO);
        return success(institutionMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机构/公司 Excel")
    @PreAuthorize("@ss.hasPermission('jl:institution:export')")
    @OperateLog(type = EXPORT)
    public void exportInstitutionExcel(@Valid InstitutionExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<Institution> list = institutionService.getInstitutionList(exportReqVO);
        // 导出 Excel
        List<InstitutionExcelVO> datas = institutionMapper.toExcelList(list);
        ExcelUtils.write(response, "机构/公司.xls", "数据", InstitutionExcelVO.class, datas);
    }

}
