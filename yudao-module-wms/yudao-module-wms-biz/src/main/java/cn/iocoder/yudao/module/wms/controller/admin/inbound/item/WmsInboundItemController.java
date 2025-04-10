package cn.iocoder.yudao.module.wms.controller.admin.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsPickupPendingPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;

@Tag(name = "入库单详情")
@RestController
@RequestMapping("/wms/inbound-item")
@Validated
public class WmsInboundItemController {

    @Resource
    private WmsInboundItemService inboundItemService;

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
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(inboundItemVO);
    }

    /**
     * @sign : 83456B9A2BFF8F84
     */
    @GetMapping("/page")
    @Operation(summary = "批次库存查询")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<PageResult<WmsInboundItemRespVO>> getInboundItemPage(@Valid WmsInboundItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsInboundItemQueryDO> doPageResult = inboundItemService.getInboundItemPage(pageReqVO);
        // 转换
        PageResult<WmsInboundItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInboundItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 装配
        inboundItemService.assembleDept(voPageResult.getList());
        inboundItemService.assembleInbound(voPageResult.getList());
        inboundItemService.assembleProducts(voPageResult.getList());
        inboundItemService.assembleWarehouse(voPageResult.getList());
        inboundItemService.assembleWarehouseBin(voPageResult.getList());
        // 返回
        return success(voPageResult);
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
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出入库单详情 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:inbound-item:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportInboundItemExcel(@Valid WmsInboundItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsInboundItemDO> list = inboundItemService.getInboundItemPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "入库单详情.xls", "数据", WmsInboundItemRespVO.class, BeanUtils.toBean(list, WmsInboundItemRespVO.class));
    // }
}
