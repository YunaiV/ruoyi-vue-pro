package cn.iocoder.yudao.module.fms.controller.admin.finance.subject;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanyPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanyRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsCompanyDO;
import cn.iocoder.yudao.module.fms.service.finance.subject.FmsCompanyService;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - Fms财务公司")
@RestController
@RequestMapping("/fms/company")
@Validated
public class FmsCompanyController {

    @Resource
    private FmsCompanyService CompanyService;

    @PostMapping("/create")
    @Operation(summary = "创建Fms财务公司")
    @PreAuthorize("@ss.hasPermission('fms:company:create')")
    public CommonResult<Long> createCompany(@Validated(Validation.OnCreate.class) @RequestBody FmsCompanySaveReqVO createReqVO) {
        return success(CompanyService.createCompany(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Fms财务公司")
    @PreAuthorize("@ss.hasPermission('fms:company:update')")
    public CommonResult<Boolean> updateCompany(@Validated(Validation.OnUpdate.class) @RequestBody FmsCompanySaveReqVO updateReqVO) {
        CompanyService.updateCompany(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Fms财务公司")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('fms:company:delete')")
    public CommonResult<Boolean> deleteCompany(@NotNull @RequestParam("id") Long id) {
        CompanyService.deleteCompany(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Fms财务公司")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('fms:company:query')")
    public CommonResult<FmsCompanyRespVO> getCompany(@NotNull @RequestParam("id") Long id) {
        FmsCompanyDO Company = CompanyService.getCompany(id);
        return success(BeanUtils.toBean(Company, FmsCompanyRespVO.class));
    }

    @GetMapping("/simple-list")
    @PreAuthorize("@ss.hasPermission('fms:company:query')")
    @Operation(summary = "获得Fms财务公司精简列表", description = "只包含被开启的财务主体，主要用于前端的下拉选项")
    public CommonResult<List<FmsCompanySimpleRespVO>> getWarehouseSimpleList() {
        List<FmsCompanySimpleRespVO> respVOS = CompanyService.ListCompanySimple();
        return success(respVOS);
    }

    @GetMapping("/page")
    @Operation(summary = "获得Fms财务公司分页")
    @PreAuthorize("@ss.hasPermission('fms:company:query')")
    public CommonResult<PageResult<FmsCompanyRespVO>> getCompanyPage(@Valid FmsCompanyPageReqVO pageReqVO) {
        PageResult<FmsCompanyDO> pageResult = CompanyService.getCompanyPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FmsCompanyRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Fms财务公司 Excel")
    @PreAuthorize("@ss.hasPermission('fms:company:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCompanyExcel(@Valid FmsCompanyPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FmsCompanyDO> list = CompanyService.getCompanyPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Fms财务公司.xls", "数据", FmsCompanyRespVO.class,
            BeanUtils.toBean(list, FmsCompanyRespVO.class));
    }

}