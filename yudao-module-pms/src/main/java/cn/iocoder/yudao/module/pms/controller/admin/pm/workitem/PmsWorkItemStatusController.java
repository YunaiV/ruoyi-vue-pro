package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemBoardConfigRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemBoardConfigSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusConfigUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusDeleteReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 工作项状态")
@RestController
@RequestMapping("/pms/pm/work-item-status")
@Validated
public class PmsWorkItemStatusController {

    @Resource
    private PmsWorkItemStatusService workItemStatusService;

    @PostMapping("/create")
    @Operation(summary = "创建工作项状态")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Long> createWorkItemStatus(
            @Valid @RequestBody PmsWorkItemStatusCreateReqVO createReqVO) {
        return success(workItemStatusService.createWorkItemStatus(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作项状态")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> updateWorkItemStatusConfig(
            @Valid @RequestBody PmsWorkItemStatusConfigUpdateReqVO updateReqVO) {
        workItemStatusService.updateWorkItemStatusConfig(updateReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-default")
    @Operation(summary = "更新工作项初始状态")
    @Parameter(name = "id", description = "状态编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> updateDefaultWorkItemStatus(@RequestParam("id") Long id) {
        workItemStatusService.updateDefaultWorkItemStatus(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-sort")
    @Operation(summary = "更新工作项状态顺序")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> updateWorkItemStatusSort(
            @Valid @RequestBody PmsWorkItemStatusSortReqVO sortReqVO) {
        workItemStatusService.updateWorkItemStatusSort(sortReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-board-config")
    @Operation(summary = "更新工作项看板列和状态映射")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> updateWorkItemBoardConfig(
            @Valid @RequestBody PmsWorkItemBoardConfigSaveReqVO saveReqVO) {
        workItemStatusService.updateWorkItemBoardConfig(saveReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作项状态并迁移工作项")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> deleteWorkItemStatus(
            @Valid @RequestBody PmsWorkItemStatusDeleteReqVO deleteReqVO) {
        workItemStatusService.deleteWorkItemStatus(deleteReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作项状态详情")
    @Parameter(name = "id", description = "状态编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<PmsWorkItemStatusRespVO> getWorkItemStatus(@RequestParam("id") Long id) {
        PmsWorkItemStatusDO status = workItemStatusService.getWorkItemStatus(id, getLoginUserId());
        return success(BeanUtils.toBean(status, PmsWorkItemStatusRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得工作项看板状态列表")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<List<PmsWorkItemStatusRespVO>> getWorkItemStatusList(
            @RequestParam("projectId") Long projectId, @RequestParam("type") Integer workItemType) {
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId, workItemType,
                getLoginUserId());
        return success(BeanUtils.toBean(statuses, PmsWorkItemStatusRespVO.class));
    }

    @GetMapping("/get-board-config")
    @Operation(summary = "获得工作项看板列和状态映射")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<PmsWorkItemBoardConfigRespVO> getWorkItemBoardConfig(
            @RequestParam("projectId") Long projectId, @RequestParam("type") Integer workItemType) {
        // 1. 校验项目可读并查询已保存的看板列、状态
        List<PmsWorkItemStatusDO> statuses = workItemStatusService
                .getWorkItemStatusList(projectId, workItemType, getLoginUserId());
        List<PmsWorkItemBoardDO> boards = workItemStatusService.getWorkItemBoardList(projectId, workItemType);

        // 2.1 按看板列名称聚合状态编号
        Map<String, List<Long>> statusIdMap = new LinkedHashMap<>();
        for (PmsWorkItemStatusDO status : statuses) {
            if (StrUtil.isNotBlank(status.getBoardName())) {
                statusIdMap.computeIfAbsent(status.getBoardName(), key -> new ArrayList<>()).add(status.getId());
            }
        }
        // 2.2 按看板列顺序组装响应，并保留空列
        Set<Long> assignedStatusIds = new LinkedHashSet<>();
        List<PmsWorkItemBoardConfigRespVO.Board> boardVOs = new ArrayList<>();
        for (PmsWorkItemBoardDO board : boards) {
            List<Long> statusIds = statusIdMap.getOrDefault(board.getName(), new ArrayList<>());
            assignedStatusIds.addAll(statusIds);
            boardVOs.add(new PmsWorkItemBoardConfigRespVO.Board().setId(board.getId())
                    .setName(board.getName()).setStatusIds(statusIds));
        }
        // 2.3 收集未分配到看板列的状态
        List<Long> unassignedStatusIds = new ArrayList<>();
        for (PmsWorkItemStatusDO status : statuses) {
            if (!assignedStatusIds.contains(status.getId())) {
                unassignedStatusIds.add(status.getId());
            }
        }
        return success(new PmsWorkItemBoardConfigRespVO().setBoards(boardVOs).setUnassignedStatusIds(unassignedStatusIds));
    }

}
