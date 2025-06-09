package cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin.WmsStockCheckBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stockcheck.WmsStockCheckService;
import cn.iocoder.yudao.module.wms.service.stockcheck.bin.WmsStockCheckBinService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import de.danielbechler.util.Collections;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * @author jisencai
 */
@Tag(name = "库位盘点")
@RestController
@RequestMapping("/wms/stock-check-bin")
@Validated
public class WmsStockCheckBinController {

    @Resource
    private WmsStockCheckBinService stockCheckBinService;

    @Resource
    private WmsStockCheckService stockCheckService;

    @Resource
    private WmsWarehouseBinService warehouseBinService;

    @Resource
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsStockBinService stockBinService;

    @Resource
    private ErpProductApi productApi;

    // /**
    // * @sign : ACAABD7462DAD982
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建库位盘点")
    // @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:create')")
    // public CommonResult<Long> createStockCheckBin(@Valid @RequestBody WmsStockCheckBinSaveReqVO createReqVO) {
    // return success(stockCheckBinService.createStockCheckBin(createReqVO).getId());
    // }
    @PostMapping("/append")
    @Operation(summary = "追加盘点库位")
    @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:append')")
    public CommonResult<Boolean> appendStockCheckBin(@Valid @RequestBody List<WmsStockCheckBinSaveReqVO> createReqVOList) {
        if (Collections.isEmpty(createReqVOList)) {
            throw exception(STOCKCHECK_BIN_NOT_EXISTS);
        }
        return success(stockCheckBinService.appendStockCheckBin(createReqVOList));
    }

    // 
    // /**
    // * @sign : 75B7A098990ADFC4
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新库位盘点")
    // @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:update')")
    // public CommonResult<Boolean> updateStockCheckBin(@Valid @RequestBody WmsStockCheckBinSaveReqVO updateReqVO) {
    // stockCheckBinService.updateStockCheckBin(updateReqVO);
    // return success(true);
    // }
    @PutMapping("/update-actual-quantity")
    @Operation(summary = "设置实际库存量")
    @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:update')")
    public CommonResult<Boolean> updateActualQuantity(@Validated(ValidationGroup.update.class) @RequestBody List<WmsStockCheckBinSaveReqVO> updateReqVOList) {
        stockCheckBinService.updateActualQuantity(updateReqVOList);
        return success(true);
    }

    // 
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除库位盘点")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:delete')")
    // public CommonResult<Boolean> deleteStockCheckBin(@RequestParam("id") Long id) {
    // stockCheckBinService.deleteStockCheckBin(id);
    // return success(true);
    // }
    // 
    // /**
    // * @sign : 48F08E170F254296
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得库位盘点")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:query')")
    // public CommonResult<WmsStockCheckBinRespVO> getStockCheckBin(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsStockCheckBinDO stockCheckBin = stockCheckBinService.getStockCheckBin(id);
    // if (stockCheckBin == null) {
    // throw exception(STOCKCHECK_BIN_NOT_EXISTS);
    // }
    // // 转换
    // WmsStockCheckBinRespVO stockCheckBinVO = BeanUtils.toBean(stockCheckBin, WmsStockCheckBinRespVO.class);
    // // 返回
    // return success(stockCheckBinVO);
    // }
    // 
    // /**
    // * @sign : E02F374AE9A85D01
    // */
    // @GetMapping("/page")
    // @Operation(summary = "获得库位盘点分页")
    // @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:query')")
    // public CommonResult<PageResult<WmsStockCheckBinRespVO>> getStockCheckBinPage(@Valid WmsStockCheckBinPageReqVO pageReqVO) {
    // // 查询数据
    // PageResult<WmsStockCheckBinDO> doPageResult = stockCheckBinService.getStockCheckBinPage(pageReqVO);
    // // 转换
    // PageResult<WmsStockCheckBinRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockCheckBinRespVO.class);
    // // 返回
    // return success(voPageResult);
    // }
    // 
    @GetMapping("/export-excel")
    @Operation(summary = "导出库位盘点 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stockCheck-bin:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockCheckBinExcel(@Valid WmsStockCheckBinPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        WmsStockCheckDO stockCheck = stockCheckService.validateStockCheckExists(pageReqVO.getStockCheckId());
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockCheckBinDO> doList = stockCheckBinService.getStockCheckBinPage(pageReqVO).getList();
        List<WmsStockCheckBinRespVO> voList = BeanUtils.toBean(doList, WmsStockCheckBinRespVO.class);
        stockCheckBinService.assembleBin(voList);
        stockCheckBinService.assembleProduct(voList);
        Map<Long, WmsStockCheckBinRespVO> voMap = StreamX.from(voList).toMap(WmsStockCheckBinRespVO::getId);
        List<WmsStockCheckBinExcelVO> exVOList = BeanUtils.toBean(voList, WmsStockCheckBinExcelVO.class);
        for (WmsStockCheckBinExcelVO excelVO : exVOList) {
            WmsStockCheckBinRespVO itemRespVO = voMap.get(excelVO.getId());
            if (itemRespVO == null) {
                continue;
            }
            if (itemRespVO.getProduct() != null) {
                excelVO.setProductCode(itemRespVO.getProduct().getCode());
            }
            // 
            excelVO.setStockCheckCode(stockCheck.getCode());
            if (itemRespVO.getBin() != null) {
                excelVO.setBinCode(itemRespVO.getBin().getCode());
                excelVO.setBinName(itemRespVO.getBin().getName());
            }
        }
        // 导出 Excel
        ExcelUtils.write(response, "库位盘点-" + stockCheck.getCode() + ".xls", "数据", WmsStockCheckBinExcelVO.class, BeanUtils.toBean(exVOList, WmsStockCheckBinExcelVO.class));
    }

    @PostMapping("/parse-product-bin")
    @Operation(summary = "产品库位转换")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:parse-product-bin')")
    public CommonResult<List<WmsStockBinRespVO>> importProductExcel(@Valid WmsStockCheckProductParseVO importReqVO) throws Exception {
        WmsWarehouseDO wmsWarehouseDO = warehouseService.validateWarehouseExists(importReqVO.getWarehouseId());
        // 读取数据
        List<WmsStockCheckProductExcelVO> impVOList = ExcelUtils.read(importReqVO.getFile(), WmsStockCheckProductExcelVO.class);

        List<WmsStockBinRespVO> stockCheckBinRespVOList = stockCheckService.parseProductExcel(wmsWarehouseDO, impVOList);

        return success(stockCheckBinRespVOList);
    }

    @GetMapping("/download-product-template")
    @Operation(summary = "下载模板 盘点导入产品转换")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:download-product-template')")
    public ResponseEntity<byte[]> downloadProductExcelTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/stockCheck-product-import.xlsx");
            byte[] fileContent;
            try (InputStream inputStream = resource.getInputStream()) {
                fileContent = inputStream.readAllBytes();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            // 设置文件名
            String fileName = "盘点导入产品转换.xlsx";
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"");

            return ResponseEntity.ok()
                        .headers(headers)
                        .body(fileContent);
    }


    @PostMapping("/import-excel")
    @Operation(summary = "导入盘点结果")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:import')")
    public CommonResult<List<WmsStockCheckBinRespVO>> importExcel(@Valid WmsStockCheckBinImportVO importReqVO) throws Exception {
        WmsStockCheckDO stockCheck = stockCheckService.validateStockCheckExists(importReqVO.getStockCheckId());
        WmsStockCheckAuditStatus stockCheckAuditStatus = WmsStockCheckAuditStatus.parse(stockCheck.getAuditStatus());
        // 不允许导入
        if (stockCheckAuditStatus != WmsStockCheckAuditStatus.AUDITING) {
            throw exception(STOCKCHECK_BIN_CAN_NOT_IMPORT);
        }
        // 读取数据
        List<WmsStockCheckBinExcelVO> impVOList = ExcelUtils.read(importReqVO.getFile(), WmsStockCheckBinExcelVO.class);
        // 准备装配仓位ID和产品ID
        List<WmsWarehouseBinDO> binList = warehouseBinService.selectByCodes(StreamX.from(impVOList).toSet(WmsStockCheckBinExcelVO::getBinCode));
        Map<String, WmsWarehouseBinDO> binMap = StreamX.from(binList).toMap(WmsWarehouseBinDO::getCode);
        Map<String, ErpProductDTO> productMap = productApi.getProductMapByCode(StreamX.from(impVOList).toSet(WmsStockCheckBinExcelVO::getProductCode));
        // 装配仓位ID和产品ID
        for (WmsStockCheckBinExcelVO excelVO : impVOList) {
            WmsWarehouseBinDO bin = binMap.get(excelVO.getBinCode());
            if (bin != null) {
                excelVO.setBinId(bin.getId());
            }
            ErpProductDTO product = productMap.get(excelVO.getProductCode());
            if (product != null) {
                excelVO.setProductId(product.getId());
            }
        }
        List<WmsStockCheckBinRespVO> voList = BeanUtils.toBean(impVOList, WmsStockCheckBinRespVO.class);
        // 装配产品ID
        stockCheckBinService.assembleBin(voList);
        stockCheckBinService.assembleProduct(voList);
        // 检查仓位与产品的有效性
        for (int i = 0; i < voList.size(); i++) {
            WmsStockCheckBinRespVO stockCheckBinRespVO = voList.get(i);
            WmsStockCheckBinExcelVO excelVO = impVOList.get(i);
            if (stockCheckBinRespVO.getBin() == null) {
                throw exception(STOCKCHECK_BIN_BIN_NOT_EXISTS, excelVO.getBinCode());
            }
            if (stockCheckBinRespVO.getProduct() == null) {
                throw exception(STOCKCHECK_BIN_PRODUCT_NOT_EXISTS, excelVO.getProductCode());
            }
        }
        // 检查仓位数据的合规性
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByCodes(StreamX.from(impVOList).toSet(WmsStockCheckBinExcelVO::getBinCode));
        Set<Long> warehouseIdSet = StreamX.from(binDOList).toSet(WmsWarehouseBinDO::getWarehouseId);
        if (warehouseIdSet.size() != 1) {
            throw exception(STOCKCHECK_BIN_WAREHOUSE_BIN_ERROR);
        }
        Long warehouseId = StreamX.from(warehouseIdSet).first();
        if (!Objects.equals(warehouseId, stockCheck.getWarehouseId())) {
            throw exception(STOCKCHECK_BIN_WAREHOUSE_BIN_NOT_MATCH);
        }

        List<WmsWarehouseProductVO> wmsWarehouseProductVOList = new ArrayList<>();

        Map<String, WmsStockCheckBinExcelVO> imMap = StreamX.from(impVOList).toMap(e -> e.getBinId() + "-" + e.getProductId());
        // 转 DOList 去保存
        List<WmsStockCheckBinDO> dosInDB = stockCheckBinService.selectByStockCheckId(stockCheck.getId());
        for (WmsStockCheckBinDO stockCheckBinDO : dosInDB) {
            WmsStockCheckBinExcelVO stockCheckBinExcelVO = imMap.get(stockCheckBinDO.getBinId() + "-" + stockCheckBinDO.getProductId());
            if (stockCheckBinExcelVO != null) {
                stockCheckBinDO.setActualQty(stockCheckBinExcelVO.getActualQty());
                stockCheckBinDO.setRemark(stockCheckBinExcelVO.getRemark());
            }
            wmsWarehouseProductVOList.add(WmsWarehouseProductVO.builder().warehouseId(stockCheck.getWarehouseId()).productId(stockCheckBinDO.getProductId()).build());
        }

        Map<String, WmsStockCheckBinDO> map = StreamX.from(dosInDB).toMap(e -> e.getBinId() + "-" + e.getProductId());
        for (WmsStockCheckBinExcelVO stockCheckBinExcelVO : impVOList) {
            WmsStockCheckBinDO stockCheckBinDO = map.get(stockCheckBinExcelVO.getBinId() + "-" + stockCheckBinExcelVO.getProductId());
            if (stockCheckBinDO == null) {
                dosInDB.add(BeanUtils.toBean(stockCheckBinExcelVO, WmsStockCheckBinDO.class));
                wmsWarehouseProductVOList.add(WmsWarehouseProductVO.builder().warehouseId(stockCheck.getWarehouseId()).productId(stockCheckBinExcelVO.getProductId()).build());
            }
        }

        List<WmsStockBinRespVO> stockBinList = stockBinService.selectStockBinList(wmsWarehouseProductVOList, false);
        Map<String, WmsStockBinRespVO> stockBinMap = StreamX.from(stockBinList).toMap(e -> e.getBinId()+"-"+e.getProductId());

        for (WmsStockCheckBinDO stockCheckBinDO : dosInDB) {
            WmsStockBinRespVO stockBinDO = stockBinMap.get(stockCheckBinDO.getBinId() + "-" + stockCheckBinDO.getProductId());
            if(stockBinDO==null) {
                stockCheckBinDO.setExpectedQty(0);
            } else {
                stockCheckBinDO.setExpectedQty(stockBinDO.getAvailableQty());
            }
        }

        List<WmsStockCheckBinRespVO> stockCheckBinRespVOS = BeanUtils.toBean(dosInDB, WmsStockCheckBinRespVO.class);
        stockCheckBinService.assembleProduct(stockCheckBinRespVOS);
        stockCheckBinService.assembleBin(stockCheckBinRespVOS);
        // stockCheckBinService.saveStockCheckBinList(stockCheck, dosInDB);
        return success(stockCheckBinRespVOS);
    }

    @GetMapping("/download-template")
    @Operation(summary = "下载模板 盘点结果")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:download-template')")
    public ResponseEntity<byte[]> downloadExcelTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/import-item-result.xlsx");
        byte[] fileContent;
        try (InputStream inputStream = resource.getInputStream()) {
            fileContent = inputStream.readAllBytes();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        // 设置文件名
        String fileName = "盘点结果模板.xlsx";
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }
}
