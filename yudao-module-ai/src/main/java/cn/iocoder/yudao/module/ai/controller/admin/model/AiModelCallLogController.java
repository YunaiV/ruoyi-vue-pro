package cn.iocoder.yudao.module.ai.controller.admin.model;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.service.billing.AiModelCallLogService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 模型调用日志")
@RestController
@RequestMapping("/ai/model-call-log")
@Validated
public class AiModelCallLogController {

    @Resource
    private AiModelCallLogService callLogService;

    @GetMapping("/get")
    @Operation(summary = "获得调用日志详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:model-call-log:query')")
    public CommonResult<AiModelCallLogRespVO> getCallLog(@RequestParam("id") Long id) {
        AiModelCallLogDO callLog = callLogService.getCallLog(id);
        return success(convertToRespVO(callLog));
    }

    @GetMapping("/page")
    @Operation(summary = "获得调用日志分页")
    @PreAuthorize("@ss.hasPermission('ai:model-call-log:query')")
    public CommonResult<PageResult<AiModelCallLogRespVO>> getCallLogPage(@Valid AiModelCallLogPageReqVO pageReqVO) {
        PageResult<AiModelCallLogDO> pageResult = callLogService.getCallLogPage(pageReqVO);
        return success(convertToPageRespVO(pageResult));
    }

    @GetMapping("/stat")
    @Operation(summary = "获得调用日志统计")
    @PreAuthorize("@ss.hasPermission('ai:model-call-log:query')")
    public CommonResult<AiModelCallLogStatRespVO> getCallLogStat(@Valid AiModelCallLogStatReqVO statReqVO) {
        return success(callLogService.getCallLogStat(statReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出调用日志 Excel")
    @PreAuthorize("@ss.hasPermission('ai:model-call-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCallLogExcel(@Valid AiModelCallLogPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        List<AiModelCallLogDO> list = callLogService.getCallLogList(pageReqVO);
        List<AiModelCallLogExcelVO> excelList = BeanUtils.toBean(list, AiModelCallLogExcelVO.class);
        ExcelUtils.write(response, "AI调用日志.xls", "数据", AiModelCallLogExcelVO.class, excelList);
    }

    // ========== 转换方法 ==========

    private AiModelCallLogRespVO convertToRespVO(AiModelCallLogDO callLog) {
        if (callLog == null) {
            return null;
        }
        AiModelCallLogRespVO respVO = BeanUtils.toBean(callLog, AiModelCallLogRespVO.class);
        // 微元转元
        if (callLog.getCostAmount() != null) {
            respVO.setCostAmountYuan(callLog.getCostAmount() / 1_000_000.0);
        }
        return respVO;
    }

    private PageResult<AiModelCallLogRespVO> convertToPageRespVO(PageResult<AiModelCallLogDO> pageResult) {
        return new PageResult<>(
                pageResult.getList().stream().map(this::convertToRespVO).toList(),
                pageResult.getTotal()
        );
    }

}
