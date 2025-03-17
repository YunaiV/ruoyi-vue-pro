package cn.iocoder.yudao.module.wms.controller.admin.inbound.item;

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

import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;

@Tag(name = "管理后台 - 入库单详情")
@RestController
@RequestMapping("/wms/inbound-item")
@Validated
public class WmsInboundItemController {

    @Resource
    private WmsInboundItemService inboundItemService;

    @PostMapping("/create")
    @Operation(summary = "创建入库单详情")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:create')")
    public CommonResult<Long> createInboundItem(@Valid @RequestBody WmsInboundItemSaveReqVO createReqVO) {
        return success(inboundItemService.createInboundItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新入库单详情")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:update')")
    public CommonResult<Boolean> updateInboundItem(@Valid @RequestBody WmsInboundItemSaveReqVO updateReqVO) {
        inboundItemService.updateInboundItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除入库单详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:delete')")
    public CommonResult<Boolean> deleteInboundItem(@RequestParam("id") Long id) {
        inboundItemService.deleteInboundItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得入库单详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<WmsInboundItemRespVO> getInboundItem(@RequestParam("id") Long id) {
        WmsInboundItemDO inboundItem = inboundItemService.getInboundItem(id);
        return success(BeanUtils.toBean(inboundItem, WmsInboundItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得入库单详情分页")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<PageResult<WmsInboundItemRespVO>> getInboundItemPage(@Valid WmsInboundItemPageReqVO pageReqVO) {
        PageResult<WmsInboundItemDO> pageResult = inboundItemService.getInboundItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInboundItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出入库单详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInboundItemExcel(@Valid WmsInboundItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInboundItemDO> list = inboundItemService.getInboundItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "入库单详情.xls", "数据", WmsInboundItemRespVO.class,
                        BeanUtils.toBean(list, WmsInboundItemRespVO.class));
    }

}