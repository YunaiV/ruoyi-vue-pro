package cn.iocoder.yudao.module.wms.controller.admin.stock.flow;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockType;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "库存流水")
@RestController
@RequestMapping("/wms/stock-flow")
@Validated
public class WmsStockFlowController {

    @Resource
    private WmsStockFlowService stockFlowService;

    // /**
    // * @sign : F7B60067C4EBBC25
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建库存流水")
    // @PreAuthorize("@ss.hasPermission('wms:stock-flow:create')")
    // public CommonResult<Long> createStockFlow(@Valid @RequestBody WmsStockFlowSaveReqVO createReqVO) {
    // return success(stockFlowService.createStockFlow(createReqVO).getId());
    // }
    // /**
    // * @sign : 70D5A6239EA6C813
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新库存流水")
    // @PreAuthorize("@ss.hasPermission('wms:stock-flow:update')")
    // public CommonResult<Boolean> updateStockFlow(@Valid @RequestBody WmsStockFlowSaveReqVO updateReqVO) {
    // stockFlowService.updateStockFlow(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除库存流水")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-flow:delete')")
    // public CommonResult<Boolean> deleteStockFlow(@RequestParam("id") Long id) {
    // stockFlowService.deleteStockFlow(id);
    // return success(true);
    // }
    /**
     * @sign : 1840B90C7B488D12
     */
    @GetMapping("/flows")
    @Operation(summary = "获得库存流水")
    @Parameter(name = "stockType", description = "库存类型", required = true, example = "1")
    @Parameter(name = "stockId", description = "库存ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<List<WmsStockFlowRespVO>> getStockFlow(@RequestParam("stockType") Long stockType, @RequestParam("stockId") Long stockId) {
        // 查询数据
        List<WmsStockFlowDO> stockFlowList = stockFlowService.selectStockFlow(stockType, stockId);
        // 转换
        List<WmsStockFlowRespVO> stockFlowVOList = BeanUtils.toBean(stockFlowList, WmsStockFlowRespVO.class);
        // 返回
        return success(stockFlowVOList);
    }

    /**
     * @sign : E223AB2DDEC0F1A8
     */
    @GetMapping("/page-warehouse")
    @Operation(summary = "获得仓库库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPageWarehouse(@Valid WmsStockFlowPageReqVO pageReqVO) {

        pageReqVO.setStockType(WmsStockType.WAREHOUSE.getValue());
        pageReqVO.setReason(new Integer[]{ WmsStockReason.INBOUND.getValue(), WmsStockReason.OUTBOUND_AGREE.getValue() });

        return getStockFlowPage(pageReqVO);
    }

    @GetMapping("/page-ownership")
    @Operation(summary = "获得所有者库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPageOwnership(@Valid WmsStockFlowPageReqVO pageReqVO) {
        pageReqVO.setStockType(WmsStockType.OWNERSHIP.getValue());
        pageReqVO.setReason(new Integer[]{ WmsStockReason.INBOUND.getValue(), WmsStockReason.OUTBOUND_AGREE.getValue() });
        return getStockFlowPage(pageReqVO);
    }

    @GetMapping("/page-bin")
    @Operation(summary = "获得仓位库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPageBin(@Valid WmsStockFlowPageReqVO pageReqVO) {
        pageReqVO.setStockType(WmsStockType.BIN.getValue());
        pageReqVO.setReason(new Integer[]{ WmsStockReason.INBOUND.getValue(), WmsStockReason.PICKUP.getValue(),WmsStockReason.OUTBOUND_AGREE.getValue() });
        return getStockFlowPage(pageReqVO);
    }

    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPage(@Valid WmsStockFlowPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockFlowDO> doPageResult = stockFlowService.getStockFlowPage(pageReqVO);
        // 转换
        PageResult<WmsStockFlowRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockFlowRespVO.class);

        // 装配模型
        stockFlowService.assembleProducts(voPageResult.getList());
        stockFlowService.assembleWarehouse(voPageResult.getList());
        stockFlowService.assembleBin(voPageResult.getList());
        stockFlowService.assembleCompanyAndDept(voPageResult.getList());
        stockFlowService.assembleInbound(voPageResult.getList());
        stockFlowService.assembleOutbound(voPageResult.getList());
        stockFlowService.assemblePickup(voPageResult.getList());


        // 返回
        return success(voPageResult);
    }

    // @GetMapping("/export-excel")
    // @Operation(summary = "导出库存流水 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-flow:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockFlowExcel(@Valid WmsStockFlowPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockFlowDO> list = stockFlowService.getStockFlowPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "库存流水.xls", "数据", WmsStockFlowRespVO.class, BeanUtils.toBean(list, WmsStockFlowRespVO.class));
    // }
}
