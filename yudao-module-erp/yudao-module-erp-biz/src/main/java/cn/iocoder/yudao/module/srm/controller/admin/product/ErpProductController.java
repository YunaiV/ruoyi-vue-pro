package cn.iocoder.yudao.module.srm.controller.admin.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductSimpleRespVO;
import cn.iocoder.yudao.module.srm.service.product.ErpProductServiceDelegator;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * @author Administrator
 */
@Tag(name = "管理后台 - ERP 产品")
@RestController
@RequestMapping("/erp/product")
@Validated
public class ErpProductController {

    @Resource
    private ErpProductServiceDelegator productService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('erp:product:create')")
    @Idempotent
    public CommonResult<Long> createProduct(@Valid @RequestBody ErpProductSaveReqVO createReqVO) {
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody ErpProductSaveReqVO updateReqVO) {
        productService.updateProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<ErpProductRespVO> getProduct(@RequestParam("id") Long id) {
        ErpProductRespVO productRespVO = productService.getProduct(id);
        if (productRespVO == null) {
            return success(null);
        }
        return success(productRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<PageResult<ErpProductRespVO>> getProductPage(@Valid ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductRespVO> productVOPage = productService.getProductVOPage(pageReqVO);
        return success(productVOPage);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得产品精简列表", description = "只包含被开启的产品，主要用于前端的下拉选项")
    public CommonResult<List<ErpProductSimpleRespVO>> getProductSimpleList() {
        List<ErpProductRespVO> list = productService.getProductVOListByStatus(true);
//        pageReqVO.setPageSize(100);//返回100个结果
//        PageResult<ErpProductRespVO> voPage = productService.getProductVOPage(pageReqVO);
//        List<ErpProductRespVO> list = voPage.getList();
        return success(convertList(list, vo -> BeanUtils.toBean(vo, ErpProductSimpleRespVO.class)));
    }

    //获得产品精简列表(高效)
    @GetMapping("/simple-list-efficient")
    @Operation(summary = "获得产品精简列表(高效)返回100个结果")
    public CommonResult<List<ErpProductSimpleRespVO>> getProductSimpleListEfficient(@Valid ErpProductPageReqVO pageReqVO) {
        pageReqVO.setPageSize(100);//返回100个结果
        PageResult<ErpProductRespVO> voPage = productService.getProductVOPage(pageReqVO);
        List<ErpProductRespVO> list = voPage.getList();
        return success(convertList(list, vo -> BeanUtils.toBean(vo, ErpProductSimpleRespVO.class)));
    }


    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid ErpProductPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpProductRespVO> pageResult = productService.getProductVOPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "产品.xls", "数据", ErpProductRespVO.class,
            pageResult.getList());
    }

}