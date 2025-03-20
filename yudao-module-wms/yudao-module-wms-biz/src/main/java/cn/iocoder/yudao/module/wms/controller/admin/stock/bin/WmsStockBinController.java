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
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "仓位库存")
@RestController
@RequestMapping("/wms/stock-bin")
@Validated
public class WmsStockBinController {

    @Resource
    private WmsStockBinService stockBinService;

    /**
     * @sign : 7472CDCA246B810A
     */
    @PostMapping("/create")
    @Operation(summary = "创建仓位库存")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:create')")
    public CommonResult<Long> createStockBin(@Valid @RequestBody WmsStockBinSaveReqVO createReqVO) {
        return success(stockBinService.createStockBin(createReqVO).getId());
    }

    /**
     * @sign : AC84893CE186DA40
     */
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

    /**
     * @sign : D7B68E7D4D845527
     */
    @GetMapping("/get")
    @Operation(summary = "获得仓位库存")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<WmsStockBinRespVO> getStockBin(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockBinDO stockBin = stockBinService.getStockBin(id);
        if (stockBin == null) {
            throw exception(STOCK_BIN_NOT_EXISTS);
        }
        // 转换
        WmsStockBinRespVO stockBinVO = BeanUtils.toBean(stockBin, WmsStockBinRespVO.class);
        // 返回
        return success(stockBinVO);
    }

    /**
     * @sign : 33164D4484F99F37
     */
    @GetMapping("/page")
    @Operation(summary = "获得仓位库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<PageResult<WmsStockBinRespVO>> getStockBinPage(@Valid WmsStockBinPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockBinDO> doPageResult = stockBinService.getStockBinPage(pageReqVO);
        // 转换
        PageResult<WmsStockBinRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockBinRespVO.class);
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓位库存 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockBinExcel(@Valid WmsStockBinPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockBinDO> list = stockBinService.getStockBinPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "仓位库存.xls", "数据", WmsStockBinRespVO.class, BeanUtils.toBean(list, WmsStockBinRespVO.class));
    }
}