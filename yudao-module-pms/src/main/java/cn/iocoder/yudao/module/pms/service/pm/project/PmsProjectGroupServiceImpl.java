package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectGroupDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectGroupRelationDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectGroupMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectGroupRelationMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectGroupTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_DEFAULT_CANNOT_MODIFY;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_NOT_EXISTS;

/**
 * PMS 项目分组 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsProjectGroupServiceImpl implements PmsProjectGroupService {

    @Resource
    private PmsProjectGroupMapper projectGroupMapper;
    @Resource
    private PmsProjectGroupRelationMapper projectGroupRelationMapper;
    @Resource
    private PmsProjectMemberService projectMemberService;

    @Override
    public Long createProjectGroup(PmsProjectGroupSaveReqVO createReqVO, Long userId) {
        // 1. 校验分组名称唯一
        validateProjectGroupNameDuplicate(userId, createReqVO.getName(), null);

        // 2. 创建自定义分组，并追加到现有分组末尾
        PmsProjectGroupDO lastGroup = projectGroupMapper.selectLastByUserId(userId);
        PmsProjectGroupDO group = BeanUtils.toBean(createReqVO, PmsProjectGroupDO.class)
                .setUserId(userId)
                .setSort(lastGroup == null ? 2 : lastGroup.getSort() + 1)
                .setType(PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(group);
        return group.getId();
    }

    @Override
    public void updateProjectGroup(PmsProjectGroupSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验分组存在
        PmsProjectGroupDO group = validateProjectGroupExists(updateReqVO.getId(), userId);
        // 1.2 校验分组为自定义分组
        if (ObjectUtil.notEqual(PmsProjectGroupTypeEnum.CUSTOM.getType(), group.getType())) {
            throw exception(PROJECT_GROUP_DEFAULT_CANNOT_MODIFY);
        }
        // 1.3 校验分组名称唯一
        validateProjectGroupNameDuplicate(userId, updateReqVO.getName(), updateReqVO.getId());

        // 2. 更新分组名称
        projectGroupMapper.updateById(BeanUtils.toBean(updateReqVO, PmsProjectGroupDO.class)
                .setUserId(null).setSort(null).setType(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProjectGroupSort(PmsProjectGroupSortReqVO sortReqVO, Long userId) {
        // 1. 校验排序列表中的分组都属于当前用户
        Set<Long> groupIds = convertSet(sortReqVO.getItems(), PmsProjectGroupSortReqVO.Item::getId);
        if (projectGroupMapper.selectListByIdsAndUserId(groupIds, userId).size() != groupIds.size()) {
            throw exception(PROJECT_GROUP_NOT_EXISTS);
        }

        // 2. 更新全部分组的显示顺序
        projectGroupMapper.updateBatch(convertList(sortReqVO.getItems(),
                item -> new PmsProjectGroupDO().setId(item.getId()).setSort(item.getSort())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProjectGroup(Long id, Long userId) {
        // 1.1 校验分组存在
        PmsProjectGroupDO group = validateProjectGroupExists(id, userId);
        // 1.2 校验分组为自定义分组
        if (ObjectUtil.notEqual(PmsProjectGroupTypeEnum.CUSTOM.getType(), group.getType())) {
            throw exception(PROJECT_GROUP_DEFAULT_CANNOT_DELETE);
        }

        // 2. 删除分组及其项目关系
        projectGroupRelationMapper.deleteByUserIdAndGroupId(userId, id);
        projectGroupMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PmsProjectGroupDO> getProjectGroupList(Long userId) {
        // 1. 查询当前用户的项目分组
        List<PmsProjectGroupDO> groups = projectGroupMapper.selectListByUserId(userId);

        // 2. 确保“全部项目”和“未分组”两个系统分组存在
        ensureDefaultProjectGroupList(userId, groups);
        return groups;
    }

    @Override
    public Map<Long, Integer> getProjectGroupCountMap(Long userId, Collection<PmsProjectGroupDO> groups,
                                                      Collection<Long> projectIds) {
        if (CollUtil.isEmpty(groups)) {
            return Collections.emptyMap();
        }
        // 1. 聚合统计各自定义分组的进行中项目数量
        Map<Long, Integer> groupCountMap = CollUtil.isEmpty(projectIds) ? new LinkedHashMap<>()
                : projectGroupRelationMapper.selectGroupCountMapByUserIdAndProjectIds(userId, projectIds);
        int groupedProjectCount = CollUtil.isEmpty(projectIds) ? 0
                : projectGroupRelationMapper.selectProjectCountByUserIdAndProjectIds(userId, projectIds);
        // 2. 补充“全部项目”和“未分组”的项目数量
        int projectCount = CollUtil.size(projectIds);
        for (PmsProjectGroupDO group : groups) {
            if (PmsProjectGroupTypeEnum.ALL.getType().equals(group.getType())) {
                groupCountMap.put(group.getId(), projectCount);
            } else if (PmsProjectGroupTypeEnum.UNGROUPED.getType().equals(group.getType())) {
                groupCountMap.put(group.getId(), projectCount - groupedProjectCount);
            }
        }
        return groupCountMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveProjectToGroup(PmsProjectGroupMoveReqVO moveReqVO, Long userId) {
        // 1. 校验当前用户是项目成员
        projectMemberService.validateProjectMember(moveReqVO.getProjectId(), userId);

        // 2. 移出分组时，删除已有的个人分组关系
        if (moveReqVO.getGroupId() == null) {
            projectGroupRelationMapper.deleteByUserIdAndProjectId(userId, moveReqVO.getProjectId());
            return;
        }

        // 3. 校验目标分组；移动到“未分组”等同于移出分组，“全部项目”不能作为目标分组
        PmsProjectGroupDO targetGroup = validateProjectGroupExists(moveReqVO.getGroupId(), userId);
        if (PmsProjectGroupTypeEnum.UNGROUPED.getType().equals(targetGroup.getType())) {
            projectGroupRelationMapper.deleteByUserIdAndProjectId(userId, moveReqVO.getProjectId());
            return;
        }
        if (PmsProjectGroupTypeEnum.ALL.getType().equals(targetGroup.getType())) {
            throw exception(PROJECT_GROUP_DEFAULT_CANNOT_MODIFY);
        }

        // 4. 新增或更新个人项目分组关系
        PmsProjectGroupRelationDO relation = projectGroupRelationMapper.selectByUserIdAndProjectId(
                userId, moveReqVO.getProjectId());
        if (relation == null) {
            projectGroupRelationMapper.insert(BeanUtils.toBean(moveReqVO, PmsProjectGroupRelationDO.class)
                    .setUserId(userId).setSort(0));
            return;
        }
        projectGroupRelationMapper.updateById(BeanUtils.toBean(moveReqVO, PmsProjectGroupRelationDO.class)
                .setId(relation.getId()).setUserId(null).setProjectId(null).setSort(null));
    }

    @Override
    public List<Long> filterProjectIdListByGroupId(Long groupId, Long userId, Collection<Long> projectIds) {
        if (CollUtil.isEmpty(projectIds) || groupId == null) {
            return CollUtil.isEmpty(projectIds) ? Collections.emptyList()
                    : new ArrayList<>(new LinkedHashSet<>(projectIds));
        }

        // 1. 校验分组存在；情况一：“全部项目”不额外过滤
        PmsProjectGroupDO group = validateProjectGroupExists(groupId, userId);
        if (PmsProjectGroupTypeEnum.ALL.getType().equals(group.getType())) {
            return new ArrayList<>(new LinkedHashSet<>(projectIds));
        }

        // 2. 获得候选项目编号集合
        Set<Long> candidateProjectIds = new LinkedHashSet<>(projectIds);
        // 情况二：“未分组”排除已经加入任意自定义分组的项目
        if (PmsProjectGroupTypeEnum.UNGROUPED.getType().equals(group.getType())) {
            List<PmsProjectGroupRelationDO> relations = projectGroupRelationMapper.selectListByUserIdAndProjectIds(
                    userId, candidateProjectIds);
            candidateProjectIds.removeAll(convertSet(relations, PmsProjectGroupRelationDO::getProjectId));
            return new ArrayList<>(candidateProjectIds);
        }
        // 情况三：自定义分组只保留属于该分组的项目
        List<PmsProjectGroupRelationDO> relations = projectGroupRelationMapper.selectListByUserIdAndGroupId(userId, groupId);
        candidateProjectIds.retainAll(convertSet(relations, PmsProjectGroupRelationDO::getProjectId));
        return new ArrayList<>(candidateProjectIds);
    }

    @Override
    public void deleteProjectGroupRelationListByProjectId(Long projectId) {
        projectGroupRelationMapper.deleteByProjectId(projectId);
    }

    /**
     * 确保用户拥有“全部项目”和“未分组”两个系统分组
     *
     * @param userId 后台用户编号
     * @param groups 当前已查询到的项目分组
     */
    private void ensureDefaultProjectGroupList(Long userId, List<PmsProjectGroupDO> groups) {
        // 1. 检查两个系统分组是否已经存在
        boolean hasAllGroup = CollUtil.findOne(groups,
                group -> PmsProjectGroupTypeEnum.ALL.getType().equals(group.getType())) != null;
        boolean hasUngroupedGroup = CollUtil.findOne(groups,
                group -> PmsProjectGroupTypeEnum.UNGROUPED.getType().equals(group.getType())) != null;
        if (hasAllGroup && hasUngroupedGroup) {
            return;
        }

        // 2. 构造并插入缺失的系统分组
        List<PmsProjectGroupDO> defaultGroups = new ArrayList<>();
        if (!hasAllGroup) {
            defaultGroups.add(new PmsProjectGroupDO().setUserId(userId)
                    .setName(PmsProjectGroupTypeEnum.ALL.getName()).setSort(0)
                    .setType(PmsProjectGroupTypeEnum.ALL.getType()));
        }
        if (!hasUngroupedGroup) {
            defaultGroups.add(new PmsProjectGroupDO().setUserId(userId)
                    .setName(PmsProjectGroupTypeEnum.UNGROUPED.getName()).setSort(1)
                    .setType(PmsProjectGroupTypeEnum.UNGROUPED.getType()));
        }
        projectGroupMapper.insertBatch(defaultGroups);

        // 3. 合并本次新增记录并保持稳定排序
        groups.addAll(defaultGroups);
        groups.sort(Comparator.comparing(PmsProjectGroupDO::getSort,
                Comparator.nullsLast(Integer::compareTo)).thenComparing(PmsProjectGroupDO::getId));
    }

    /**
     * 校验项目分组存在，并且属于当前用户
     *
     * @param id 项目分组编号
     * @param userId 后台用户编号
     * @return 项目分组
     */
    private PmsProjectGroupDO validateProjectGroupExists(Long id, Long userId) {
        PmsProjectGroupDO group = projectGroupMapper.selectByIdAndUserId(id, userId);
        if (group == null) {
            throw exception(PROJECT_GROUP_NOT_EXISTS);
        }
        return group;
    }

    /**
     * 校验当前用户的项目分组名称唯一
     *
     * @param userId 后台用户编号
     * @param name 分组名称
     * @param excludeId 排除的项目分组编号
     */
    private void validateProjectGroupNameDuplicate(Long userId, String name, Long excludeId) {
        PmsProjectGroupDO group = projectGroupMapper.selectByUserIdAndName(userId, name, excludeId);
        if (group != null) {
            throw exception(PROJECT_GROUP_NAME_DUPLICATE);
        }
    }

}
