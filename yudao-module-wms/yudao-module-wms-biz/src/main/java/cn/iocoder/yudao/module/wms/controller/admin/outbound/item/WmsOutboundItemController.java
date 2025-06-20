package cn.iocoder.yudao.module.wms.controller.admin.outbound.item;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.*;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.outbound.item.WmsOutboundItemService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

@Tag(name = "出库单详情")
@RestController
@RequestMapping("/wms/outbound-item")
@Validated
public class WmsOutboundItemController {

    @Resource
    private WmsOutboundItemService outboundItemService;

    @Resource
    private WmsOutboundService outboundService;

    @Resource
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsWarehouseBinService warehouseBinService;

    // /**
    // * @sign : C28B99A86B641037
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建出库单详情")
    // @PreAuthorize("@ss.hasPermission('wms:outbound-item:create')")
    // public CommonResult<Long> createOutboundItem(@Validated(ValidationGroup.create.class) @RequestBody WmsOutboundItemSaveReqVO createReqVO) {
    // return success(outboundItemService.createOutboundItem(createReqVO).getId());
    // }
    // 
    // /**
    // * @sign : 4D88C9EDC345F949
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新出库单详情")
    // @PreAuthorize("@ss.hasPermission('wms:outbound-item:update')")
    // public CommonResult<Boolean> updateOutboundItem(@Validated(ValidationGroup.update.class) @RequestBody WmsOutboundItemSaveReqVO updateReqVO) {
    // outboundItemService.updateOutboundItem(updateReqVO);
    // return success(true);
    // }
    // 
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除出库单详情")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:outbound-item:delete')")
    // public CommonResult<Boolean> deleteOutboundItem(@RequestParam("id") Long id) {
    // outboundItemService.deleteOutboundItem(id);
    // return success(true);
    // }
    // 
    // /**
    // * @sign : 726EEB89E54A0751
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得出库单详情")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:outbound-item:query')")
    // public CommonResult<WmsOutboundItemRespVO> getOutboundItem(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsOutboundItemDO outboundItem = outboundItemService.getOutboundItem(id);
    // if (outboundItem == null) {
    // throw exception(OUTBOUND_ITEM_NOT_EXISTS);
    // }
    // // 转换
    // WmsOutboundItemRespVO outboundItemVO = BeanUtils.toBean(outboundItem, WmsOutboundItemRespVO.class);
    // // 人员姓名填充
    // AdminUserApi.inst().prepareFill(List.of(outboundItemVO))
    // .mapping(WmsOutboundItemRespVO::getCreator, WmsOutboundItemRespVO::setCreatorName)
    // .mapping(WmsOutboundItemRespVO::getCreator, WmsOutboundItemRespVO::setUpdaterName)
    // .fill();
    // // 返回
    // return success(outboundItemVO);
    // }
    // 
    // /**
    // * @sign : 9E5094837074BDD8
    // */
    // @GetMapping("/page")
    // @Operation(summary = "获得出库单详情分页")
    // @PreAuthorize("@ss.hasPermission('wms:outbound-item:query')")
    // public CommonResult<PageResult<WmsOutboundItemRespVO>> getOutboundItemPage(@Valid WmsOutboundItemPageReqVO pageReqVO) {
    // // 查询数据
    // PageResult<WmsOutboundItemDO> doPageResult = outboundItemService.getOutboundItemPage(pageReqVO);
    // // 转换
    // PageResult<WmsOutboundItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsOutboundItemRespVO.class);
    // // 人员姓名填充
    // AdminUserApi.inst().prepareFill(voPageResult.getList())
    // .mapping(WmsOutboundItemRespVO::getCreator, WmsOutboundItemRespVO::setCreatorName)
    // .mapping(WmsOutboundItemRespVO::getCreator, WmsOutboundItemRespVO::setUpdaterName)
    // .fill();
    // // 返回
    // return success(voPageResult);
    // }
    //


    @GetMapping("/export-excel")
    @Operation(summary = "导出出库单详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutboundItemExcel(@Valid WmsOutboundItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);

        WmsOutboundDO outboundDO = outboundService.getOutbound(pageReqVO.getOutboundId());
        WmsWarehouseDO warehouse = warehouseService.getWarehouse(outboundDO.getWarehouseId());

        List<WmsOutboundItemDO> doList = outboundItemService.getOutboundItemPage(pageReqVO).getList();

        List<WmsOutboundItemRespVO> voList = BeanUtils.toBean(doList, WmsOutboundItemRespVO.class);
        Map<Long, WmsOutboundItemRespVO> voMap = StreamX.from(voList).toMap(WmsOutboundItemRespVO::getId);
        //
        outboundItemService.assembleProducts(voList);
        outboundItemService.assembleBin(voList);
        outboundItemService.assembleDept(voList);
        outboundItemService.assembleCompany(voList);


        List<WmsOutboundItemExportVO> expVoList = BeanUtils.toBean(voList, WmsOutboundItemExportVO.class);
        for (WmsOutboundItemExportVO exportVO : expVoList) {
            WmsOutboundItemRespVO itemRespVO = voMap.get(exportVO.getId());
            if(itemRespVO==null) {
                continue;
            }
            if (itemRespVO.getProduct() != null) {
                exportVO.setProductCode(itemRespVO.getProduct().getCode());
            }
            exportVO.setWarehouseName(warehouse.getName());
            exportVO.setOutboundCode(outboundDO.getCode());
            WmsOutboundStatus outboundStatus = WmsOutboundStatus.parse(itemRespVO.getOutboundStatus());
            exportVO.setOutboundStatusLabel(outboundStatus.getLabel());
            exportVO.setBinName(itemRespVO.getBin().getName());
            exportVO.setDeptName(itemRespVO.getDept().getName());
            exportVO.setCompanyName(itemRespVO.getCompany().getName());
        }

        // 导出 Excel
        ExcelUtils.write(response, "出库单详情-"+outboundDO.getCode()+".xls", "数据", WmsOutboundItemExportVO.class,expVoList);
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入详情")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:import')")
    public CommonResult<Boolean> importExcel(@Valid WmsOutboundItemImportVO importReqVO) throws Exception {

        WmsOutboundDO outboundDO = outboundService.validateOutboundExists(importReqVO.getOutboundId());

        List<WmsOutboundItemImportExcelVO> impVOList = ExcelUtils.read(importReqVO.getFile(), WmsOutboundItemImportExcelVO.class);
        // 装配产品ID
        outboundItemService.assembleProductIds(impVOList);
        for (WmsOutboundItemImportExcelVO importExcelVO : impVOList) {
            if (importExcelVO.getProductId() == null) {
                throw exception(PRODUCT_NOT_EXISTS, importExcelVO.getProductCode());
            }
        }


        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByCodes(StreamX.from(impVOList).toSet(WmsOutboundItemImportExcelVO::getBinCode));
        Set<Long> warehouseIdSet = StreamX.from(binDOList).toSet(WmsWarehouseBinDO::getWarehouseId);
        if(warehouseIdSet.size()!=1) {
            throw exception(OUTBOUND_ITEM_WAREHOUSE_BIN_ERROR);
        }
        Long warehouseId = StreamX.from(warehouseIdSet).first();
        if(!Objects.equals(warehouseId,outboundDO.getWarehouseId())) {
            throw exception(OUTBOUND_ITEM_WAREHOUSE_BIN_NOT_MATCH);
        }
        Map<String, WmsWarehouseBinDO> binMap = StreamX.from(binDOList).toMap(WmsWarehouseBinDO::getCode);
        StreamX.from(impVOList).assemble(binMap, WmsOutboundItemImportExcelVO::getBinCode,(ex,bin)->{
            ex.setBinId(bin.getId());
        });


        List<WmsOutboundItemSaveReqVO> saveReqVOList = BeanUtils.toBean(impVOList, WmsOutboundItemSaveReqVO.class);
        WmsOutboundRespVO inbound = outboundService.getOutboundWithItemList(importReqVO.getOutboundId());
        WmsOutboundSaveReqVO saveReqVO = BeanUtils.toBean(inbound, WmsOutboundSaveReqVO.class);
        saveReqVO.setItemList(saveReqVOList);
        outboundService.updateOutbound(saveReqVO);
        return success(true);
    }


//    @PutMapping("/update-actual-quantity")
//    @Operation(summary = "设置实际出库量")
//    @PreAuthorize("@ss.hasPermission('wms:outbound-item:update')")
//    public CommonResult<Boolean> updateActualQuantity(@Validated(ValidationGroup.update.class) @RequestBody WmsOutboundSaveReqVO updateReqVOList) {
//        return null;
////        outboundItemService.updateActualQuantity(updateReqVOList.getItemList());
////        return success(true);
//    }
}
