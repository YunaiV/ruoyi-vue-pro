package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse;

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
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_WAREHOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "仓库库存")
@RestController
@RequestMapping("/wms/stock-warehouse")
@Validated
public class WmsStockWarehouseController {

    @Resource
    private WmsStockWarehouseService stockWarehouseService;

    /**
     * @sign : AD9D4916BFDB9B45
     */
    @PostMapping("/create")
    @Operation(summary = "创建仓库库存")
    @PreAuthorize("@ss.hasPermission('wms:stock-warehouse:create')")
    public CommonResult<Long> createStockWarehouse(@Valid @RequestBody WmsStockWarehouseSaveReqVO createReqVO) {
        return success(stockWarehouseService.createStockWarehouse(createReqVO).getId());
    }

    /**
     * @sign : A68FAA9D68AA9447
     */
    @PutMapping("/update")
    @Operation(summary = "更新仓库库存")
    @PreAuthorize("@ss.hasPermission('wms:stock-warehouse:update')")
    public CommonResult<Boolean> updateStockWarehouse(@Valid @RequestBody WmsStockWarehouseSaveReqVO updateReqVO) {
        stockWarehouseService.updateStockWarehouse(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除仓库库存")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-warehouse:delete')")
    public CommonResult<Boolean> deleteStockWarehouse(@RequestParam("id") Long id) {
        stockWarehouseService.deleteStockWarehouse(id);
        return success(true);
    }

    /**
     * @sign : 6807425D09A7BD34
     */
    @GetMapping("/get")
    @Operation(summary = "获得仓库库存")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-warehouse:query')")
    public CommonResult<WmsStockWarehouseRespVO> getStockWarehouse(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockWarehouseDO stockWarehouse = stockWarehouseService.getStockWarehouse(id);
        if (stockWarehouse == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        }
        // 转换
        WmsStockWarehouseRespVO stockWarehouseVO = BeanUtils.toBean(stockWarehouse, WmsStockWarehouseRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(stockWarehouseVO))
			.mapping(WmsStockWarehouseRespVO::getCreator, WmsStockWarehouseRespVO::setCreatorName)
			.mapping(WmsStockWarehouseRespVO::getCreator, WmsStockWarehouseRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(stockWarehouseVO);
    }

    /**
     * @sign : 5473D7BBFDAEBB83
     */
    @GetMapping("/page")
    @Operation(summary = "获得仓库库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-warehouse:query')")
    public CommonResult<PageResult<WmsStockWarehouseRespVO>> getStockWarehousePage(@Valid WmsStockWarehousePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockWarehouseDO> doPageResult = stockWarehouseService.getStockWarehousePage(pageReqVO);
        // 转换
        PageResult<WmsStockWarehouseRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockWarehouseRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsStockWarehouseRespVO::getCreator, WmsStockWarehouseRespVO::setCreatorName)
			.mapping(WmsStockWarehouseRespVO::getCreator, WmsStockWarehouseRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓库库存 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-warehouse:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockWarehouseExcel(@Valid WmsStockWarehousePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockWarehouseDO> list = stockWarehouseService.getStockWarehousePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "仓库库存.xls", "数据", WmsStockWarehouseRespVO.class, BeanUtils.toBean(list, WmsStockWarehouseRespVO.class));
    }
}