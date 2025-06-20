package cn.iocoder.yudao.module.wms.controller.admin.pickup;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.service.pickup.WmsPickupService;
import cn.iocoder.yudao.module.wms.service.pickup.item.WmsPickupItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.PICKUP_NOT_EXISTS;

@Tag(name = "拣货单")
@RestController
@RequestMapping("/wms/pickup")
@Validated
public class WmsPickupController {

    @Resource
    @Lazy
    private WmsPickupItemService pickupItemService;

    @Resource
    private WmsPickupService pickupService;

    /**
     * @sign : 50A2CF839F346ECB
     */
    @PostMapping("/create")
    @Operation(summary = "创建拣货单")
    @PreAuthorize("@ss.hasPermission('wms:pickup:create')")
    public CommonResult<Long> createPickup(@RequestBody WmsPickupSaveReqVO createReqVO) {
        return success(pickupService.createPickup(createReqVO, null).getId());
    }

    // /**
    // * @sign : E1B13418492DDEBD
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新拣货单")
    // @PreAuthorize("@ss.hasPermission('wms:pickup:update')")
    // public CommonResult<Boolean> updatePickup(@Valid @RequestBody WmsPickupSaveReqVO updateReqVO) {
    // pickupService.updatePickup(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除拣货单")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:pickup:delete')")
    // public CommonResult<Boolean> deletePickup(@RequestParam("id") Long id) {
    // pickupService.deletePickup(id);
    // return success(true);
    // }
    /**
     * @sign : 882A350F07B4DCFE
     */
    @GetMapping("/get")
    @Operation(summary = "获得拣货单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:pickup:query')")
    public CommonResult<WmsPickupRespVO> getPickup(@RequestParam("id") Long id) {
        // 查询数据
        WmsPickupDO pickup = pickupService.getPickup(id);
        if (pickup == null) {
            throw exception(PICKUP_NOT_EXISTS);
        }
        // 转换
        WmsPickupRespVO pickupVO = BeanUtils.toBean(pickup, WmsPickupRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(pickupVO))
			.mapping(WmsPickupRespVO::getCreator, WmsPickupRespVO::setCreatorName)
			.mapping(WmsPickupRespVO::getUpdater, WmsPickupRespVO::setUpdaterName)
			.fill();
        // 组装拣货单详情
        List<WmsPickupItemDO> pickupItemList = pickupItemService.selectByPickupId(pickupVO.getId());
        pickupVO.setItemList(BeanUtils.toBean(pickupItemList, WmsPickupItemRespVO.class));
        // 
        pickupService.assembleWarehouse(List.of(pickupVO));
        pickupItemService.assembleProduct(pickupVO.getItemList());
        pickupItemService.assembleInbound(pickupVO.getItemList());
        // 返回
        return success(pickupVO);
    }

    /**
     * @sign : 5D5029FCDD560031
     */
    @PostMapping("/page")
    @Operation(summary = "获得拣货单分页")
    @PreAuthorize("@ss.hasPermission('wms:pickup:query')")
    public CommonResult<PageResult<WmsPickupRespVO>> getPickupPage(@Valid @RequestBody WmsPickupPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsPickupDO> doPageResult = pickupService.getPickupPage(pageReqVO);
        // 转换
        PageResult<WmsPickupRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsPickupRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsPickupRespVO::getCreator, WmsPickupRespVO::setCreatorName)
			.mapping(WmsPickupRespVO::getUpdater, WmsPickupRespVO::setUpdaterName)
			.fill();
        // 组装仓库
        pickupService.assembleWarehouse(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出拣货单 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:pickup:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportPickupExcel(@Valid WmsPickupPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsPickupDO> list = pickupService.getPickupPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "拣货单.xls", "数据", WmsPickupRespVO.class, BeanUtils.toBean(list, WmsPickupRespVO.class));
    // }
}
