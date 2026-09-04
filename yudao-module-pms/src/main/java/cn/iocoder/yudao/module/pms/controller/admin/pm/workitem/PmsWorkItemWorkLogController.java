package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSummaryRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemWorkLogDO;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemWorkLogService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 工作项工时")
@RestController
@RequestMapping("/pms/pm/work-item-work-log")
@Validated
public class PmsWorkItemWorkLogController {

    @Resource
    private PmsWorkItemWorkLogService workLogService;
    @Resource
    private PmsWorkItemService workItemService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建工作项工时记录")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Long> createWorkItemWorkLog(@Valid @RequestBody PmsWorkItemWorkLogSaveReqVO saveReqVO) {
        return success(workLogService.createWorkItemWorkLog(saveReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作项工时记录")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> updateWorkItemWorkLog(@Valid @RequestBody PmsWorkItemWorkLogSaveReqVO saveReqVO) {
        workLogService.updateWorkItemWorkLog(saveReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作项工时记录")
    @Parameter(name = "id", description = "工时记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<PmsWorkItemWorkLogRespVO> getWorkItemWorkLog(@RequestParam("id") Long id) {
        PmsWorkItemWorkLogDO workLog = workLogService.getWorkItemWorkLog(id, getLoginUserId());
        PmsWorkItemWorkLogRespVO respVO = BeanUtils.toBean(workLog, PmsWorkItemWorkLogRespVO.class)
                .setCreatorUserId(NumberUtils.parseLong(workLog.getCreator()));
        fillWorkItemWorkLogRespVOList(Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/summary")
    @Operation(summary = "获得工作项工时汇总")
    @Parameter(name = "workItemId", description = "工作项编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<PmsWorkItemWorkLogSummaryRespVO> getWorkItemWorkLogSummary(
            @RequestParam("workItemId") Long workItemId) {
        PmsWorkItemWorkLogSummaryRespVO summary = workLogService.getWorkItemWorkLogSummary(workItemId, getLoginUserId());
        fillWorkItemWorkLogRespVOList(summary.getRecords());
        return success(summary);
    }

    @GetMapping("/project-report")
    @Operation(summary = "获得项目工时报表")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<PmsProjectWorkLogReportRespVO> getProjectWorkItemWorkLogReport(
            @Valid PmsProjectWorkLogReportReqVO reqVO) {
        PmsProjectWorkLogReportRespVO report = workLogService.getProjectWorkItemWorkLogReport(
                reqVO, getLoginUserId());
        return success(report);
    }

    // ==================== 拼接 VO ====================

    private List<PmsWorkItemWorkLogRespVO> fillWorkItemWorkLogRespVOList(List<PmsWorkItemWorkLogRespVO> workLogs) {
        if (CollUtil.isEmpty(workLogs)) {
            return Collections.emptyList();
        }
        // 1. 批量查询创建人和工作项
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(
                workLogs, PmsWorkItemWorkLogRespVO::getCreatorUserId));
        Map<Long, PmsWorkItemDO> workItemMap = workItemService.getWorkItemMap(
                convertSet(workLogs, PmsWorkItemWorkLogRespVO::getWorkItemId));
        // 2. 拼接创建人和工作项名称
        for (PmsWorkItemWorkLogRespVO workLog : workLogs) {
            findAndThen(userMap, workLog.getCreatorUserId(), user -> workLog.setCreatorUserName(user.getNickname()));
            findAndThen(workItemMap, workLog.getWorkItemId(), item -> workLog.setWorkItemName(item.getName()));
        }
        return workLogs;
    }

}
