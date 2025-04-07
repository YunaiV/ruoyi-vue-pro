package cn.iocoder.yudao.module.wms.controller.admin.inventory.result;

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

import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.WmsInventoryResultDO;
import cn.iocoder.yudao.module.wms.service.inventory.result.WmsInventoryResultService;

@Tag(name = "管理后台 - 库存盘点结果")
@RestController
@RequestMapping("/wms/inventory-result")
@Validated
public class WmsInventoryResultController {

    @Resource
    private WmsInventoryResultService inventoryResultService;

    @PostMapping("/create")
    @Operation(summary = "创建库存盘点结果")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result:create')")
    public CommonResult<Long> createInventoryResult(@Valid @RequestBody WmsInventoryResultSaveReqVO createReqVO) {
        return success(inventoryResultService.createInventoryResult(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存盘点结果")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result:update')")
    public CommonResult<Boolean> updateInventoryResult(@Valid @RequestBody WmsInventoryResultSaveReqVO updateReqVO) {
        inventoryResultService.updateInventoryResult(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存盘点结果")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inventory-result:delete')")
    public CommonResult<Boolean> deleteInventoryResult(@RequestParam("id") Long id) {
        inventoryResultService.deleteInventoryResult(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存盘点结果")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result:query')")
    public CommonResult<WmsInventoryResultRespVO> getInventoryResult(@RequestParam("id") Long id) {
        WmsInventoryResultDO inventoryResult = inventoryResultService.getInventoryResult(id);
        return success(BeanUtils.toBean(inventoryResult, WmsInventoryResultRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存盘点结果分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result:query')")
    public CommonResult<PageResult<WmsInventoryResultRespVO>> getInventoryResultPage(@Valid WmsInventoryResultPageReqVO pageReqVO) {
        PageResult<WmsInventoryResultDO> pageResult = inventoryResultService.getInventoryResultPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInventoryResultRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存盘点结果 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInventoryResultExcel(@Valid WmsInventoryResultPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInventoryResultDO> list = inventoryResultService.getInventoryResultPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存盘点结果.xls", "数据", WmsInventoryResultRespVO.class,
                        BeanUtils.toBean(list, WmsInventoryResultRespVO.class));
    }

}