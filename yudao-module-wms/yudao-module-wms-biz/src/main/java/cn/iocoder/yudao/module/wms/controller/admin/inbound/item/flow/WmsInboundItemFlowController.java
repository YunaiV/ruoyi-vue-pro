package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow;

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

import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsInboundItemFlowService;

@Tag(name = "管理后台 - 入库单库存详情扣减")
@RestController
@RequestMapping("/wms/inbound-item-flow")
@Validated
public class WmsInboundItemFlowController {

    @Resource
    private WmsInboundItemFlowService inboundItemFlowService;

    @PostMapping("/create")
    @Operation(summary = "创建入库单库存详情扣减")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:create')")
    public CommonResult<Long> createInboundItemFlow(@Valid @RequestBody WmsInboundItemFlowSaveReqVO createReqVO) {
        return success(inboundItemFlowService.createInboundItemFlow(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新入库单库存详情扣减")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:update')")
    public CommonResult<Boolean> updateInboundItemFlow(@Valid @RequestBody WmsInboundItemFlowSaveReqVO updateReqVO) {
        inboundItemFlowService.updateInboundItemFlow(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除入库单库存详情扣减")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:delete')")
    public CommonResult<Boolean> deleteInboundItemFlow(@RequestParam("id") Long id) {
        inboundItemFlowService.deleteInboundItemFlow(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得入库单库存详情扣减")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:query')")
    public CommonResult<WmsInboundItemFlowRespVO> getInboundItemFlow(@RequestParam("id") Long id) {
        WmsInboundItemFlowDO inboundItemFlow = inboundItemFlowService.getInboundItemFlow(id);
        return success(BeanUtils.toBean(inboundItemFlow, WmsInboundItemFlowRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得入库单库存详情扣减分页")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:query')")
    public CommonResult<PageResult<WmsInboundItemFlowRespVO>> getInboundItemFlowPage(@Valid WmsInboundItemFlowPageReqVO pageReqVO) {
        PageResult<WmsInboundItemFlowDO> pageResult = inboundItemFlowService.getInboundItemFlowPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInboundItemFlowRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出入库单库存详情扣减 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInboundItemFlowExcel(@Valid WmsInboundItemFlowPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInboundItemFlowDO> list = inboundItemFlowService.getInboundItemFlowPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "入库单库存详情扣减.xls", "数据", WmsInboundItemFlowRespVO.class,
                        BeanUtils.toBean(list, WmsInboundItemFlowRespVO.class));
    }

}