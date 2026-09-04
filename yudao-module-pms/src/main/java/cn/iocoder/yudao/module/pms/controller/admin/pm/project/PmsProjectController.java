package cn.iocoder.yudao.module.pms.controller.admin.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectFavoriteService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 项目")
@RestController
@RequestMapping("/pms/pm/project")
@Validated
public class PmsProjectController {

    @Resource
    private PmsProjectService projectService;
    @Resource
    private PmsProjectMemberService projectMemberService;
    @Resource
    private PmsProjectFavoriteService projectFavoriteService;
    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PermissionApi permissionApi;

    @PostMapping("/create")
    @Operation(summary = "创建项目")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:create')")
    public CommonResult<Long> createProject(@Valid @RequestBody PmsProjectSaveReqVO saveReqVO) {
        return success(projectService.createProject(saveReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:update')")
    public CommonResult<Boolean> updateProject(@Valid @RequestBody PmsProjectSaveReqVO saveReqVO) {
        projectService.updateProject(saveReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目详情")
    @Parameter(name = "id", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<PmsProjectRespVO> getProject(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        PmsProjectDO project = projectService.getProjectAndUpdateVisitTime(id, userId);
        return success(buildProjectRespVO(project, userId));
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目分页")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<PageResult<PmsProjectRespVO>> getProjectPage(@Valid PmsProjectPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<PmsProjectDO> pageResult = projectService.getProjectPage(pageReqVO, userId);
        return success(new PageResult<>(buildProjectRespVOList(pageResult.getList(), userId), pageResult.getTotal()));
    }

    @GetMapping("/favorite-list")
    @Operation(summary = "获得星标项目列表")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<List<PmsProjectRespVO>> getFavoriteProjectList() {
        Long userId = getLoginUserId();
        return success(buildProjectRespVOList(projectService.getFavoriteProjectList(userId), userId));
    }

    @GetMapping("/overview")
    @Operation(summary = "获得项目概况")
    @Parameter(name = "projectId", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<PmsProjectOverviewRespVO> getProjectOverview(@RequestParam("projectId") Long projectId) {
        return success(projectService.getProjectOverview(projectId, getLoginUserId()));
    }

    @PutMapping("/archive")
    @Operation(summary = "归档项目")
    @Parameter(name = "id", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:update')")
    public CommonResult<Boolean> archiveProject(@RequestParam("id") Long id) {
        projectService.archiveProject(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/recycle")
    @Operation(summary = "将项目移入回收站")
    @Parameter(name = "id", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:update')")
    public CommonResult<Boolean> recycleProject(@RequestParam("id") Long id) {
        projectService.recycleProject(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/restore")
    @Operation(summary = "恢复项目")
    @Parameter(name = "id", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:update')")
    public CommonResult<Boolean> restoreProject(@RequestParam("id") Long id) {
        projectService.restoreProject(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "彻底删除回收站项目")
    @Parameter(name = "id", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:delete')")
    public CommonResult<Boolean> deleteProject(@RequestParam("id") Long id) {
        projectService.deleteProject(id, getLoginUserId());
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private PmsProjectRespVO buildProjectRespVO(PmsProjectDO project, Long userId) {
        if (project == null) {
            return null;
        }
        return CollUtil.getFirst(buildProjectRespVOList(Collections.singletonList(project), userId));
    }

    private List<PmsProjectRespVO> buildProjectRespVOList(List<PmsProjectDO> projects, Long userId) {
        if (CollUtil.isEmpty(projects)) {
            return Collections.emptyList();
        }
        // 1. 批量查询创建人、项目管理员、成员数量和工作项完成度
        Set<Long> projectIds = convertSet(projects, PmsProjectDO::getId);
        Map<Long, List<Long>> managerUserIdListMap = projectMemberService.getProjectManagerUserIdListMap(projectIds);
        Map<Long, Integer> memberCountMap = projectMemberService.getProjectMemberCountMap(projectIds);
        Map<Long, Map<Integer, Long>> projectStatusCountMap = workItemService
                .getProjectWorkItemStatusCountMap(projectIds);
        Set<Long> userIds = convertSet(projects, project ->
                NumberUtils.parseLong(project.getCreator()));
        managerUserIdListMap.values().forEach(userIds::addAll);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Set<Long> ownerProjectIds = convertSet(projectMemberService.getOwnerProjectIdListByUserId(userId));
        Set<Long> managedProjectIds = convertSet(projectMemberService.getManagedProjectIdListByUserId(userId));
        Set<Long> memberProjectIds = convertSet(projectMemberService.getProjectIdListByUserId(userId));
        Set<Long> writableProjectIds = convertSet(projectMemberService.getWritableProjectIdListByUserId(userId));
        Set<Long> favoriteProjectIds = convertSet(projectFavoriteService.getFavoriteProjectIdListByUserId(userId));
        favoriteProjectIds.retainAll(projectIds);
        boolean superAdmin = permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());
        // 2. 转换项目响应，并拼接展示字段
        return BeanUtils.toBean(projects, PmsProjectRespVO.class, projectVO -> {
            findAndThen(userMap, NumberUtils.parseLong(projectVO.getCreator()),
                    creator -> projectVO.setCreatorName(creator.getNickname()));
            List<String> adminNames = new ArrayList<>();
            for (Long adminUserId : managerUserIdListMap.getOrDefault(projectVO.getId(), Collections.emptyList())) {
                findAndThen(userMap, adminUserId, admin -> adminNames.add(admin.getNickname()));
            }
            Map<Integer, Long> statusCountMap = projectStatusCountMap.getOrDefault(
                    projectVO.getId(), Collections.emptyMap());
            projectVO.setAdminNames(adminNames).setMemberCount(memberCountMap.getOrDefault(projectVO.getId(), 0))
                    .setPendingWorkItemCount(statusCountMap.getOrDefault(PmsWorkItemStatusTypeEnum.PENDING.getType(), 0L))
                    .setProcessingWorkItemCount(statusCountMap.getOrDefault(PmsWorkItemStatusTypeEnum.PROCESSING.getType(), 0L))
                    .setCompletedWorkItemCount(statusCountMap.getOrDefault(PmsWorkItemStatusTypeEnum.COMPLETED.getType(), 0L))
                    .setOwnerStatus(superAdmin || ownerProjectIds.contains(projectVO.getId()))
                    .setAdminStatus(superAdmin || managedProjectIds.contains(projectVO.getId()))
                    .setMemberStatus(memberProjectIds.contains(projectVO.getId()))
                    .setExitStatus(memberProjectIds.contains(projectVO.getId()) && !ownerProjectIds.contains(projectVO.getId()))
                    .setWriteStatus(superAdmin || writableProjectIds.contains(projectVO.getId()))
                    .setFavoriteStatus(favoriteProjectIds.contains(projectVO.getId()));
        });
    }

}
