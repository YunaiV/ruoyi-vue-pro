package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member.PmsProjectMemberRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member.PmsProjectMemberSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectMemberDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectMemberMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectMemberLevelEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertLinkedSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_ADMIN_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_MEMBER_CREATOR_CANNOT_REMOVE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_MEMBER_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_MEMBER_OWNER_CANNOT_EXIT;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_MEMBER_OWNER_LEVEL_CANNOT_ASSIGN;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_WRITE_ACCESS_DENIED;

/**
 * PMS 项目成员 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsProjectMemberServiceImpl implements PmsProjectMemberService {

    @Resource
    private PmsProjectMemberMapper projectMemberMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsProjectService projectService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PermissionApi permissionApi;

    @Override
    public List<Long> getActiveProjectIdListByUserId(Long userId) {
        // 1. 查询用户参与的项目编号并去重
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByUserId(userId);
        Set<Long> projectIds = convertLinkedSet(members, PmsProjectMemberDO::getProjectId);
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyList();
        }

        // 2. 只保留进行中的项目
        List<PmsProjectDO> projects = projectService.getProjectList(projectIds);
        return convertList(projects, PmsProjectDO::getId,
                project -> PmsProjectStatusEnum.ACTIVE.getStatus().equals(project.getStatus()));
    }

    @Override
    public PmsProjectDO validateProjectMember(Long projectId, Long userId) {
        // 1. 校验项目存在
        PmsProjectDO project = validateProjectExists(projectId);

        // 2. 校验用户是项目成员
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw exception(PROJECT_ACCESS_DENIED);
        }
        return project;
    }

    @Override
    public PmsProjectDO validateProjectReadable(Long projectId, Long userId) {
        // 1. 校验项目存在
        PmsProjectDO project = validateProjectExists(projectId);

        // 2. 公开项目允许查看，私有项目只允许项目成员或超级管理员查看
        if (Boolean.FALSE.equals(project.getOpenStatus())
                && projectMemberMapper.selectByProjectIdAndUserId(projectId, userId) == null
                && !isSuperAdmin(userId)) {
            throw exception(PROJECT_ACCESS_DENIED);
        }
        return project;
    }

    @Override
    public PmsProjectDO validateProjectWritable(Long projectId, Long userId) {
        // 1.1 校验项目存在
        PmsProjectDO project = validateProjectExists(projectId);
        // 1.2 超级管理员直接通过
        if (isSuperAdmin(userId)) {
            return project;
        }

        // 2. 校验项目成员权限级别允许编辑
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null || !PmsProjectMemberLevelEnum.isWritable(member.getLevel())) {
            throw exception(PROJECT_WRITE_ACCESS_DENIED);
        }
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProjectMemberList(Long projectId, Long creatorId, Collection<Long> memberUserIds) {
        // 1. 校验初始成员
        Set<Long> userIds = new LinkedHashSet<>();
        CollectionUtils.addIfNotNull(userIds, creatorId);
        if (CollUtil.isNotEmpty(memberUserIds)) {
            userIds.addAll(memberUserIds);
        }
        adminUserApi.validateUserList(userIds);

        // 2. 创建人作为项目拥有者，其余成员使用默认编辑级别
        List<PmsProjectMemberDO> members = convertList(userIds, userId -> new PmsProjectMemberDO()
                .setProjectId(projectId).setUserId(userId)
                .setLevel(ObjectUtil.equal(creatorId, userId) ? PmsProjectMemberLevelEnum.OWNER.getLevel() : PmsProjectMemberLevelEnum.WRITE.getLevel()));
        projectMemberMapper.insertBatch(members);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProjectMemberList(PmsProjectMemberSaveReqVO saveReqVO, Long operatorUserId) {
        // 1.1 校验操作人可以管理项目
        PmsProjectDO project = validateProjectManager(saveReqVO.getProjectId(), operatorUserId);
        // 1.2 校验成员编号不重复
        List<Long> userIdList = convertList(saveReqVO.getMembers(), PmsProjectMemberSaveReqVO.Member::getUserId);
        Set<Long> userIds = new LinkedHashSet<>(userIdList);
        if (userIds.size() != userIdList.size()) {
            throw exception(PROJECT_MEMBER_NOT_EXISTS);
        }
        // 1.3 校验成员账号
        adminUserApi.validateUserList(userIds);
        // 1.4 校验成员权限级别
        Long creatorId = NumberUtils.parseLong(project.getCreator());
        for (PmsProjectMemberSaveReqVO.Member member : saveReqVO.getMembers()) {
            if (ObjectUtil.equal(creatorId, member.getUserId())) {
                throw exception(PROJECT_MEMBER_CREATOR_CANNOT_REMOVE);
            }
            if (PmsProjectMemberLevelEnum.isOwner(member.getLevel())) {
                throw exception(PROJECT_MEMBER_OWNER_LEVEL_CANNOT_ASSIGN);
            }
        }

        // 2. 删除后按请求保存每名成员的权限级别
        projectMemberMapper.deleteByProjectIdAndUserIds(saveReqVO.getProjectId(), userIds);
        projectMemberMapper.insertBatch(convertList(saveReqVO.getMembers(), member ->
                BeanUtils.toBean(member, PmsProjectMemberDO.class).setProjectId(saveReqVO.getProjectId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProjectMember(Long projectId, Long userId, Long operatorUserId) {
        // 1.1 校验操作人可以管理项目
        PmsProjectDO project = validateProjectManager(projectId, operatorUserId);
        // 1.2 校验待移除成员存在
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw exception(PROJECT_MEMBER_NOT_EXISTS);
        }
        if (ObjectUtil.equal(NumberUtils.parseLong(project.getCreator()), userId)
                || PmsProjectMemberLevelEnum.isOwner(member.getLevel())) {
            throw exception(PROJECT_MEMBER_CREATOR_CANNOT_REMOVE);
        }

        // 2. 移除项目成员
        projectMemberMapper.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitProject(Long projectId, Long userId) {
        // 1.1 校验项目可访问
        validateProjectReadable(projectId, userId);
        // 1.2 校验当前账号是直接成员
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw exception(PROJECT_MEMBER_NOT_EXISTS);
        }
        // 1.3 校验项目所有者不能主动退出
        if (PmsProjectMemberLevelEnum.isOwner(member.getLevel())) {
            throw exception(PROJECT_MEMBER_OWNER_CANNOT_EXIT);
        }

        // 2. 退出项目
        projectMemberMapper.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public void deleteProjectMemberListByProjectId(Long projectId) {
        projectMemberMapper.deleteByProjectId(projectId);
    }

    @Override
    public List<PmsProjectMemberRespVO> getProjectMemberList(Long projectId, Long userId) {
        // 1. 校验当前用户可以访问项目
        PmsProjectDO project = validateProjectReadable(projectId, userId);

        // 2. 转换项目成员
        Long creatorId = NumberUtils.parseLong(project.getCreator());
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByProjectId(projectId);
        return convertList(members, member -> new PmsProjectMemberRespVO()
                .setUserId(member.getUserId()).setLevel(member.getLevel())
                .setCreatorStatus(ObjectUtil.equal(member.getUserId(), creatorId)));
    }

    @Override
    public List<Long> getProjectIdListByUserId(Long userId) {
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByUserId(userId);
        return convertList(members, PmsProjectMemberDO::getProjectId);
    }

    @Override
    public List<Long> getOwnerProjectIdListByUserId(Long userId) {
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByUserIdAndLevels(userId,
                Collections.singleton(PmsProjectMemberLevelEnum.OWNER.getLevel()));
        return convertList(members, PmsProjectMemberDO::getProjectId);
    }

    @Override
    public List<Long> getManagedProjectIdListByUserId(Long userId) {
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByUserIdAndLevels(
                userId, PmsProjectMemberLevelEnum.MANAGER_LEVELS);
        return convertList(members, PmsProjectMemberDO::getProjectId);
    }

    @Override
    public List<Long> getWritableProjectIdListByUserId(Long userId) {
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByUserIdAndLevels(
                userId, PmsProjectMemberLevelEnum.WRITABLE_LEVELS);
        return convertList(members, PmsProjectMemberDO::getProjectId);
    }

    @Override
    public Map<Long, List<Long>> getProjectManagerUserIdListMap(Collection<Long> projectIds) {
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyMap();
        }
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByProjectIdsAndLevels(
                projectIds, PmsProjectMemberLevelEnum.MANAGER_LEVELS);
        return convertMultiMap(members, PmsProjectMemberDO::getProjectId, PmsProjectMemberDO::getUserId);
    }

    @Override
    public Map<Long, Integer> getProjectMemberCountMap(Collection<Long> projectIds) {
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyMap();
        }
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByProjectIds(projectIds);
        return CollUtil.countMap(convertList(members, PmsProjectMemberDO::getProjectId));
    }

    @Override
    public boolean hasProjectOwnerPermission(Long projectId, Long userId) {
        if (isSuperAdmin(userId)) {
            return true;
        }
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        return member != null && PmsProjectMemberLevelEnum.isOwner(member.getLevel());
    }

    @Override
    public boolean hasProjectManagerPermission(Long projectId, Long userId) {
        if (isSuperAdmin(userId)) {
            return true;
        }
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        return member != null && PmsProjectMemberLevelEnum.isManager(member.getLevel());
    }

    @Override
    public void validateProjectMemberExists(Long projectId, Long userId) {
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw exception(PROJECT_MEMBER_NOT_EXISTS);
        }
    }

    @Override
    public void validateProjectMemberList(Long projectId, Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByProjectId(projectId);
        Set<Long> projectMemberUserIds = convertSet(members, PmsProjectMemberDO::getUserId);
        if (!projectMemberUserIds.containsAll(userIds)) {
            throw exception(PROJECT_MEMBER_NOT_EXISTS);
        }
    }

    /**
     * 校验用户可以管理项目
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     * @return 项目
     */
    private PmsProjectDO validateProjectManager(Long projectId, Long userId) {
        PmsProjectDO project = validateProjectReadable(projectId, userId);
        if (!hasProjectManagerPermission(projectId, userId)) {
            throw exception(PROJECT_ADMIN_REQUIRED);
        }
        return project;
    }

    /**
     * 判断用户是否为系统超级管理员
     *
     * @param userId 后台用户编号
     * @return 是否为超级管理员
     */
    private boolean isSuperAdmin(Long userId) {
        return permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());
    }

    /**
     * 校验项目存在
     *
     * @param projectId 项目编号
     * @return 项目
     */
    private PmsProjectDO validateProjectExists(Long projectId) {
        PmsProjectDO project = projectService.getProject(projectId);
        if (project == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        return project;
    }

}
