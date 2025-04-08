package cn.iocoder.yudao.module.srm.controller.admin.purchase;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 供应商")
@RestController
@RequestMapping("/srm/supplier")
@Validated
public class SrmSupplierController {

    @Resource
    private SrmSupplierService supplierService;

    @PostMapping("/create")
    @Operation(summary = "创建供应商")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('srm:supplier:create')")
    public CommonResult<Long> createSupplier(@Valid @RequestBody SrmSupplierSaveReqVO createReqVO) {
        return success(supplierService.createSupplier(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新供应商")
    @PreAuthorize("@ss.hasPermission('srm:supplier:update')")
    public CommonResult<Boolean> updateSupplier(@Valid @RequestBody SrmSupplierSaveReqVO updateReqVO) {
        supplierService.updateSupplier(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('srm:supplier:delete')")
    public CommonResult<Boolean> deleteSupplier(@RequestParam("id") Long id) {
        supplierService.deleteSupplier(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应商")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('srm:supplier:query')")
    public CommonResult<SrmSupplierRespVO> getSupplier(@RequestParam("id") Long id) {
        SrmSupplierDO supplier = supplierService.getSupplier(id);
        return success(BeanUtils.toBean(supplier, SrmSupplierRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应商分页")
    @PreAuthorize("@ss.hasPermission('srm:supplier:query')")
    public CommonResult<PageResult<SrmSupplierRespVO>> getSupplierPage(@Valid SrmSupplierPageReqVO pageReqVO) {
        PageResult<SrmSupplierDO> pageResult = supplierService.getSupplierPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SrmSupplierRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得供应商精简列表", description = "只包含被开启的供应商，主要用于前端的下拉选项")
    @PreAuthorize("@ss.hasPermission('srm:supplier:query')")
    public CommonResult<List<SrmSupplierRespVO>> getSupplierSimpleList() {
        List<SrmSupplierDO> list = supplierService.getSupplierListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(BeanUtils.toBean(list, SrmSupplierRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出供应商 Excel")
    @PreAuthorize("@ss.hasPermission('srm:supplier:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSupplierExcel(@Valid SrmSupplierPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SrmSupplierDO> list = supplierService.getSupplierPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "供应商.xls", "数据", SrmSupplierRespVO.class,
            BeanUtils.toBean(list, SrmSupplierRespVO.class));
    }

}