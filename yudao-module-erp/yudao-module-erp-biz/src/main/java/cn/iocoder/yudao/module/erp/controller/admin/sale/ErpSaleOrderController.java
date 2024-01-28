package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderService;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;


@Tag(name = "管理后台 - ERP 销售订单")
@RestController
@RequestMapping("/erp/sale-order")
@Validated
public class ErpSaleOrderController {

    @Resource
    private ErpSaleOrderService saleOrderService;

    // TODO 芋艿：待 review
    @PostMapping("/create")
    @Operation(summary = "创建ERP 销售订单")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:create')")
    public CommonResult<Long> createSaleOrder(@Valid @RequestBody ErpSaleOrderSaveReqVO createReqVO) {
        return success(saleOrderService.createSaleOrder(createReqVO));
    }

    // TODO 芋艿：待 review
    @PutMapping("/update")
    @Operation(summary = "更新ERP 销售订单")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:update')")
    public CommonResult<Boolean> updateSaleOrder(@Valid @RequestBody ErpSaleOrderSaveReqVO updateReqVO) {
        saleOrderService.updateSaleOrder(updateReqVO);
        return success(true);
    }

    // TODO 芋艿：待 review
    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP 销售订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sale-order:delete')")
    public CommonResult<Boolean> deleteSaleOrder(@RequestParam("id") Long id) {
        saleOrderService.deleteSaleOrder(id);
        return success(true);
    }

    // TODO 芋艿：待 review
    @GetMapping("/get")
    @Operation(summary = "获得ERP 销售订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:query')")
    public CommonResult<ErpSaleOrderRespVO> getSaleOrder(@RequestParam("id") Long id) {
        ErpSaleOrderDO saleOrder = saleOrderService.getSaleOrder(id);
        return success(BeanUtils.toBean(saleOrder, ErpSaleOrderRespVO.class));
    }

    // TODO 芋艿：待 review
    @GetMapping("/page")
    @Operation(summary = "获得ERP 销售订单分页")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:query')")
    public CommonResult<PageResult<ErpSaleOrderRespVO>> getSaleOrderPage(@Valid ErpSaleOrderPageReqVO pageReqVO) {
        PageResult<ErpSaleOrderDO> pageResult = saleOrderService.getSaleOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ErpSaleOrderRespVO.class));
    }

    // TODO 芋艿：待 review
    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP 销售订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:export')")
    @OperateLog(type = EXPORT)
    public void exportSaleOrderExcel(@Valid ErpSaleOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpSaleOrderDO> list = saleOrderService.getSaleOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "ERP 销售订单.xls", "数据", ErpSaleOrderRespVO.class,
                        BeanUtils.toBean(list, ErpSaleOrderRespVO.class));
    }

}