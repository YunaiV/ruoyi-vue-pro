package cn.iocoder.yudao.module.wms.controller.admin.exchange;

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

import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.service.exchange.WmsExchangeService;

@Tag(name = "管理后台 - 换货单")
@RestController
@RequestMapping("/wms/exchange")
@Validated
public class WmsExchangeController {

    @Resource
    private WmsExchangeService exchangeService;

    @PostMapping("/create")
    @Operation(summary = "创建换货单")
    @PreAuthorize("@ss.hasPermission('wms:exchange:create')")
    public CommonResult<Long> createExchange(@Valid @RequestBody WmsExchangeSaveReqVO createReqVO) {
        return success(exchangeService.createExchange(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新换货单")
    @PreAuthorize("@ss.hasPermission('wms:exchange:update')")
    public CommonResult<Boolean> updateExchange(@Valid @RequestBody WmsExchangeSaveReqVO updateReqVO) {
        exchangeService.updateExchange(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除换货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:exchange:delete')")
    public CommonResult<Boolean> deleteExchange(@RequestParam("id") Long id) {
        exchangeService.deleteExchange(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得换货单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:exchange:query')")
    public CommonResult<WmsExchangeRespVO> getExchange(@RequestParam("id") Long id) {
        WmsExchangeDO exchange = exchangeService.getExchange(id);
        return success(BeanUtils.toBean(exchange, WmsExchangeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得换货单分页")
    @PreAuthorize("@ss.hasPermission('wms:exchange:query')")
    public CommonResult<PageResult<WmsExchangeRespVO>> getExchangePage(@Valid WmsExchangePageReqVO pageReqVO) {
        PageResult<WmsExchangeDO> pageResult = exchangeService.getExchangePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsExchangeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出换货单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:exchange:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExchangeExcel(@Valid WmsExchangePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsExchangeDO> list = exchangeService.getExchangePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "换货单.xls", "数据", WmsExchangeRespVO.class,
                        BeanUtils.toBean(list, WmsExchangeRespVO.class));
    }

}