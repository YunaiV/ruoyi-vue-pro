package cn.iocoder.yudao.module.fms.controller.admin.finance.subject;

import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectSimpleRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsFinanceSubjectDO;
import cn.iocoder.yudao.module.fms.service.finance.subject.FmsFinanceSubjectService;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
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

@Tag(name = "管理后台 - Erp财务主体")
@RestController
@RequestMapping("/erp/finance-subject")
@Validated
public class FmsFinanceSubjectController {

    @Resource
    private FmsFinanceSubjectService financeSubjectService;

    @PostMapping("/create")
    @Operation(summary = "创建Erp财务主体")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:create')")
    public CommonResult<Long> createFinanceSubject(@Validated(Validation.OnCreate.class) @RequestBody FmsFinanceSubjectSaveReqVO createReqVO) {
        return success(financeSubjectService.createFinanceSubject(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Erp财务主体")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:update')")
    public CommonResult<Boolean> updateFinanceSubject(@Validated(Validation.OnUpdate.class) @RequestBody FmsFinanceSubjectSaveReqVO updateReqVO) {
        financeSubjectService.updateFinanceSubject(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Erp财务主体")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:delete')")
    public CommonResult<Boolean> deleteFinanceSubject(@NotNull @RequestParam("id") Long id) {
        financeSubjectService.deleteFinanceSubject(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Erp财务主体")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:query')")
    public CommonResult<FmsFinanceSubjectRespVO> getFinanceSubject(@NotNull @RequestParam("id") Long id) {
        FmsFinanceSubjectDO financeSubject = financeSubjectService.getFinanceSubject(id);
        return success(BeanUtils.toBean(financeSubject, FmsFinanceSubjectRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得Erp财务主体精简列表", description = "只包含被开启的财务主体，主要用于前端的下拉选项")
    public CommonResult<List<FmsFinanceSubjectSimpleRespVO>> getWarehouseSimpleList() {
        List<FmsFinanceSubjectSimpleRespVO> respVOS = financeSubjectService.ListFinanceSubjectSimple();
        return success(respVOS);
    }

    @GetMapping("/page")
    @Operation(summary = "获得Erp财务主体分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:query')")
    public CommonResult<PageResult<FmsFinanceSubjectRespVO>> getFinanceSubjectPage(@Valid FmsFinanceSubjectPageReqVO pageReqVO) {
        PageResult<FmsFinanceSubjectDO> pageResult = financeSubjectService.getFinanceSubjectPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FmsFinanceSubjectRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Erp财务主体 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinanceSubjectExcel(@Valid FmsFinanceSubjectPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FmsFinanceSubjectDO> list = financeSubjectService.getFinanceSubjectPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Erp财务主体.xls", "数据", FmsFinanceSubjectRespVO.class,
            BeanUtils.toBean(list, FmsFinanceSubjectRespVO.class));
    }

}