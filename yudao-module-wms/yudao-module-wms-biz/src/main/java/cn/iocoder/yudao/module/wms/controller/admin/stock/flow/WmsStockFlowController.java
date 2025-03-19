package cn.iocoder.yudao.module.wms.controller.admin.stock.flow;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_FLOW_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "库存流水")
@RestController
@RequestMapping("/wms/stock-flow")
@Validated
public class WmsStockFlowController {

    @Resource
    private WmsStockFlowService stockFlowService;

    /**
     * @sign : F7B60067C4EBBC25
     */
    @PostMapping("/create")
    @Operation(summary = "创建库存流水")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:create')")
    public CommonResult<Long> createStockFlow(@Valid @RequestBody WmsStockFlowSaveReqVO createReqVO) {
        return success(stockFlowService.createStockFlow(createReqVO).getId());
    }

    /**
     * @sign : 70D5A6239EA6C813
     */
    @PutMapping("/update")
    @Operation(summary = "更新库存流水")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:update')")
    public CommonResult<Boolean> updateStockFlow(@Valid @RequestBody WmsStockFlowSaveReqVO updateReqVO) {
        stockFlowService.updateStockFlow(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存流水")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:delete')")
    public CommonResult<Boolean> deleteStockFlow(@RequestParam("id") Long id) {
        stockFlowService.deleteStockFlow(id);
        return success(true);
    }

    /**
     * @sign : 1840B90C7B488D12
     */
    @GetMapping("/get")
    @Operation(summary = "获得库存流水")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<WmsStockFlowRespVO> getStockFlow(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockFlowDO stockFlow = stockFlowService.getStockFlow(id);
        if (stockFlow == null) {
            throw exception(STOCK_FLOW_NOT_EXISTS);
        }
        // 转换
        WmsStockFlowRespVO stockFlowVO = BeanUtils.toBean(stockFlow, WmsStockFlowRespVO.class);
        // 返回
        return success(stockFlowVO);
    }

    /**
     * @sign : E223AB2DDEC0F1A8
     */
    @GetMapping("/page")
    @Operation(summary = "获得库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:query')")
    public CommonResult<PageResult<WmsStockFlowRespVO>> getStockFlowPage(@Valid WmsStockFlowPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockFlowDO> doPageResult = stockFlowService.getStockFlowPage(pageReqVO);
        // 转换
        PageResult<WmsStockFlowRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockFlowRespVO.class);
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存流水 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-flow:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockFlowExcel(@Valid WmsStockFlowPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockFlowDO> list = stockFlowService.getStockFlowPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存流水.xls", "数据", WmsStockFlowRespVO.class, BeanUtils.toBean(list, WmsStockFlowRespVO.class));
    }
}