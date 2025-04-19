package cn.iocoder.yudao.module.wms.controller.admin.pickup.item;

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
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.service.pickup.item.WmsPickupItemService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "拣货单详情")
@RestController
@RequestMapping("/wms/pickup-item")
@Validated
public class WmsPickupItemController {

    @Resource
    private WmsPickupItemService pickupItemService;
    // /**
    // * @sign : 2550F9CE4D12476E
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建拣货单详情")
    // @PreAuthorize("@ss.hasPermission('wms:pickup-item:create')")
    // public CommonResult<Long> createPickupItem(@Valid @RequestBody WmsPickupItemSaveReqVO createReqVO) {
    // return success(pickupItemService.createPickupItem(createReqVO).getId());
    // }
    // 
    // /**
    // * @sign : C4D4A714BC6DDDE1
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新拣货单详情")
    // @PreAuthorize("@ss.hasPermission('wms:pickup-item:update')")
    // public CommonResult<Boolean> updatePickupItem(@Valid @RequestBody WmsPickupItemSaveReqVO updateReqVO) {
    // pickupItemService.updatePickupItem(updateReqVO);
    // return success(true);
    // }
    // 
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除拣货单详情")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:pickup-item:delete')")
    // public CommonResult<Boolean> deletePickupItem(@RequestParam("id") Long id) {
    // pickupItemService.deletePickupItem(id);
    // return success(true);
    // }
    // 
    // /**
    // * @sign : 4DB1BE6C9EA6225A
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得拣货单详情")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:pickup-item:query')")
    // public CommonResult<WmsPickupItemRespVO> getPickupItem(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsPickupItemDO pickupItem = pickupItemService.getPickupItem(id);
    // if (pickupItem == null) {
    // throw exception(PICKUP_ITEM_NOT_EXISTS);
    // }
    // // 转换
    // WmsPickupItemRespVO pickupItemVO = BeanUtils.toBean(pickupItem, WmsPickupItemRespVO.class);
    // // 人员姓名填充
    // AdminUserApi.inst().prepareFill(List.of(pickupItemVO))
    // .mapping(WmsPickupItemRespVO::getCreator, WmsPickupItemRespVO::setCreatorName)
    // .mapping(WmsPickupItemRespVO::getCreator, WmsPickupItemRespVO::setUpdaterName)
    // .fill();
    // // 返回
    // return success(pickupItemVO);
    // }
    // 
    // /**
    // * @sign : 38D6844AE9F851D1
    // */
    // @GetMapping("/page")
    // @Operation(summary = "获得拣货单详情分页")
    // @PreAuthorize("@ss.hasPermission('wms:pickup-item:query')")
    // public CommonResult<PageResult<WmsPickupItemRespVO>> getPickupItemPage(@Valid WmsPickupItemPageReqVO pageReqVO) {
    // // 查询数据
    // PageResult<WmsPickupItemDO> doPageResult = pickupItemService.getPickupItemPage(pageReqVO);
    // // 转换
    // PageResult<WmsPickupItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsPickupItemRespVO.class);
    // // 人员姓名填充
    // AdminUserApi.inst().prepareFill(voPageResult.getList())
    // .mapping(WmsPickupItemRespVO::getCreator, WmsPickupItemRespVO::setCreatorName)
    // .mapping(WmsPickupItemRespVO::getCreator, WmsPickupItemRespVO::setUpdaterName)
    // .fill();
    // // 返回
    // return success(voPageResult);
    // }
    // 
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出拣货单详情 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:pickup-item:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportPickupItemExcel(@Valid WmsPickupItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsPickupItemDO> list = pickupItemService.getPickupItemPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "拣货单详情.xls", "数据", WmsPickupItemRespVO.class, BeanUtils.toBean(list, WmsPickupItemRespVO.class));
    // }
}