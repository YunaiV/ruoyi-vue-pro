package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.member.PmsKnowledgeLibraryUpdateMemberListReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMemberMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeLibraryMemberLevelEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.equalsAny;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_ACCESS_DENIED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_ADMIN_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_CREATOR_CANNOT_REMOVE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_DIRECT_MEMBER_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_MEMBERS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_WRITE_ACCESS_DENIED;

/**
 * PMS 知识库成员 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeLibraryMemberServiceImpl implements PmsKnowledgeLibraryMemberService {

    @Resource
    private PmsKnowledgeLibraryMemberMapper memberMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeLibraryService libraryService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeContentPermissionService contentPermissionService;

    @Resource
    private PermissionApi permissionApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Override
    public PmsKnowledgeLibraryDO validateLibraryReadable(Long libraryId, Long userId) {
        PmsKnowledgeLibraryDO library = validateLibraryExists(libraryId);
        if (Boolean.FALSE.equals(library.getOpenStatus()) && !isLibraryMember(libraryId, userId)) {
            throw exception(KNOWLEDGE_LIBRARY_ACCESS_DENIED);
        }
        return library;
    }

    @Override
    public PmsKnowledgeLibraryDO validateLibraryWritable(Long libraryId, Long userId) {
        PmsKnowledgeLibraryDO library = validateLibraryExists(libraryId);
        if (!isLibraryWritable(libraryId, userId)) {
            throw exception(KNOWLEDGE_LIBRARY_WRITE_ACCESS_DENIED);
        }
        return library;
    }

    @Override
    public PmsKnowledgeLibraryDO validateLibraryAdmin(Long libraryId, Long userId) {
        PmsKnowledgeLibraryDO library = validateLibraryExists(libraryId);
        if (!isLibraryAdmin(libraryId, userId)) {
            throw exception(KNOWLEDGE_LIBRARY_ADMIN_REQUIRED);
        }
        return library;
    }

    @Override
    public PmsKnowledgeLibraryDO validateLibraryCreator(Long libraryId, Long userId) {
        PmsKnowledgeLibraryDO library = validateLibraryExists(libraryId);
        if (isSuperAdmin(userId)) {
            return library;
        }
        PmsKnowledgeLibraryMemberDO member = getLibraryMember(libraryId, userId);
        if (member == null || notEqual(PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel(), member.getLevel())) {
            throw exception(KNOWLEDGE_LIBRARY_ADMIN_REQUIRED);
        }
        return library;
    }

    @Override
    public boolean isLibraryWritable(Long libraryId, Long userId) {
        return isSuperAdmin(userId) || getLibraryMember(libraryId, userId) != null;
    }

    @Override
    public boolean isLibraryAdmin(Long libraryId, Long userId) {
        if (isSuperAdmin(userId)) {
            return true;
        }
        PmsKnowledgeLibraryMemberDO member = getLibraryMember(libraryId, userId);
        if (member == null) {
            return false;
        }
        // 公开库也允许管理员参与成员和内容协作；创建人权限单独用于公开状态和删除等高危操作
        return equalsAny(member.getLevel(), PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel(),
                PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel());
    }

    @Override
    public List<Long> getJoinedLibraryIdList(Long userId) {
        // 1. 查询当前用户及所在部门加入的知识库编号
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        List<Long> joinedLibraryIds = getLibraryIdListByUserIdOrDeptId(
                userId, user != null ? user.getDeptId() : null);

        // 2. 过滤已删除或已进入回收站的知识库
        Set<Long> libraryIds = new LinkedHashSet<>(joinedLibraryIds);
        libraryIds.retainAll(libraryService.getLibraryIdList(null));
        return convertList(libraryIds, item -> item);
    }

    @Override
    public List<Long> getReadableLibraryIdList(Long userId) {
        // 1. 超级管理员可读取全部知识库
        if (isSuperAdmin(userId)) {
            return libraryService.getLibraryIdList(null);
        }

        // 2. 查询公开知识库和自己加入的知识库
        List<Long> openLibraryIds = libraryService.getLibraryIdList(true);
        List<Long> joinedLibraryIds = getJoinedLibraryIdList(userId);

        // 3. 合并并去重可读知识库编号
        Set<Long> libraryIds = new LinkedHashSet<>(openLibraryIds);
        libraryIds.addAll(joinedLibraryIds);
        return convertList(libraryIds, item -> item);
    }

    @Override
    public void createLibraryMemberList(Long libraryId, Long creatorUserId,
                                        Collection<Long> adminUserIds, Collection<Long> memberUserIds) {
        // 1.1 整理初始成员并校验重复、创建人保护
        List<Long> adminUserIdList = CollUtil.isEmpty(adminUserIds)
                ? new ArrayList<>() : new ArrayList<>(adminUserIds);
        List<Long> memberUserIdList = CollUtil.isEmpty(memberUserIds)
                ? new ArrayList<>() : new ArrayList<>(memberUserIds);
        Set<Long> initialMemberUserIds = new LinkedHashSet<>();
        // 两个角色数组统一校验创建人保护和重复用户，避免维护两套不一致的校验分支
        for (Long userId : adminUserIdList) {
            if (userId == null || Objects.equals(creatorUserId, userId)
                    || !initialMemberUserIds.add(userId)) {
                throw exception(KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
            }
        }
        for (Long userId : memberUserIdList) {
            if (userId == null || Objects.equals(creatorUserId, userId)
                    || !initialMemberUserIds.add(userId)) {
                throw exception(KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
            }
        }
        // 1.2 校验初始成员账号存在
        adminUserApi.validateUserList(initialMemberUserIds);

        // 2. 创建创建人和初始成员
        List<PmsKnowledgeLibraryMemberDO> members = new ArrayList<>();
        members.addAll(convertList(adminUserIdList, userId -> new PmsKnowledgeLibraryMemberDO()
                .setLibraryId(libraryId).setUserId(userId)
                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel())));
        members.addAll(convertList(memberUserIdList, userId -> new PmsKnowledgeLibraryMemberDO()
                .setLibraryId(libraryId).setUserId(userId)
                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel())));
        members.add(0, new PmsKnowledgeLibraryMemberDO().setLibraryId(libraryId).setUserId(creatorUserId)
                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel()));
        memberMapper.insertBatch(members);
    }

    @Override
    public List<PmsKnowledgeLibraryMemberDO> getLibraryMemberList(Long libraryId, Long userId) {
        validateLibraryReadable(libraryId, userId);
        return memberMapper.selectListByLibraryId(libraryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLibraryMemberList(PmsKnowledgeLibraryUpdateMemberListReqVO updateReqVO, Long userId) {
        // 1.1 校验知识库管理员权限
        validateLibraryAdmin(updateReqVO.getLibraryId(), userId);
        // 1.2 校验成员不重复
        Set<Long> memberUserIds = convertSet(updateReqVO.getMembers(),
                PmsKnowledgeLibraryUpdateMemberListReqVO.Member::getUserId);
        Set<Long> memberDeptIds = convertSet(updateReqVO.getMembers(),
                PmsKnowledgeLibraryUpdateMemberListReqVO.Member::getDeptId);
        List<PmsKnowledgeLibraryMemberDO> oldMembers =
                memberMapper.selectListByLibraryId(updateReqVO.getLibraryId());
        PmsKnowledgeLibraryMemberDO creator = CollUtil.findOne(oldMembers, member ->
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel().equals(member.getLevel()));
        if (memberUserIds.size() + memberDeptIds.size() != updateReqVO.getMembers().size()) {
            throw exception(KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
        }
        // 1.3 校验成员不包含知识库创建人
        if (creator != null && memberUserIds.contains(creator.getUserId())) {
            throw exception(KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
        }
        // 1.4 校验成员不能伪造创建人等级
        if (CollUtil.findOne(updateReqVO.getMembers(), member ->
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel().equals(member.getLevel())) != null) {
            throw exception(KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
        }
        // 1.5 校验用户成员存在
        adminUserApi.validateUserList(memberUserIds);
        // 1.6 校验部门成员存在
        deptApi.validateDeptList(memberDeptIds);

        // 2. 保留创建人并重建管理员和普通成员
        memberMapper.deleteByLibraryIdAndLevels(updateReqVO.getLibraryId(), Arrays.asList(
                PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel(),
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));
        if (CollUtil.isNotEmpty(updateReqVO.getMembers())) {
            memberMapper.insertBatch(convertList(updateReqVO.getMembers(), member ->
                    BeanUtils.toBean(member, PmsKnowledgeLibraryMemberDO.class)
                            .setLibraryId(updateReqVO.getLibraryId())));
        }

        // 3. 移除已退出知识库成员的内容级协作权限
        Set<Long> retainedUserIds = new LinkedHashSet<>(memberUserIds);
        if (creator != null) {
            retainedUserIds.add(creator.getUserId());
        }
        Set<Long> removedUserIds = convertSet(oldMembers, PmsKnowledgeLibraryMemberDO::getUserId);
        removedUserIds.removeAll(retainedUserIds);
        Set<Long> removedDeptIds = convertSet(oldMembers, PmsKnowledgeLibraryMemberDO::getDeptId);
        removedDeptIds.removeAll(memberDeptIds);
        contentPermissionService.deleteContentPermissionMembersByLibraryId(updateReqVO.getLibraryId(), removedUserIds, removedDeptIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitLibrary(Long libraryId, Long userId) {
        // 1.1 校验知识库可访问
        validateLibraryReadable(libraryId, userId);
        // 1.2 校验当前账号是直接成员
        PmsKnowledgeLibraryMemberDO member = memberMapper.selectByLibraryIdAndUserId(libraryId, userId);
        if (member == null) {
            throw exception(KNOWLEDGE_LIBRARY_DIRECT_MEMBER_NOT_EXISTS);
        }
        // 1.3 校验知识库创建人不能主动退出
        if (PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel().equals(member.getLevel())) {
            throw exception(KNOWLEDGE_LIBRARY_CREATOR_CANNOT_REMOVE);
        }

        // 2. 退出知识库并清理当前账号的内容级协作权限
        memberMapper.deleteByLibraryIdAndUserId(libraryId, userId);
        contentPermissionService.deleteContentPermissionMembersByLibraryId(libraryId, Collections.singleton(userId),
                Collections.emptySet());
    }

    @Override
    public void deleteLibraryMembersByLibraryId(Long libraryId) {
        memberMapper.deleteByLibraryId(libraryId);
    }

    @Override
    public Map<Long, List<PmsKnowledgeLibraryMemberDO>> getLibraryMemberListMap(Collection<Long> libraryIds) {
        if (CollUtil.isEmpty(libraryIds)) {
            return Collections.emptyMap();
        }
        return convertMultiMap(memberMapper.selectListByLibraryIds(libraryIds),
                PmsKnowledgeLibraryMemberDO::getLibraryId);
    }

    @Override
    public PmsKnowledgeLibraryMemberDO getMemberByLibraryIdAndUserId(Long libraryId, Long userId) {
        return memberMapper.selectByLibraryIdAndUserId(libraryId, userId);
    }

    @Override
    public PmsKnowledgeLibraryMemberDO getMemberByLibraryIdAndDeptId(Long libraryId, Long deptId) {
        return memberMapper.selectByLibraryIdAndDeptId(libraryId, deptId);
    }

    @Override
    public List<Long> getLibraryIdListByUserIdOrDeptId(Long userId, Long deptId) {
        return memberMapper.selectLibraryIdListByUserIdOrDeptId(userId, deptId);
    }

    private PmsKnowledgeLibraryDO validateLibraryExists(Long libraryId) {
        PmsKnowledgeLibraryDO library = libraryService.getLibrary(libraryId);
        if (library == null || notEqual(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(), library.getStatus())) {
            throw exception(KNOWLEDGE_LIBRARY_NOT_EXISTS);
        }
        return library;
    }

    private boolean isSuperAdmin(Long userId) {
        return permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());
    }

    private boolean isLibraryMember(Long libraryId, Long userId) {
        return isSuperAdmin(userId) || getLibraryMember(libraryId, userId) != null;
    }

    private PmsKnowledgeLibraryMemberDO getLibraryMember(Long libraryId, Long userId) {
        PmsKnowledgeLibraryMemberDO member = getMemberByLibraryIdAndUserId(libraryId, userId);
        if (member != null) {
            return member;
        }
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        if (user == null || user.getDeptId() == null) {
            return null;
        }
        return getMemberByLibraryIdAndDeptId(libraryId, user.getDeptId());
    }

}
