package cn.iocoder.yudao.module.jl.controller.admin.laboratory;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.ChargeItem;
import cn.iocoder.yudao.module.jl.mapper.laboratory.ChargeItemMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.ChargeItemService;

@Tag(name = "管理后台 - 实验收费项")
@RestController
@RequestMapping("/jl/charge-item")
@Validated
public class ChargeItemController {

    @Resource
    private ChargeItemService chargeItemService;

    @Resource
    private ChargeItemMapper chargeItemMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验收费项")
    @PreAuthorize("@ss.hasPermission('jl:charge-item:create')")
    public CommonResult<Long> createChargeItem(@Valid @RequestBody ChargeItemCreateReqVO createReqVO) {
        return success(chargeItemService.createChargeItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验收费项")
    @PreAuthorize("@ss.hasPermission('jl:charge-item:update')")
    public CommonResult<Boolean> updateChargeItem(@Valid @RequestBody ChargeItemUpdateReqVO updateReqVO) {
        chargeItemService.updateChargeItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验收费项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:charge-item:delete')")
    public CommonResult<Boolean> deleteChargeItem(@RequestParam("id") Long id) {
        chargeItemService.deleteChargeItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验收费项")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:charge-item:query')")
    public CommonResult<ChargeItemRespVO> getChargeItem(@RequestParam("id") Long id) {
            Optional<ChargeItem> chargeItem = chargeItemService.getChargeItem(id);
        return success(chargeItem.map(chargeItemMapper::toDto).orElseThrow(() -> exception(CHARGE_ITEM_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验收费项列表")
    @PreAuthorize("@ss.hasPermission('jl:charge-item:query')")
    public CommonResult<PageResult<ChargeItemRespVO>> getChargeItemPage(@Valid ChargeItemPageReqVO pageVO, @Valid ChargeItemPageOrder orderV0) {
        PageResult<ChargeItem> pageResult = chargeItemService.getChargeItemPage(pageVO, orderV0);
        return success(chargeItemMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验收费项 Excel")
    @PreAuthorize("@ss.hasPermission('jl:charge-item:export')")
    @OperateLog(type = EXPORT)
    public void exportChargeItemExcel(@Valid ChargeItemExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ChargeItem> list = chargeItemService.getChargeItemList(exportReqVO);
        // 导出 Excel
        List<ChargeItemExcelVO> excelData = chargeItemMapper.toExcelList(list);
        ExcelUtils.write(response, "实验收费项.xls", "数据", ChargeItemExcelVO.class, excelData);
    }

}
