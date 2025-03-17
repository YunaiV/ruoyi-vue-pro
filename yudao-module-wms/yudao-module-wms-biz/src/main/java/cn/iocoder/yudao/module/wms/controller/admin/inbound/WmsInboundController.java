package cn.iocoder.yudao.module.wms.controller.admin.inbound;

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

import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;

@Tag(name = "管理后台 - 入库单")
@RestController
@RequestMapping("/wms/inbound")
@Validated
public class WmsInboundController {

    @Resource
    private WmsInboundService inboundService;

    @PostMapping("/create")
    @Operation(summary = "创建入库单")
    @PreAuthorize("@ss.hasPermission('wms:inbound:create')")
    public CommonResult<Long> createInbound(@Valid @RequestBody WmsInboundSaveReqVO createReqVO) {
        return success(inboundService.createInbound(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新入库单")
    @PreAuthorize("@ss.hasPermission('wms:inbound:update')")
    public CommonResult<Boolean> updateInbound(@Valid @RequestBody WmsInboundSaveReqVO updateReqVO) {
        inboundService.updateInbound(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inbound:delete')")
    public CommonResult<Boolean> deleteInbound(@RequestParam("id") Long id) {
        inboundService.deleteInbound(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inbound:query')")
    public CommonResult<WmsInboundRespVO> getInbound(@RequestParam("id") Long id) {
        WmsInboundDO inbound = inboundService.getInbound(id);
        return success(BeanUtils.toBean(inbound, WmsInboundRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得入库单分页")
    @PreAuthorize("@ss.hasPermission('wms:inbound:query')")
    public CommonResult<PageResult<WmsInboundRespVO>> getInboundPage(@Valid WmsInboundPageReqVO pageReqVO) {
        PageResult<WmsInboundDO> pageResult = inboundService.getInboundPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInboundRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出入库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inbound:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInboundExcel(@Valid WmsInboundPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInboundDO> list = inboundService.getInboundPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "入库单.xls", "数据", WmsInboundRespVO.class,
                        BeanUtils.toBean(list, WmsInboundRespVO.class));
    }

}