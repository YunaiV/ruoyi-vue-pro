package cn.iocoder.yudao.module.wms.controller.admin.outbound.item;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
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
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.module.wms.service.outbound.item.WmsOutboundItemService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "出库单详情")
@RestController
@RequestMapping("/wms/outbound-item")
@Validated
public class WmsOutboundItemController {

    @Resource
    private WmsOutboundItemService outboundItemService;

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
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出出库单详情 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:outbound-item:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportOutboundItemExcel(@Valid WmsOutboundItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsOutboundItemDO> list = outboundItemService.getOutboundItemPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "出库单详情.xls", "数据", WmsOutboundItemRespVO.class, BeanUtils.toBean(list, WmsOutboundItemRespVO.class));
    // }
    @PutMapping("/update-actual-quantity")
    @Operation(summary = "设置实际入库量")
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:update')")
    public CommonResult<Boolean> updateActualQuantity(@Validated(ValidationGroup.update.class) @RequestBody List<WmsOutboundItemSaveReqVO> updateReqVOList) {
        outboundItemService.updateActualQuantity(updateReqVOList);
        return success(true);
    }
}