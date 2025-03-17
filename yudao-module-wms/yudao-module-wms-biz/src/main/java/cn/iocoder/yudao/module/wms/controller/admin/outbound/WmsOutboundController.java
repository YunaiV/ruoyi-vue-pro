package cn.iocoder.yudao.module.wms.controller.admin.outbound;

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

import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;

@Tag(name = "管理后台 - 出库单")
@RestController
@RequestMapping("/wms/outbound")
@Validated
public class WmsOutboundController {

    @Resource
    private WmsOutboundService outboundService;

    @PostMapping("/create")
    @Operation(summary = "创建出库单")
    @PreAuthorize("@ss.hasPermission('wms:outbound:create')")
    public CommonResult<Long> createOutbound(@Valid @RequestBody WmsOutboundSaveReqVO createReqVO) {
        return success(outboundService.createOutbound(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出库单")
    @PreAuthorize("@ss.hasPermission('wms:outbound:update')")
    public CommonResult<Boolean> updateOutbound(@Valid @RequestBody WmsOutboundSaveReqVO updateReqVO) {
        outboundService.updateOutbound(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:outbound:delete')")
    public CommonResult<Boolean> deleteOutbound(@RequestParam("id") Long id) {
        outboundService.deleteOutbound(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:outbound:query')")
    public CommonResult<WmsOutboundRespVO> getOutbound(@RequestParam("id") Long id) {
        WmsOutboundDO outbound = outboundService.getOutbound(id);
        return success(BeanUtils.toBean(outbound, WmsOutboundRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出库单分页")
    @PreAuthorize("@ss.hasPermission('wms:outbound:query')")
    public CommonResult<PageResult<WmsOutboundRespVO>> getOutboundPage(@Valid WmsOutboundPageReqVO pageReqVO) {
        PageResult<WmsOutboundDO> pageResult = outboundService.getOutboundPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsOutboundRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:outbound:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutboundExcel(@Valid WmsOutboundPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsOutboundDO> list = outboundService.getOutboundPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出库单.xls", "数据", WmsOutboundRespVO.class,
                        BeanUtils.toBean(list, WmsOutboundRespVO.class));
    }

}