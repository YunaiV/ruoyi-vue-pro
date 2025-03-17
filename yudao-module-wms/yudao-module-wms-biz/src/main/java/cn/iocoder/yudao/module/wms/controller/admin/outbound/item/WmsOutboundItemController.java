package cn.iocoder.yudao.module.wms.controller.admin.outbound.item;

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

@Tag(name = "管理后台 - 出库单详情")
@RestController
@RequestMapping("/wms/outbound-item")
@Validated
public class WmsOutboundItemController {

    @Resource
    private WmsOutboundItemService outboundItemService;

    @PostMapping("/create")
    @Operation(summary = "创建出库单详情")
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:create')")
    public CommonResult<Long> createOutboundItem(@Valid @RequestBody WmsOutboundItemSaveReqVO createReqVO) {
        return success(outboundItemService.createOutboundItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出库单详情")
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:update')")
    public CommonResult<Boolean> updateOutboundItem(@Valid @RequestBody WmsOutboundItemSaveReqVO updateReqVO) {
        outboundItemService.updateOutboundItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出库单详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:delete')")
    public CommonResult<Boolean> deleteOutboundItem(@RequestParam("id") Long id) {
        outboundItemService.deleteOutboundItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出库单详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:query')")
    public CommonResult<WmsOutboundItemRespVO> getOutboundItem(@RequestParam("id") Long id) {
        WmsOutboundItemDO outboundItem = outboundItemService.getOutboundItem(id);
        return success(BeanUtils.toBean(outboundItem, WmsOutboundItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出库单详情分页")
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:query')")
    public CommonResult<PageResult<WmsOutboundItemRespVO>> getOutboundItemPage(@Valid WmsOutboundItemPageReqVO pageReqVO) {
        PageResult<WmsOutboundItemDO> pageResult = outboundItemService.getOutboundItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsOutboundItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出库单详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:outbound-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutboundItemExcel(@Valid WmsOutboundItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsOutboundItemDO> list = outboundItemService.getOutboundItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出库单详情.xls", "数据", WmsOutboundItemRespVO.class,
                        BeanUtils.toBean(list, WmsOutboundItemRespVO.class));
    }

}