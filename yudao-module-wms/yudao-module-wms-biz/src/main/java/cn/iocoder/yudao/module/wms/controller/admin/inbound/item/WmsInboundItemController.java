package cn.iocoder.yudao.module.wms.controller.admin.inbound.item;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemBinQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockType;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.quantity.InboundExecutor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.INBOUND_ITEM_PRODUCT_NOT_EXISTS;

/**
 * @author jisencai
 */
@Tag(name = "入库单详情")
@RestController
@RequestMapping("/wms/inbound-item")
@Validated
public class WmsInboundItemController {

    @Resource
    private WmsInboundItemService inboundItemService;

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    // /**
    // * @sign : FDA8F53584D62A17
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建入库单详情")
    // @PreAuthorize("@ss.hasPermission('wms:inbound-item:create')")
    // public CommonResult<Long> createInboundItem(@Validated(ValidationGroup.create.class) @RequestBody WmsInboundItemSaveReqVO createReqVO) {
    // return success(inboundItemService.createInboundItem(createReqVO).getId());
    // }
    // /**
    // * @sign : 18BE32AD9E053614
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新入库单详情")
    // @PreAuthorize("@ss.hasPermission('wms:inbound-item:update')")
    // public CommonResult<Boolean> updateInboundItem(@Validated(ValidationGroup.update.class) @RequestBody WmsInboundItemSaveReqVO updateReqVO) {
    // inboundItemService.updateInboundItem(updateReqVO);
    // return success(true);
    // }
    @PutMapping("/update-actual-quantity")
    @Operation(summary = "设置实际入库量")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:update')")
    public CommonResult<Boolean> updateActualQuantity(@Validated(ValidationGroup.update.class) @RequestBody List<WmsInboundItemSaveReqVO> updateReqVOList) {
//        if (CollectionUtils.isEmpty(updateReqVOList)) {
//            throw exception(INBOUND_NOT_EXISTS);
//        }
//        Set<Long> inboundIds = StreamX.from(updateReqVOList).toSet(WmsInboundItemSaveReqVO::getInboundId);
//        if (inboundIds.size() > 1) {
//            throw exception(INBOUND_ITEM_INBOUND_ID_DUPLICATE);
//        }
//        Long inboundId = inboundIds.stream().findFirst().get();
//        WmsInboundDO inboundDO = inboundService.validateInboundExists(inboundId);
//        WmsApprovalReqVO wmsApprovalReqVO = new WmsApprovalReqVO();
//        wmsApprovalReqVO.setBillType(WMS_INBOUND.getValue());
//        wmsApprovalReqVO.setBillId(inboundId);
//        wmsApprovalReqVO.setStatusType(inboundDO.getInboundStatus()+"");
//        wmsApprovalReqVO.setComment(inboundDO.getRemark());
//        inboundService.approve(WmsInboundAuditStatus.Event.AGREE, wmsApprovalReqVO);
        inboundItemService.updateActualQuantity(updateReqVOList);
        return success(true);
    }

    // @DeleteMapping("/delete")
    // @Operation(summary = "删除入库单详情")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:inbound-item:delete')")
    // public CommonResult<Boolean> deleteInboundItem(@RequestParam("id") Long id) {
    // inboundItemService.deleteInboundItem(id);
    // return success(true);
    // }
    // /**
    // * @sign : F0DA74F2E45ABE2D
    /**
     * @sign : F0DA74F2E45ABE2D
     */
    @GetMapping("/get")
    @Operation(summary = "获得入库单详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<WmsInboundItemRespVO> getInboundItem(@RequestParam("id") Long id) {
        // 查询数据
        WmsInboundItemDO inboundItem = inboundItemService.getInboundItem(id);
        if (inboundItem == null) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        // 转换
        WmsInboundItemRespVO inboundItemVO = BeanUtils.toBean(inboundItem, WmsInboundItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(inboundItemVO))
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
			.mapping(WmsInboundItemRespVO::getUpdater, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(inboundItemVO);
    }

    /**
     * @sign : 83456B9A2BFF8F84
     */
    @PostMapping("/page")
    @Operation(summary = "常规批次库存查询")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<PageResult<WmsInboundItemRespVO>> getInboundItemPage(@Valid @RequestBody WmsInboundItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsInboundItemQueryDO> doPageResult = inboundItemService.getInboundItemPage(pageReqVO);
        // 转换
        PageResult<WmsInboundItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInboundItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
			.mapping(WmsInboundItemRespVO::getUpdater, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 装配
        inboundItemService.assembleDept(voPageResult.getList());
        inboundItemService.assembleInbound(voPageResult.getList());
        inboundItemService.assembleProducts(voPageResult.getList());
        inboundItemService.assembleWarehouse(voPageResult.getList());
        inboundItemService.assembleStockType(voPageResult.getList());
        inboundItemService.assembleCompany(voPageResult.getList());
        inboundItemService.assembleStockWarehouse(voPageResult.getList());
        //
        InboundExecutor.setShelveAvailableQty(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }


    @PostMapping("/list")
    @Operation(summary = "常规批次库存列表查询")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<List<WmsInboundItemRespVO>> getInboundItemList(@RequestParam("companyId") Long companyId,  @RequestParam("productIds") List<Long> productIds) {
        // 查询数据
        List<WmsInboundItemQueryDO> doListResult = inboundItemService.getInboundItemList(companyId, productIds);
        // 转换
        List<WmsInboundItemRespVO> voListResult = BeanUtils.toBean(doListResult, WmsInboundItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voListResult)
                .mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
                .mapping(WmsInboundItemRespVO::getUpdater, WmsInboundItemRespVO::setUpdaterName)
                .fill();
        // 装配
//        inboundItemService.assembleDept(voListResult);
//        inboundItemService.assembleInbound(voListResult);
//        inboundItemService.assembleProducts(voListResult);
//        inboundItemService.assembleWarehouse(voListResult);
//        inboundItemService.assembleStockType(voListResult);
//        inboundItemService.assembleCompany(voListResult);
        inboundItemService.assembleStockWarehouse(voListResult);
        InboundExecutor.setShelveAvailableQty(voListResult);
        // 返回
        return success(voListResult);
    }

    /**
     * @sign : 83456B9A2BFF8F84
     */
    @PostMapping("/page-bin")
    @Operation(summary = "库位批次库存查询")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<PageResult<WmsInboundItemBinRespVO>> getInboundItemBinPage(@Valid @RequestBody WmsInboundItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsInboundItemBinQueryDO> doPageResult = inboundItemService.getInboundItemBinPage(pageReqVO,false);
        // 转换
        PageResult<WmsInboundItemBinRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInboundItemBinRespVO.class);

        // 装配
        inboundItemService.assembleDept(voPageResult.getList());
        inboundItemService.assembleProducts(voPageResult.getList());
        inboundItemService.assembleWarehouse(voPageResult.getList());
        inboundItemService.assembleCompany(voPageResult.getList());
        inboundItemService.assembleWarehouseBin(voPageResult.getList());
        inboundItemService.assembleStockWarehouse(voPageResult.getList());
        // 填充入库单信息
        inboundItemService.assembleInbound(voPageResult.getList());
        //
        InboundExecutor.setShelveAvailableQty(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }


    @PostMapping("/export-excel-bin")
    @Operation(summary = "导出批次库存仓位详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item-bin:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInboundItemExcelBin(@Valid @RequestBody WmsInboundItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInboundItemBinRespVO> voList = this.getInboundItemBinPage(pageReqVO).getData().getList();
        Integer lineNumber=0;
        for (WmsInboundItemBinRespVO wmsInboundItemBinRespVO : voList) {
            wmsInboundItemBinRespVO.setLineNumber(lineNumber++);
        }
        Map<Integer, WmsInboundItemBinRespVO> distinctMap = StreamX.from(voList).toMap(WmsInboundItemBinRespVO::getLineNumber);



        List<WmsInboundItemBinExcelVO> inboundItemBinVOS = BeanUtils.toBean(voList, WmsInboundItemBinExcelVO.class);
        for (WmsInboundItemBinExcelVO inboundItemBinVO : inboundItemBinVOS) {
            WmsInboundItemBinRespVO itemRespVO = distinctMap.get(inboundItemBinVO.getLineNumber());
            if (itemRespVO == null) {
                continue;
            }

            inboundItemBinVO.setWarehouseName(itemRespVO.getWarehouse().getName());
            inboundItemBinVO.setInboundCode(itemRespVO.getInbound().getCode());
            inboundItemBinVO.setProductCode(itemRespVO.getProduct().getCode());

            WmsStockType stockType = WmsStockType.parse(itemRespVO.getStockType());
            inboundItemBinVO.setStockTypeLabel(stockType.getLabel());

            if(itemRespVO.getInboundCompany()!=null) {
                inboundItemBinVO.setAddressLine3(itemRespVO.getInboundCompany().getName());
            }

            if(itemRespVO.getInboundDept()!=null) {
                inboundItemBinVO.setDeptName(itemRespVO.getInboundDept().getName());
            }

        }

        // 导出 Excel
        ExcelUtils.write(response, "批次库存仓位详情.xls", "数据", WmsInboundItemBinExcelVO.class, inboundItemBinVOS);
    }


    @GetMapping("/pickup-pending")
    @Operation(summary = "待上架的入库明细")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<PageResult<WmsInboundItemRespVO>> getPickupPending(@Valid WmsPickupPendingPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsInboundItemQueryDO> doPageResult = inboundItemService.getPickupPending(pageReqVO);
        // 转换
        PageResult<WmsInboundItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInboundItemRespVO.class);
        // 填充部门信息
        inboundItemService.assembleDept(voPageResult.getList());
        // 填充产品信息
        inboundItemService.assembleProducts(voPageResult.getList());
        // 填充入库单信息
        inboundItemService.assembleInbound(voPageResult.getList());
        // 填充仓库信息
        inboundItemService.assembleWarehouse(voPageResult.getList());
        //
        inboundItemService.assembleStockWarehouse(voPageResult.getList());
        //
        InboundExecutor.setShelveAvailableQty(voPageResult.getList());
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
			.mapping(WmsInboundItemRespVO::getUpdater, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @PostMapping("/export-excel")
    @Operation(summary = "导出入库单详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInboundItemExcel(@Valid @RequestBody WmsInboundItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<WmsInboundItemQueryDO> doPageResult = inboundItemService.getInboundItemPage(pageReqVO);
        List<WmsInboundItemQueryDO> distinct = StreamX.from(doPageResult.getList()).distinct(WmsInboundItemQueryDO::getId);
        Map<Long, WmsInboundItemQueryDO> distinctMap = StreamX.from(distinct).toMap(WmsInboundItemQueryDO::getId);
        List<WmsInboundItemRespVO> inboundItemVOS = BeanUtils.toBean(distinct, WmsInboundItemRespVO.class);
        // 装配
        inboundItemService.assembleDept(inboundItemVOS);
        inboundItemService.assembleInbound(inboundItemVOS);
        inboundItemService.assembleProducts(inboundItemVOS);
        inboundItemVOS.forEach(item -> {
            if (item.getInbound() != null) {
                item.setWarehouseId(item.getInbound().getWarehouseId());
            }
        });
        inboundItemService.assembleWarehouse(inboundItemVOS);
        inboundItemService.assembleCompany(inboundItemVOS);
        // 转换
        List<WmsInboundItemExportVO> exVOList = BeanUtils.toBean(distinct, WmsInboundItemExportVO.class);
        String inboundCode = null;
        // 扁平化
        Map<Long, WmsInboundItemExportVO> exportMap = StreamX.from(exVOList).toMap(WmsInboundItemExportVO::getId);
        for (WmsInboundItemRespVO itemRespVO : inboundItemVOS) {
            WmsInboundItemExportVO exportVO = exportMap.get(itemRespVO.getId());
            if (exportVO == null) {
                continue;
            }
            if (itemRespVO.getProduct() != null) {
                exportVO.setProductCode(itemRespVO.getProduct().getCode());
                exportVO.setProductName(itemRespVO.getProduct().getName());
            }
            if (itemRespVO.getWarehouse() != null) {
                exportVO.setWarehouseName(itemRespVO.getWarehouse().getName());
            }
            if (itemRespVO.getInbound() != null) {
                exportVO.setInboundCode(itemRespVO.getInbound().getCode());
            }
            if (itemRespVO.getDept() != null) {
                exportVO.setDeptName(itemRespVO.getDept().getName());
            }
            WmsInboundStatus inboundStatus = WmsInboundStatus.parse(itemRespVO.getInboundStatus());
            exportVO.setInboundStatusName(inboundStatus.getLabel());
            WmsInboundItemQueryDO queryDO = distinctMap.get(itemRespVO.getId());
            if (queryDO != null) {
                exportVO.setAge(queryDO.getAge());
            }
            inboundCode = exportVO.getInboundCode();
        }
        // 导出 Excel
        ExcelUtils.write(response, "入库单详情-" + inboundCode + ".xls", "数据", WmsInboundItemExportVO.class, exVOList);
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入详情")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:import')")
    public CommonResult<Boolean> importExcel(@Valid WmsInboundItemImportVO importReqVO) throws Exception {
        List<WmsInboundItemImportExcelVO> impVOList = ExcelUtils.read(importReqVO.getFile(), WmsInboundItemImportExcelVO.class);
        // 装配产品ID
        inboundItemService.assembleProductIds(impVOList);
        for (WmsInboundItemImportExcelVO importExcelVO : impVOList) {
            if (importExcelVO.getProductId() == null) {
                throw exception(INBOUND_ITEM_PRODUCT_NOT_EXISTS, importExcelVO.getProductCode());
            }
        }
        List<WmsInboundItemSaveReqVO> saveReqVOList = BeanUtils.toBean(impVOList, WmsInboundItemSaveReqVO.class);
        WmsInboundRespVO inbound = inboundService.getInboundWithItemList(importReqVO.getInboundId());
        WmsInboundSaveReqVO saveReqVO = BeanUtils.toBean(inbound, WmsInboundSaveReqVO.class);
        saveReqVO.setItemList(saveReqVOList);
        inboundService.updateInbound(saveReqVO);
        return success(true);
    }
}
