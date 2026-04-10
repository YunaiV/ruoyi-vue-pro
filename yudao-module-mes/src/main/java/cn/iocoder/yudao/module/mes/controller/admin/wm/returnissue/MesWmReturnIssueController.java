package cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.MesWmReturnIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.MesWmReturnIssueRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.MesWmReturnIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueDO;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.returnissue.MesWmReturnIssueService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 生产退料单")
@RestController
@RequestMapping("/mes/wm/return-issue")
@Validated
public class MesWmReturnIssueController {

    @Resource
    private MesWmReturnIssueService issueService;
    @Resource
    private MesMdWorkstationService workstationService;
    @Resource
    private MesProWorkOrderService workOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建生产退料单")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:create')")
    public CommonResult<Long> createReturnIssue(@Valid @RequestBody MesWmReturnIssueSaveReqVO createReqVO) {
        return success(issueService.createReturnIssue(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改生产退料单")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:update')")
    public CommonResult<Boolean> updateReturnIssue(@Valid @RequestBody MesWmReturnIssueSaveReqVO updateReqVO) {
        issueService.updateReturnIssue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产退料单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:delete')")
    public CommonResult<Boolean> deleteReturnIssue(@RequestParam("id") Long id) {
        issueService.deleteReturnIssue(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产退料单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:query')")
    public CommonResult<MesWmReturnIssueRespVO> getReturnIssue(@RequestParam("id") Long id) {
        MesWmReturnIssueDO issue = issueService.getReturnIssue(id);
        if (issue == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(issue)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产退料单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:query')")
    public CommonResult<PageResult<MesWmReturnIssueRespVO>> getReturnIssuePage(
            @Valid MesWmReturnIssuePageReqVO pageReqVO) {
        PageResult<MesWmReturnIssueDO> pageResult = issueService.getReturnIssuePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产退料单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReturnIssueExcel(@Valid MesWmReturnIssuePageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmReturnIssueDO> pageResult = issueService.getReturnIssuePage(pageReqVO);
        ExcelUtils.write(response, "生产退料单.xls", "数据", MesWmReturnIssueRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/finish")
    @Operation(summary = "完成生产退料单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:finish')")
    public CommonResult<Boolean> finishReturnIssue(@RequestParam("id") Long id) {
        issueService.finishReturnIssue(id);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交生产退料单（草稿 → 待检验/待上架）")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:update')")
    public CommonResult<Boolean> submitReturnIssue(@RequestParam("id") Long id) {
        issueService.submitReturnIssue(id);
        return success(true);
    }

    @PutMapping("/stock")
    @Operation(summary = "入库上架")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:update')")
    public CommonResult<Boolean> stockReturnIssue(@RequestParam("id") Long id) {
        issueService.stockReturnIssue(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消生产退料单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-issue:update')")
    public CommonResult<Boolean> cancelReturnIssue(@RequestParam("id") Long id) {
        issueService.cancelReturnIssue(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesWmReturnIssueRespVO> buildRespVOList(List<MesWmReturnIssueDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(list, MesWmReturnIssueDO::getWorkstationId));
        Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesWmReturnIssueDO::getWorkOrderId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmReturnIssueRespVO.class, vo -> {
            MapUtils.findAndThen(workstationMap, vo.getWorkstationId(),
                    workstation -> vo.setWorkstationName(workstation.getName()));
            MapUtils.findAndThen(workOrderMap, vo.getWorkOrderId(),
                    workOrder -> vo.setWorkOrderCode(workOrder.getCode()));
        });
    }

}
