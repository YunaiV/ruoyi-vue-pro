package cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move;

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

import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.WmsStockOwnershiopMoveDO;
import cn.iocoder.yudao.module.wms.service.stock.ownershiop.move.WmsStockOwnershiopMoveService;

@Tag(name = "管理后台 - 所有者库存移动")
@RestController
@RequestMapping("/wms/stock-ownershiop-move")
@Validated
public class WmsStockOwnershiopMoveController {

    @Resource
    private WmsStockOwnershiopMoveService stockOwnershiopMoveService;

    @PostMapping("/create")
    @Operation(summary = "创建所有者库存移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move:create')")
    public CommonResult<Long> createStockOwnershiopMove(@Valid @RequestBody WmsStockOwnershiopMoveSaveReqVO createReqVO) {
        return success(stockOwnershiopMoveService.createStockOwnershiopMove(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新所有者库存移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move:update')")
    public CommonResult<Boolean> updateStockOwnershiopMove(@Valid @RequestBody WmsStockOwnershiopMoveSaveReqVO updateReqVO) {
        stockOwnershiopMoveService.updateStockOwnershiopMove(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除所有者库存移动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move:delete')")
    public CommonResult<Boolean> deleteStockOwnershiopMove(@RequestParam("id") Long id) {
        stockOwnershiopMoveService.deleteStockOwnershiopMove(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得所有者库存移动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move:query')")
    public CommonResult<WmsStockOwnershiopMoveRespVO> getStockOwnershiopMove(@RequestParam("id") Long id) {
        WmsStockOwnershiopMoveDO stockOwnershiopMove = stockOwnershiopMoveService.getStockOwnershiopMove(id);
        return success(BeanUtils.toBean(stockOwnershiopMove, WmsStockOwnershiopMoveRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得所有者库存移动分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move:query')")
    public CommonResult<PageResult<WmsStockOwnershiopMoveRespVO>> getStockOwnershiopMovePage(@Valid WmsStockOwnershiopMovePageReqVO pageReqVO) {
        PageResult<WmsStockOwnershiopMoveDO> pageResult = stockOwnershiopMoveService.getStockOwnershiopMovePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockOwnershiopMoveRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出所有者库存移动 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockOwnershiopMoveExcel(@Valid WmsStockOwnershiopMovePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockOwnershiopMoveDO> list = stockOwnershiopMoveService.getStockOwnershiopMovePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "所有者库存移动.xls", "数据", WmsStockOwnershiopMoveRespVO.class,
                        BeanUtils.toBean(list, WmsStockOwnershiopMoveRespVO.class));
    }

}