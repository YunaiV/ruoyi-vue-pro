package cn.iocoder.yudao.module.mes.controller.admin.wm.transfer;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.MesWmTransferPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.MesWmTransferRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.MesWmTransferSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferDO;
import cn.iocoder.yudao.module.mes.service.wm.transfer.MesWmTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 转移单")
@RestController
@RequestMapping("/mes/wm/transfer")
@Validated
public class MesWmTransferController {

    @Resource
    private MesWmTransferService transferService;

    @PostMapping("/create")
    @Operation(summary = "创建转移单")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:create')")
    public CommonResult<Long> createTransfer(@Valid @RequestBody MesWmTransferSaveReqVO createReqVO) {
        return success(transferService.createTransfer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改转移单")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:update')")
    public CommonResult<Boolean> updateTransfer(@Valid @RequestBody MesWmTransferSaveReqVO updateReqVO) {
        transferService.updateTransfer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除转移单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:delete')")
    public CommonResult<Boolean> deleteTransfer(@RequestParam("id") Long id) {
        transferService.deleteTransfer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得转移单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:query')")
    public CommonResult<MesWmTransferRespVO> getTransfer(@RequestParam("id") Long id) {
        MesWmTransferDO transfer = transferService.getTransfer(id);
        return success(BeanUtils.toBean(transfer, MesWmTransferRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得转移单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:query')")
    public CommonResult<PageResult<MesWmTransferRespVO>> getTransferPage(
            @Valid MesWmTransferPageReqVO pageReqVO) {
        PageResult<MesWmTransferDO> pageResult = transferService.getTransferPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesWmTransferRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出转移单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransferExcel(@Valid MesWmTransferPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmTransferDO> pageResult = transferService.getTransferPage(pageReqVO);
        ExcelUtils.write(response, "转移单.xls", "数据", MesWmTransferRespVO.class,
                BeanUtils.toBean(pageResult.getList(), MesWmTransferRespVO.class));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交转移单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:update')")
    public CommonResult<Boolean> submitTransfer(@RequestParam("id") Long id) {
        transferService.submitTransfer(id);
        return success(true);
    }

    @PutMapping("/confirm")
    @Operation(summary = "确认转移单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:update')")
    public CommonResult<Boolean> confirmTransfer(@RequestParam("id") Long id) {
        transferService.confirmTransfer(id);
        return success(true);
    }

    @PutMapping("/stock")
    @Operation(summary = "执行上架")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:update')")
    public CommonResult<Boolean> stockTransfer(@RequestParam("id") Long id) {
        transferService.stockTransfer(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成转移单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:finish')")
    public CommonResult<Boolean> finishTransfer(@RequestParam("id") Long id) {
        transferService.finishTransfer(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消转移单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-transfer:update')")
    public CommonResult<Boolean> cancelTransfer(@RequestParam("id") Long id) {
        transferService.cancelTransfer(id);
        return success(true);
    }

}
