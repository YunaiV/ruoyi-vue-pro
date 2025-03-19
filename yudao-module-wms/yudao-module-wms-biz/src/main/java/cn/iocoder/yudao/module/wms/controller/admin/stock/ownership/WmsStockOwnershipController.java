package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership;

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

import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;

@Tag(name = "管理后台 - 所有者库存")
@RestController
@RequestMapping("/wms/stock-ownership")
@Validated
public class WmsStockOwnershipController {

    @Resource
    private WmsStockOwnershipService stockOwnershipService;

    @PostMapping("/create")
    @Operation(summary = "创建所有者库存")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:create')")
    public CommonResult<Long> createStockOwnership(@Valid @RequestBody WmsStockOwnershipSaveReqVO createReqVO) {
        return success(stockOwnershipService.createStockOwnership(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新所有者库存")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:update')")
    public CommonResult<Boolean> updateStockOwnership(@Valid @RequestBody WmsStockOwnershipSaveReqVO updateReqVO) {
        stockOwnershipService.updateStockOwnership(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除所有者库存")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:delete')")
    public CommonResult<Boolean> deleteStockOwnership(@RequestParam("id") Long id) {
        stockOwnershipService.deleteStockOwnership(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得所有者库存")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:query')")
    public CommonResult<WmsStockOwnershipRespVO> getStockOwnership(@RequestParam("id") Long id) {
        WmsStockOwnershipDO stockOwnership = stockOwnershipService.getStockOwnership(id);
        return success(BeanUtils.toBean(stockOwnership, WmsStockOwnershipRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得所有者库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:query')")
    public CommonResult<PageResult<WmsStockOwnershipRespVO>> getStockOwnershipPage(@Valid WmsStockOwnershipPageReqVO pageReqVO) {
        PageResult<WmsStockOwnershipDO> pageResult = stockOwnershipService.getStockOwnershipPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockOwnershipRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出所有者库存 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockOwnershipExcel(@Valid WmsStockOwnershipPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockOwnershipDO> list = stockOwnershipService.getStockOwnershipPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "所有者库存.xls", "数据", WmsStockOwnershipRespVO.class,
                        BeanUtils.toBean(list, WmsStockOwnershipRespVO.class));
    }

}