package cn.iocoder.yudao.module.pms.controller.admin.pm.workbench;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchCountRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchIterationRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchWorkItemRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectService;
import cn.iocoder.yudao.module.pms.service.pm.workbench.PmsWorkbenchService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemStatusService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 工作台")
@RestController
@RequestMapping("/pms/pm/workbench")
@Validated
public class PmsWorkbenchController {

    @Resource
    private PmsWorkbenchService workbenchService;
    @Resource
    private PmsProjectService projectService;
    @Resource
    private PmsProjectMemberService projectMemberService;
    @Resource
    private PmsWorkItemStatusService workItemStatusService;
    @Resource
    private PermissionApi permissionApi;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/work-item-page")
    @Operation(summary = "获得我负责的工作项分页")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<PageResult<PmsWorkbenchWorkItemRespVO>> getWorkbenchWorkItemPage(
            @Valid PmsWorkbenchPageReqVO pageReqVO) {
        // 1. 查询当前用户负责的工作项分页
        Long userId = getLoginUserId();
        PageResult<PmsWorkItemDO> pageResult = workbenchService.getWorkbenchWorkItemPage(pageReqVO, userId);
        // 2.1 批量查询项目、状态及项目写权限
        Map<Long, PmsProjectDO> projectMap = projectService.getProjectMap(
                convertSet(pageResult.getList(), PmsWorkItemDO::getProjectId));
        Map<Long, PmsWorkItemStatusDO> statusMap = workItemStatusService.getWorkItemStatusMap(
                convertSet(pageResult.getList(), PmsWorkItemDO::getStatusId));
        Set<Long> writableProjectIds = convertSet(projectMemberService.getWritableProjectIdListByUserId(userId));
        boolean superAdmin = permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());
        // 2.2 批量查询负责人和创建人
        Set<Long> userIds = convertSet(pageResult.getList(), PmsWorkItemDO::getAssigneeUserId);
        userIds.addAll(convertSet(pageResult.getList(), workItem -> NumberUtils.parseLong(workItem.getCreator())));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, Long> creatorUserIdMap = convertMap(pageResult.getList(), PmsWorkItemDO::getId,
                workItem -> NumberUtils.parseLong(workItem.getCreator()));
        // 3. 转换响应 VO，并拼接项目、状态、用户及写权限
        List<PmsWorkbenchWorkItemRespVO> workItemVOList = BeanUtils.toBean(pageResult.getList(),
                PmsWorkbenchWorkItemRespVO.class, itemVO -> {
                    findAndThen(projectMap, itemVO.getProjectId(), project -> itemVO
                            .setProjectName(project.getName()).setProjectType(project.getType()));
                    findAndThen(statusMap, itemVO.getStatusId(), status -> itemVO.setStatusName(status.getName()));
                    findAndThen(userMap, itemVO.getAssigneeUserId(),
                            user -> itemVO.setAssigneeUserName(user.getNickname()));
                    itemVO.setCreatorUserId(creatorUserIdMap.get(itemVO.getId()));
                    findAndThen(userMap, itemVO.getCreatorUserId(),
                            user -> itemVO.setCreatorUserName(user.getNickname()));
                    itemVO.setWriteStatus(superAdmin || writableProjectIds.contains(itemVO.getProjectId()));
                });
        return success(new PageResult<>(workItemVOList, pageResult.getTotal()));
    }

    @GetMapping("/iteration-page")
    @Operation(summary = "获得我负责的迭代分页")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:query')")
    public CommonResult<PageResult<PmsWorkbenchIterationRespVO>> getWorkbenchIterationPage(
            @Valid PmsWorkbenchPageReqVO pageReqVO) {
        // 1.1 查询当前用户负责的迭代分页
        PageResult<PmsIterationDO> pageResult = workbenchService.getWorkbenchIterationPage(pageReqVO, getLoginUserId());
        // 1.2 批量查询关联项目
        Map<Long, PmsProjectDO> projectMap = projectService.getProjectMap(
                convertSet(pageResult.getList(), PmsIterationDO::getProjectId));
        // 2. 转换响应 VO，并拼接项目展示字段
        List<PmsWorkbenchIterationRespVO> iterationVOList = BeanUtils.toBean(pageResult.getList(),
                PmsWorkbenchIterationRespVO.class, itemVO ->
                        findAndThen(projectMap, itemVO.getProjectId(), project -> itemVO
                                .setProjectName(project.getName()).setProjectType(project.getType())));
        return success(new PageResult<>(iterationVOList, pageResult.getTotal()));
    }

    @GetMapping("/count")
    @Operation(summary = "获得我的未完成事项数量")
    @PreAuthorize("@ss.hasAnyPermissions('pms:pm:work-item:query', 'pms:pm:iteration:query')")
    public CommonResult<PmsWorkbenchCountRespVO> getWorkbenchCount(PmsWorkbenchPageReqVO pageReqVO) {
        return success(workbenchService.getWorkbenchCount(pageReqVO, getLoginUserId()));
    }

}
