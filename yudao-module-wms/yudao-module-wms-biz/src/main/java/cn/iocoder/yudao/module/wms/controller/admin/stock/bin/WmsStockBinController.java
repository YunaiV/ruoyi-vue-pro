package cn.iocoder.yudao.module.wms.controller.admin.stock.bin;

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

import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;

@Tag(name = "管理后台 - 仓位库存")
@RestController
@RequestMapping("/wms/stock-bin")
@Validated
public class WmsStockBinController {

    @Resource
    private WmsStockBinService stockBinService;

    @PostMapping("/create")
    @Operation(summary = "创建仓位库存")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:create')")
    public CommonResult<Long> createStockBin(@Valid @RequestBody WmsStockBinSaveReqVO createReqVO) {
        return success(stockBinService.createStockBin(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新仓位库存")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:update')")
    public CommonResult<Boolean> updateStockBin(@Valid @RequestBody WmsStockBinSaveReqVO updateReqVO) {
        stockBinService.updateStockBin(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除仓位库存")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:delete')")
    public CommonResult<Boolean> deleteStockBin(@RequestParam("id") Long id) {
        stockBinService.deleteStockBin(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得仓位库存")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<WmsStockBinRespVO> getStockBin(@RequestParam("id") Long id) {
        WmsStockBinDO stockBin = stockBinService.getStockBin(id);
        return success(BeanUtils.toBean(stockBin, WmsStockBinRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得仓位库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<PageResult<WmsStockBinRespVO>> getStockBinPage(@Valid WmsStockBinPageReqVO pageReqVO) {
        PageResult<WmsStockBinDO> pageResult = stockBinService.getStockBinPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockBinRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓位库存 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockBinExcel(@Valid WmsStockBinPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockBinDO> list = stockBinService.getStockBinPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "仓位库存.xls", "数据", WmsStockBinRespVO.class,
                        BeanUtils.toBean(list, WmsStockBinRespVO.class));
    }

}