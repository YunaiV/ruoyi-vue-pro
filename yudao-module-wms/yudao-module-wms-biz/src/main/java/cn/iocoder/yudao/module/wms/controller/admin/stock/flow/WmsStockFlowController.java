package cn.iocoder.yudao.module.wms.controller.admin.stock.flow;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockType;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
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
    @PostMapping("/page-warehouse")
    @Operation(summary = "获得仓库库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPageWarehouse(@Valid @RequestBody WmsStockFlowPageReqVO pageReqVO) {
        pageReqVO.setStockType(WmsStockType.WAREHOUSE.getValue());
        pageReqVO.setReason(new Integer[]{WmsStockReason.INBOUND.getValue(), WmsStockReason.OUTBOUND_FINISH.getValue()});
        return getStockFlowPage(pageReqVO);
    }

    @PostMapping("/page-logic")
    @Operation(summary = "获得逻辑库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPageLogic(@Valid @RequestBody WmsStockFlowPageReqVO pageReqVO) {
        pageReqVO.setStockType(WmsStockType.LOGIC.getValue());
        pageReqVO.setReason(new Integer[]{WmsStockReason.INBOUND.getValue(), WmsStockReason.OUTBOUND_FINISH.getValue()});
        return getStockFlowPage(pageReqVO);
    }

    @PostMapping("/page-bin")
    @Operation(summary = "获得仓位库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPageBin(@Valid @RequestBody WmsStockFlowPageReqVO pageReqVO) {
        pageReqVO.setStockType(WmsStockType.BIN.getValue());
        pageReqVO.setReason(new Integer[]{WmsStockReason.INBOUND.getValue(), WmsStockReason.PICKUP.getValue(), WmsStockReason.OUTBOUND_FINISH.getValue(), WmsStockReason.EXCHANGE.getValue()});
        return getStockFlowPage(pageReqVO);
    }

    /**
     * @sign : E223AB2DDEC0F1A8
     */
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPage(@Valid WmsStockFlowPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockFlowDO> doPageResult = stockFlowService.getStockFlowPage(pageReqVO);
        // 转换
        PageResult<WmsStockFlowRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockFlowRespVO.class);
        stockFlowService.assembleProducts(voPageResult.getList());
        stockFlowService.assembleBin(voPageResult.getList());
        stockFlowService.assembleWarehouse(voPageResult.getList());
        stockFlowService.assembleInbound(voPageResult.getList());
        stockFlowService.assembleOutbound(voPageResult.getList());
        stockFlowService.assemblePickup(voPageResult.getList());
        stockFlowService.assembleStockWarehouse(voPageResult.getList());
        stockFlowService.assembleInboundItemFlow(voPageResult.getList());
        stockFlowService.assembleCompanyAndDept(voPageResult.getList());
        stockFlowService.assembleStockCheck(voPageResult.getList());
        stockFlowService.assembleBinMove(voPageResult.getList());
        stockFlowService.assembleLogicMove(voPageResult.getList());
        stockFlowService.assembleExchange(voPageResult.getList());
        //批次可用库存数量显示为库存变更前数量
        stockFlowService.assembleBatchAvailableQty(voPageResult.getList());
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsStockFlowRespVO::getCreator, WmsStockFlowRespVO::setCreatorName)
			.mapping(WmsStockFlowRespVO::getUpdater, WmsStockFlowRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @PostMapping("/export-warehouse")
    @Operation(summary = "导出仓库库存流水 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:export-warehouse')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockWarehouseFlowExcel(@Valid @RequestBody WmsStockFlowPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockFlowRespVO> list = this.getStockFlowPageWarehouse(pageReqVO).getData().getList();
        Map<Long,WmsStockFlowRespVO> map = StreamX.from(list).toMap(WmsStockFlowRespVO::getId);
        List<WmsStockFlowWarehouseExcelVO> excelVOS = BeanUtils.toBean(list, WmsStockFlowWarehouseExcelVO.class);
        for (WmsStockFlowWarehouseExcelVO excelVO : excelVOS) {
            WmsStockFlowRespVO flowRespVO = map.get(excelVO.getId());
            if(flowRespVO==null) {
                continue;
            }
            applyExcelVO(excelVO, flowRespVO);
        }
        // 导出 Excel
        ExcelUtils.write(response, "仓库库存流水.xls", "数据", WmsStockFlowWarehouseExcelVO.class, excelVOS);
    }

    private void applyExcelVO(WmsStockFlowWarehouseExcelVO excelVO, WmsStockFlowRespVO flowRespVO) {
        excelVO.setWarehouseName(flowRespVO.getWarehouse().getName());
        excelVO.setProductCode(flowRespVO.getProduct().getCode());
        excelVO.setFlowTime(DateUtils.formatLocalDateTime(flowRespVO.getCreateTime()));
        if(flowRespVO.getInbound()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getInbound().getCode());
        } else if(flowRespVO.getOutbound()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getOutbound().getCode());
        } else if(flowRespVO.getPickup()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getPickup().getCode());
        } else if (flowRespVO.getStockCheck() != null) {
            excelVO.setReasonBillCode(flowRespVO.getStockCheck().getCode());
        } else if(flowRespVO.getStockBinMove()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getStockBinMove().getNo());
        } else if (flowRespVO.getStockLogicMove() != null) {
            excelVO.setReasonBillCode(flowRespVO.getStockLogicMove().getNo());
        }
        excelVO.setDirectionLabel(WmsStockFlowDirection.parse(flowRespVO.getDirection()).getLabel());

    }


    @PostMapping("/export-bin")
    @Operation(summary = "导出库位库存流水 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:export-bin')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockBinFlowExcel(@Valid @RequestBody WmsStockFlowPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockFlowRespVO> list = this.getStockFlowPageWarehouse(pageReqVO).getData().getList();
        Map<Long,WmsStockFlowRespVO> map = StreamX.from(list).toMap(WmsStockFlowRespVO::getId);
        List<WmsStockFlowBinExcelVO> excelVOS = BeanUtils.toBean(list, WmsStockFlowBinExcelVO.class);
        for (WmsStockFlowBinExcelVO excelVO : excelVOS) {
            WmsStockFlowRespVO flowRespVO = map.get(excelVO.getId());
            if(flowRespVO==null) {
                continue;
            }
            applyExcelVO(excelVO, flowRespVO);
        }
        // 导出 Excel
        ExcelUtils.write(response, "库位库存流水.xls", "数据", WmsStockFlowBinExcelVO.class, excelVOS);
    }

    private void applyExcelVO(WmsStockFlowBinExcelVO excelVO, WmsStockFlowRespVO flowRespVO) {
        excelVO.setWarehouseName(flowRespVO.getWarehouse().getName());
        excelVO.setProductCode(flowRespVO.getProduct().getCode());
        excelVO.setFlowTime(DateUtils.formatLocalDateTime(flowRespVO.getCreateTime()));
        if(flowRespVO.getInbound()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getInbound().getCode());
        } else if(flowRespVO.getOutbound()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getOutbound().getCode());
        } else if(flowRespVO.getPickup()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getPickup().getCode());
        } else if (flowRespVO.getStockCheck() != null) {
            excelVO.setReasonBillCode(flowRespVO.getStockCheck().getCode());
        } else if(flowRespVO.getStockBinMove()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getStockBinMove().getNo());
        } else if (flowRespVO.getStockLogicMove() != null) {
            excelVO.setReasonBillCode(flowRespVO.getStockLogicMove().getNo());
        }
        excelVO.setDirectionLabel(WmsStockFlowDirection.parse(flowRespVO.getDirection()).getLabel());
        excelVO.setBinName(flowRespVO.getBin().getName());
    }


    @PostMapping("/export-logic")
    @Operation(summary = "导出库位库存流水 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:export-logic')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockLogicFlowExcel(@Valid @RequestBody WmsStockFlowPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockFlowRespVO> list = this.getStockFlowPageWarehouse(pageReqVO).getData().getList();
        Map<Long,WmsStockFlowRespVO> map = StreamX.from(list).toMap(WmsStockFlowRespVO::getId);
        List<WmsStockFlowLogicExcelVO> excelVOS = BeanUtils.toBean(list, WmsStockFlowLogicExcelVO.class);
        for (WmsStockFlowLogicExcelVO excelVO : excelVOS) {
            WmsStockFlowRespVO flowRespVO = map.get(excelVO.getId());
            if(flowRespVO==null) {
                continue;
            }
            applyExcelVO(excelVO, flowRespVO);
        }
        // 导出 Excel
        ExcelUtils.write(response, "逻辑库存流水.xls", "数据", WmsStockFlowLogicExcelVO.class, excelVOS);
    }

    private void applyExcelVO(WmsStockFlowLogicExcelVO excelVO, WmsStockFlowRespVO flowRespVO) {
        excelVO.setWarehouseName(flowRespVO.getWarehouse().getName());
        excelVO.setProductCode(flowRespVO.getProduct().getCode());
        excelVO.setFlowTime(DateUtils.formatLocalDateTime(flowRespVO.getCreateTime()));
        if(flowRespVO.getInbound()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getInbound().getCode());
        } else if(flowRespVO.getOutbound()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getOutbound().getCode());
        } else if(flowRespVO.getPickup()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getPickup().getCode());
        } else if (flowRespVO.getStockCheck() != null) {
            excelVO.setReasonBillCode(flowRespVO.getStockCheck().getCode());
        } else if(flowRespVO.getStockBinMove()!=null) {
            excelVO.setReasonBillCode(flowRespVO.getStockBinMove().getNo());
        } else if (flowRespVO.getStockLogicMove() != null) {
            excelVO.setReasonBillCode(flowRespVO.getStockLogicMove().getNo());
        }
        excelVO.setDirectionLabel(WmsStockFlowDirection.parse(flowRespVO.getDirection()).getLabel());
        excelVO.setCompanyName(flowRespVO.getCompany().getName());
        excelVO.setDeptName(flowRespVO.getDept().getName());
    }


}
