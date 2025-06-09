package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.WmsStockLogicMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.logic.move.WmsStockLogicMoveService;
import cn.iocoder.yudao.module.wms.service.stock.logic.move.item.WmsStockLogicMoveItemService;
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

/**
 * @author jisencai
 */
@Tag(name = "逻辑库存移动")
@RestController
@RequestMapping("/wms/stock-logic-move")
@Validated
public class WmsStockLogicMoveController {

    @Resource()
    @Lazy()
    private WmsStockLogicMoveItemService stockLogicMoveItemService;

    @Resource
    private WmsStockLogicMoveService stockLogicMoveService;

    /**
     * @sign : E50BC63A85635F27
     */
    @PostMapping("/create")
    @Operation(summary = "创建逻辑库存移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:create')")
    public CommonResult<Long> createStockLogicMove(@Valid @RequestBody WmsStockLogicMoveSaveReqVO createReqVO) {
        return success(stockLogicMoveService.createStockLogicMove(createReqVO).getId());
    }

    // /**
    // * @sign : B17AAF1E8A33881D
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新逻辑库存移动")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:update')")
    // public CommonResult<Boolean> updateStockLogicMove(@Valid @RequestBody WmsStockLogicMoveSaveReqVO updateReqVO) {
    // stockLogicMoveService.updateStockLogicMove(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除逻辑库存移动")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:delete')")
    // public CommonResult<Boolean> deleteStockLogicMove(@RequestParam("id") Long id) {
    // stockLogicMoveService.deleteStockLogicMove(id);
    // return success(true);
    // }

    /**
     * @sign : B7406A1F19B24A11
     */
    @GetMapping("/get")
    @Operation(summary = "获得逻辑库存移动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:query')")
    public CommonResult<WmsStockLogicMoveRespVO> getStockLogicMove(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockLogicMoveDO stockLogicMove = stockLogicMoveService.getStockLogicMove(id);
        if (stockLogicMove == null) {
            throw exception(STOCK_LOGIC_MOVE_NOT_EXISTS);
        }
        // 转换
        WmsStockLogicMoveRespVO stockLogicMoveVO = BeanUtils.toBean(stockLogicMove, WmsStockLogicMoveRespVO.class);
        // 组装逻辑库存移动详情
        List<WmsStockLogicMoveItemDO> stockLogicMoveItemList = stockLogicMoveItemService.selectByLogicMoveId(stockLogicMoveVO.getId());
        stockLogicMoveVO.setItemList(BeanUtils.toBean(stockLogicMoveItemList, WmsStockLogicMoveItemRespVO.class));
        // 组装
        stockLogicMoveService.assembleWarehouse(List.of(stockLogicMoveVO));
        stockLogicMoveItemService.assembleProduct(stockLogicMoveVO.getItemList());
        stockLogicMoveItemService.assembleCompanyAndDept(stockLogicMoveVO.getItemList());

        // 返回
        return success(stockLogicMoveVO);
    }

    /**
     * @sign : 586BDA157BC07B30
     */
    @GetMapping("/page")
    @Operation(summary = "获得逻辑库存移动分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:query')")
    public CommonResult<PageResult<WmsStockLogicMoveRespVO>> getStockLogicMovePage(@Valid WmsStockLogicMovePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockLogicMoveDO> doPageResult = stockLogicMoveService.getStockLogicMovePage(pageReqVO);
        // 转换
        PageResult<WmsStockLogicMoveRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockLogicMoveRespVO.class);
        // 组装
        stockLogicMoveService.assembleWarehouse(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出逻辑库存移动 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockLogicMoveExcel(@Valid WmsStockLogicMovePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockLogicMoveDO> list = stockLogicMoveService.getStockLogicMovePage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "逻辑库存移动.xls", "数据", WmsStockLogicMoveRespVO.class, BeanUtils.toBean(list, WmsStockLogicMoveRespVO.class));
    // }


    @PostMapping("/import-excel")
    @Operation(summary = "导入产品归属移动清单")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:import')")
    public CommonResult<Boolean> importExcel(@Valid WmsStockLogicMoveImportVO importReqVO) throws Exception {
        //
        List<WmsStockLogicMoveImportExcelVO> impVOList = ExcelUtils.read(importReqVO.getFile(), WmsStockLogicMoveImportExcelVO.class);
        // 识别代码
        stockLogicMoveItemService.assembleWarehouseForImp(impVOList);
        stockLogicMoveItemService.assembleCompanyAndDeptForImp(impVOList);
        stockLogicMoveItemService.assembleProductForImp(impVOList);

        // 校验仓库ID
        Set<Long> warehouseIds = StreamX.from(impVOList).filter(Objects::nonNull).toSet(WmsStockLogicMoveImportExcelVO::getWarehouseId);
        if (warehouseIds.size() != 1) {
            throw exception(STOCK_LOGIC_MOVE_SINGLE_WAREHOUSE_ALLOW);
        }
        // 校验数据有效性
        for (WmsStockLogicMoveImportExcelVO excelVO : impVOList) {
            if (excelVO.getToCompanyId() == null) {
                throw exception(STOCK_LOGIC_MOVE_ITEM_TO_COMPANY_ERROR);
            }
            if (excelVO.getToDeptId() == null) {
                throw exception(STOCK_LOGIC_MOVE_ITEM_FROM_DEPT_ERROR);
            }
            if (excelVO.getFromCompanyId() == null) {
                throw exception(STOCK_LOGIC_MOVE_ITEM_FROM_COMPANY_ERROR);
            }
            if (excelVO.getFromDeptId() == null) {
                throw exception(STOCK_LOGIC_MOVE_ITEM_FROM_DEPT_ERROR);
            }
            if (excelVO.getProductId() == null) {
                throw exception(STOCK_LOGIC_MOVE_ITEM_PRODUCT_ERROR, excelVO.getProductCode());
            }
        }

        WmsStockLogicMoveSaveReqVO saveReqVO = new WmsStockLogicMoveSaveReqVO();
        saveReqVO.setWarehouseId(warehouseIds.iterator().next());

        saveReqVO.setItemList(BeanUtils.toBean(impVOList, WmsStockLogicMoveItemSaveReqVO.class));

        stockLogicMoveService.createStockLogicMove(saveReqVO);

        return success(true);
    }

    @GetMapping("/download-template")
    @Operation(summary = "下载模板 库存归属")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic-move:download-template')")
    public ResponseEntity<byte[]> downloadExcelTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/logic-move-import.xlsx");
        byte[] fileContent;
        try (InputStream inputStream = resource.getInputStream()) {
            fileContent = inputStream.readAllBytes();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        // 设置文件名
        String fileName = "库存归属模板.xlsx";
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
            .headers(headers)
            .body(fileContent);
    }

}
