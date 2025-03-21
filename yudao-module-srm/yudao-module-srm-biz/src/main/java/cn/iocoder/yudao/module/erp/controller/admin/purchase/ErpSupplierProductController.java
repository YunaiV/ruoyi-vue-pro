package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierProductService;
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
@RequestMapping("/erp/supplier-product")
@Validated
public class ErpSupplierProductController {

    @Resource
    private ErpSupplierProductService supplierProductService;



    @PostMapping("/create")
    @Operation(summary = "创建ERP 供应商产品")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('erp:supplier-product:create')")
    public CommonResult<Long> createSupplierProduct(@Valid @RequestBody ErpSupplierProductSaveReqVO createReqVO) {
        return success(supplierProductService.createSupplierProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP 供应商产品")
    @PreAuthorize("@ss.hasPermission('erp:supplier-product:update')")
    public CommonResult<Boolean> updateSupplierProduct(@Valid @RequestBody ErpSupplierProductSaveReqVO updateReqVO) {
        supplierProductService.updateSupplierProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP 供应商产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:supplier-product:delete')")
    public CommonResult<Boolean> deleteSupplierProduct(@RequestParam("id") Long id) {
        supplierProductService.deleteSupplierProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP 供应商产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:supplier-product:query')")
    public CommonResult<ErpSupplierProductRespVO> getSupplierProduct(@RequestParam("id") Long id) {
        ErpSupplierProductDO supplierProduct = supplierProductService.getSupplierProduct(id);
        return success(BeanUtils.toBean(supplierProduct, ErpSupplierProductRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP 供应商产品分页")
    @PreAuthorize("@ss.hasPermission('erp:supplier-product:query')")
    public CommonResult<PageResult<ErpSupplierProductRespVO>> getSupplierProductPage(@Valid ErpSupplierProductPageReqVO pageReqVO) {
        PageResult<ErpSupplierProductDO> pageResult = supplierProductService.getSupplierProductPage(pageReqVO);
        return success(supplierProductService.buildSupplierProductVOPageResult(pageResult));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得ERP 供应商产品精简列表", description = "只包含被开启的产品，主要用于前端的下拉选项")
    public CommonResult<List<ErpSupplierProductRespVO>> getProductSimpleList() {
        List<ErpSupplierProductRespVO> list = supplierProductService.getSupplierProductVOListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP 供应商产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:supplier-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSupplierProductExcel(@Valid ErpSupplierProductPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpSupplierProductRespVO> list = supplierProductService.buildSupplierProductVOPageResult(supplierProductService.getSupplierProductPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "ERP 供应商产品.xls", "数据", ErpSupplierProductRespVO.class,
                        BeanUtils.toBean(list, ErpSupplierProductRespVO.class));
    }



}