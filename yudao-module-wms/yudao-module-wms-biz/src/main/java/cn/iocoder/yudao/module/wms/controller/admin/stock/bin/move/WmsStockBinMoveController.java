package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.item.WmsStockBinMoveItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

@Tag(name = "库位移动")
@RestController
@RequestMapping("/wms/stock-bin-move")
@Validated
public class WmsStockBinMoveController {

    @Resource()
    @Lazy()
    private WmsStockBinMoveItemService stockBinMoveItemService;

    @Resource
    private WmsStockBinMoveService stockBinMoveService;

    /**
     * @sign : FDBDD7D2FB7319B5
     */
    @PostMapping("/create")
    @Operation(summary = "创建库位移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:create')")
    public CommonResult<Long> createStockBinMove(@Valid @RequestBody WmsStockBinMoveSaveReqVO createReqVO) {
        return success(stockBinMoveService.createStockBinMove(createReqVO).getId());
    }

    // /**
    // * @sign : 5785B61F8D831D77
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新库位移动")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:update')")
    // public CommonResult<Boolean> updateStockBinMove(@Valid @RequestBody WmsStockBinMoveSaveReqVO updateReqVO) {
    // stockBinMoveService.updateStockBinMove(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除库位移动")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:delete')")
    // public CommonResult<Boolean> deleteStockBinMove(@RequestParam("id") Long id) {
    // stockBinMoveService.deleteStockBinMove(id);
    // return success(true);
    // }
    /**
     * @sign : 51C7A99B4BB4E361
     */
    @GetMapping("/get")
    @Operation(summary = "获得库位移动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:query')")
    public CommonResult<WmsStockBinMoveRespVO> getStockBinMove(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockBinMoveDO stockBinMove = stockBinMoveService.getStockBinMove(id);
        if (stockBinMove == null) {
            throw exception(STOCK_BIN_MOVE_NOT_EXISTS);
        }
        // 转换
        WmsStockBinMoveRespVO stockBinMoveVO = BeanUtils.toBean(stockBinMove, WmsStockBinMoveRespVO.class);
        // 组装库位移动详情
        List<WmsStockBinMoveItemDO> stockBinMoveItemList = stockBinMoveItemService.selectByBinMoveId(stockBinMoveVO.getId());
        stockBinMoveVO.setItemList(BeanUtils.toBean(stockBinMoveItemList, WmsStockBinMoveItemRespVO.class));
        stockBinMoveService.assembleWarehouse(List.of(stockBinMoveVO));
        stockBinMoveItemService.assembleBin(stockBinMoveVO.getItemList());
        stockBinMoveItemService.assembleProduct(stockBinMoveVO.getItemList());
        // 返回
        return success(stockBinMoveVO);
    }

    /**
     * @sign : 4A344623E27B783A
     */
    @GetMapping("/page")
    @Operation(summary = "获得库位移动分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:query')")
    public CommonResult<PageResult<WmsStockBinMoveRespVO>> getStockBinMovePage(@Valid WmsStockBinMovePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockBinMoveDO> doPageResult = stockBinMoveService.getStockBinMovePage(pageReqVO);
        // 转换
        PageResult<WmsStockBinMoveRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockBinMoveRespVO.class);
        stockBinMoveService.assembleWarehouse(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }

    // @GetMapping("/export-excel")
    // @Operation(summary = "导出库位移动 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockBinMoveExcel(@Valid WmsStockBinMovePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockBinMoveDO> list = stockBinMoveService.getStockBinMovePage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "库位移动.xls", "数据", WmsStockBinMoveRespVO.class, BeanUtils.toBean(list, WmsStockBinMoveRespVO.class));
    // }
    @PostMapping("/import-excel")
    @Operation(summary = "导入产品库位移动清单")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:import')")
    public CommonResult<Boolean> importExcel(@Valid @RequestBody WmsStockBinMoveImportVO importReqVO) throws Exception {
        // 
        List<WmsStockBinMoveImportExcelVO> impVOList = ExcelUtils.read(importReqVO.getFile(), WmsStockBinMoveImportExcelVO.class);
        // 识别代码
        stockBinMoveItemService.assembleWarehouseForImp(impVOList);
        stockBinMoveItemService.assembleBinForImp(impVOList);
        stockBinMoveItemService.assembleProductForImp(impVOList);
        Set<Long> warehouseIds = StreamX.from(impVOList).filter(Objects::nonNull).toSet(WmsStockBinMoveImportExcelVO::getWarehouseId);
        if (warehouseIds.size() != 1) {
            throw exception(STOCK_BIN_MOVE_SINGLE_WAREHOUSE_ALLOW);
        }
        for (WmsStockBinMoveImportExcelVO excelVO : impVOList) {
            if (excelVO.getToBinId() == null) {
                throw exception(STOCK_BIN_MOVE_ITEM_TO_BIN_ERROR);
            }
            if (excelVO.getFromBinId() == null) {
                throw exception(STOCK_BIN_MOVE_ITEM_FROM_BIN_ERROR);
            }
            if (excelVO.getProductId() == null) {
                throw exception(STOCK_BIN_MOVE_ITEM_PRODUCT_ERROR, excelVO.getProductCode());
            }
        }
        WmsStockBinMoveSaveReqVO saveReqVO = new WmsStockBinMoveSaveReqVO();
        saveReqVO.setWarehouseId(warehouseIds.iterator().next());
        saveReqVO.setItemList(BeanUtils.toBean(impVOList, WmsStockBinMoveItemSaveReqVO.class));
        stockBinMoveService.createStockBinMove(saveReqVO);
        return success(true);
    }

    @GetMapping("/download-template")
    @Operation(summary = "下载模板 批量库位")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:download-template')")
    public ResponseEntity<byte[]> downloadExcelTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/stockCheck-bin-import.xlsx");
        byte[] fileContent;
        try (InputStream inputStream = resource.getInputStream()) {
            fileContent = inputStream.readAllBytes();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        // 设置文件名
        String fileName = "批量库位模板.xlsx";
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }
}
