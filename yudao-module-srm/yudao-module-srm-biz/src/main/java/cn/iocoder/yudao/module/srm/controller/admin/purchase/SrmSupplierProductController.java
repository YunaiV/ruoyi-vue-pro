package cn.iocoder.yudao.module.srm.controller.admin.purchase;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierProductDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierProductService;
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

@Tag(name = "管理后台 - ERP 供应商产品")
@RestController
@RequestMapping("/srm/supplier-product")
@Validated
public class SrmSupplierProductController {

    @Resource
    private SrmSupplierProductService supplierProductService;



    @PostMapping("/create")
    @Operation(summary = "创建ERP 供应商产品")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('srm:supplier-product:create')")
    public CommonResult<Long> createSupplierProduct(@Valid @RequestBody SrmSupplierProductSaveReqVO createReqVO) {
        return success(supplierProductService.createSupplierProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP 供应商产品")
    @PreAuthorize("@ss.hasPermission('srm:supplier-product:update')")
    public CommonResult<Boolean> updateSupplierProduct(@Valid @RequestBody SrmSupplierProductSaveReqVO updateReqVO) {
        supplierProductService.updateSupplierProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP 供应商产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('srm:supplier-product:delete')")
    public CommonResult<Boolean> deleteSupplierProduct(@RequestParam("id") Long id) {
        supplierProductService.deleteSupplierProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP 供应商产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('srm:supplier-product:query')")
    public CommonResult<SrmSupplierProductRespVO> getSupplierProduct(@RequestParam("id") Long id) {
        SrmSupplierProductDO supplierProduct = supplierProductService.getSupplierProduct(id);
        return success(BeanUtils.toBean(supplierProduct, SrmSupplierProductRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP 供应商产品分页")
    @PreAuthorize("@ss.hasPermission('srm:supplier-product:query')")
    public CommonResult<PageResult<SrmSupplierProductRespVO>> getSupplierProductPage(@Valid SrmSupplierProductPageReqVO pageReqVO) {
        PageResult<SrmSupplierProductDO> pageResult = supplierProductService.getSupplierProductPage(pageReqVO);
        return success(supplierProductService.buildSupplierProductVOPageResult(pageResult));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得ERP 供应商产品精简列表", description = "只包含被开启的产品，主要用于前端的下拉选项")
    public CommonResult<List<SrmSupplierProductRespVO>> getProductSimpleList() {
        List<SrmSupplierProductRespVO> list = supplierProductService.getSupplierProductVOListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP 供应商产品 Excel")
    @PreAuthorize("@ss.hasPermission('srm:supplier-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSupplierProductExcel(@Valid SrmSupplierProductPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SrmSupplierProductRespVO> list = supplierProductService.buildSupplierProductVOPageResult(supplierProductService.getSupplierProductPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "ERP 供应商产品.xls", "数据", SrmSupplierProductRespVO.class,
                        BeanUtils.toBean(list, SrmSupplierProductRespVO.class));
    }



}