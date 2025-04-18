package cn.iocoder.yudao.module.wms.controller.admin.exchange.defective;

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

import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import cn.iocoder.yudao.module.wms.service.exchange.defective.WmsExchangeDefectiveService;

@Tag(name = "管理后台 - 良次换货详情")
@RestController
@RequestMapping("/wms/exchange-defective")
@Validated
public class WmsExchangeDefectiveController {

    @Resource
    private WmsExchangeDefectiveService exchangeDefectiveService;

    @PostMapping("/create")
    @Operation(summary = "创建良次换货详情")
    @PreAuthorize("@ss.hasPermission('wms:exchange-defective:create')")
    public CommonResult<Long> createExchangeDefective(@Valid @RequestBody WmsExchangeDefectiveSaveReqVO createReqVO) {
        return success(exchangeDefectiveService.createExchangeDefective(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新良次换货详情")
    @PreAuthorize("@ss.hasPermission('wms:exchange-defective:update')")
    public CommonResult<Boolean> updateExchangeDefective(@Valid @RequestBody WmsExchangeDefectiveSaveReqVO updateReqVO) {
        exchangeDefectiveService.updateExchangeDefective(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除良次换货详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:exchange-defective:delete')")
    public CommonResult<Boolean> deleteExchangeDefective(@RequestParam("id") Long id) {
        exchangeDefectiveService.deleteExchangeDefective(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得良次换货详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:exchange-defective:query')")
    public CommonResult<WmsExchangeDefectiveRespVO> getExchangeDefective(@RequestParam("id") Long id) {
        WmsExchangeDefectiveDO exchangeDefective = exchangeDefectiveService.getExchangeDefective(id);
        return success(BeanUtils.toBean(exchangeDefective, WmsExchangeDefectiveRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得良次换货详情分页")
    @PreAuthorize("@ss.hasPermission('wms:exchange-defective:query')")
    public CommonResult<PageResult<WmsExchangeDefectiveRespVO>> getExchangeDefectivePage(@Valid WmsExchangeDefectivePageReqVO pageReqVO) {
        PageResult<WmsExchangeDefectiveDO> pageResult = exchangeDefectiveService.getExchangeDefectivePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsExchangeDefectiveRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出良次换货详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:exchange-defective:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExchangeDefectiveExcel(@Valid WmsExchangeDefectivePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsExchangeDefectiveDO> list = exchangeDefectiveService.getExchangeDefectivePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "良次换货详情.xls", "数据", WmsExchangeDefectiveRespVO.class,
                        BeanUtils.toBean(list, WmsExchangeDefectiveRespVO.class));
    }

}