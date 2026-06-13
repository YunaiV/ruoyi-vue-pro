package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesSaveReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesShippingReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice.MesWmSalesNoticeDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.wm.productsales.MesWmProductSalesService;
import cn.iocoder.yudao.module.mes.service.wm.salesnotice.MesWmSalesNoticeService;
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
import java.util.*;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 销售出库单")
@RestController
@RequestMapping("/mes/wm/product-sales")
@Validated
public class MesWmProductSalesController {

    @Resource
    private MesWmProductSalesService productSalesService;
    @Resource
    private MesMdClientService clientService;
    @Resource
    private MesWmSalesNoticeService salesNoticeService;

    @PostMapping("/create")
    @Operation(summary = "创建销售出库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:create')")
    public CommonResult<Long> createProductSales(@Valid @RequestBody MesWmProductSalesSaveReqVO createReqVO) {
        return success(productSalesService.createProductSales(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改销售出库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:update')")
    public CommonResult<Boolean> updateProductSales(@Valid @RequestBody MesWmProductSalesSaveReqVO updateReqVO) {
        productSalesService.updateProductSales(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:delete')")
    public CommonResult<Boolean> deleteProductSales(@RequestParam("id") Long id) {
        productSalesService.deleteProductSales(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:query')")
    public CommonResult<MesWmProductSalesRespVO> getProductSales(@RequestParam("id") Long id) {
        MesWmProductSalesDO sales = productSalesService.getProductSales(id);
        if (sales == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(sales)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售出库单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:query')")
    public CommonResult<PageResult<MesWmProductSalesRespVO>> getProductSalesPage(
            @Valid MesWmProductSalesPageReqVO pageReqVO) {
        PageResult<MesWmProductSalesDO> pageResult = productSalesService.getProductSalesPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售出库单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductSalesExcel(@Valid MesWmProductSalesPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmProductSalesDO> pageResult = productSalesService.getProductSalesPage(pageReqVO);
        ExcelUtils.write(response, "销售出库单.xls", "数据", MesWmProductSalesRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交销售出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:submit')")
    public CommonResult<Boolean> submitProductSales(@RequestParam("id") Long id) {
        productSalesService.submitProductSales(id);
        return success(true);
    }

    @GetMapping("/check-quantity")
    @Operation(summary = "校验销售出库单数量")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:query')")
    public CommonResult<Boolean> checkProductSalesQuantity(@RequestParam("id") Long id) {
        return success(productSalesService.checkProductSalesQuantity(id));
    }

    @PutMapping("/stock")
    @Operation(summary = "执行拣货")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:stock')")
    public CommonResult<Boolean> stockProductSales(@RequestParam("id") Long id) {
        productSalesService.stockProductSales(id);
        return success(true);
    }

    @PutMapping("/shipping")
    @Operation(summary = "填写运单")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:shipping')")
    public CommonResult<Boolean> shippingProductSales(@Valid @RequestBody MesWmProductSalesShippingReqVO reqVO) {
        productSalesService.shippingProductSales(reqVO);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "执行出库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:finish')")
    public CommonResult<Boolean> finishProductSales(@RequestParam("id") Long id) {
        productSalesService.finishProductSales(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消销售出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-sales:cancel')")
    public CommonResult<Boolean> cancelProductSales(@RequestParam("id") Long id) {
        productSalesService.cancelProductSales(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesWmProductSalesRespVO> buildRespVOList(List<MesWmProductSalesDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesWmProductSalesDO::getClientId));
        Map<Long, MesWmSalesNoticeDO> noticeMap = salesNoticeService.getSalesNoticeMap(
                convertSet(list, MesWmProductSalesDO::getNoticeId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmProductSalesRespVO.class, vo -> {
            MapUtils.findAndThen(clientMap, vo.getClientId(), client ->
                    vo.setClientName(client.getName()).setClientCode(client.getCode()));
            MapUtils.findAndThen(noticeMap, vo.getNoticeId(),
                    notice -> vo.setNoticeCode(notice.getCode()));
        });
    }

}
