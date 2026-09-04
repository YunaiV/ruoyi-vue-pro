package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupSortReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeLibraryMoveGroupReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupRelationDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeGroupMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeGroupRelationMapper;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeGroupTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_GROUP_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_GROUP_DEFAULT_CANNOT_MODIFY;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_GROUP_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_GROUP_NOT_EXISTS;

/**
 * PMS 知识库分组 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeGroupServiceImpl implements PmsKnowledgeGroupService {

    @Resource
    private PmsKnowledgeGroupMapper groupMapper;
    @Resource
    private PmsKnowledgeGroupRelationMapper groupRelationMapper;

    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;

    @Override
    public Long createGroup(PmsKnowledgeGroupSaveReqVO saveReqVO, Long userId) {
        // 1. 校验分组名称唯一
        String name = StrUtil.trim(saveReqVO.getName());
        validateKnowledgeGroupNameDuplicate(userId, name, null);

        // 2. 创建自定义分组，并追加到现有分组末尾
        PmsKnowledgeGroupDO lastGroup = groupMapper.selectLastByUserId(userId);
        int sort = lastGroup == null || lastGroup.getSort() == null
                ? 2 : Math.max(lastGroup.getSort() + 1, 2);
        PmsKnowledgeGroupDO group = BeanUtils.toBean(saveReqVO, PmsKnowledgeGroupDO.class)
                .setUserId(userId).setName(name)
                .setSort(sort)
                .setType(PmsKnowledgeGroupTypeEnum.CUSTOM.getType());
        groupMapper.insert(group);
        return group.getId();
    }

    @Override
    public void updateGroup(PmsKnowledgeGroupSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验分组存在
        PmsKnowledgeGroupDO group = validateKnowledgeGroupExists(saveReqVO.getId(), userId);
        // 1.2 校验分组为自定义分组
        if (notEqual(PmsKnowledgeGroupTypeEnum.CUSTOM.getType(), group.getType())) {
            throw exception(KNOWLEDGE_GROUP_DEFAULT_CANNOT_MODIFY);
        }
        // 1.3 校验分组名称唯一
        validateKnowledgeGroupNameDuplicate(userId, StrUtil.trim(saveReqVO.getName()), saveReqVO.getId());

        // 2. 更新分组名称
        groupMapper.updateById(BeanUtils.toBean(saveReqVO, PmsKnowledgeGroupDO.class)
                .setName(StrUtil.trim(saveReqVO.getName())).setUserId(null).setSort(null).setType(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupSort(PmsKnowledgeGroupSortReqVO sortReqVO, Long userId) {
        // 1. 校验排序列表中的分组都属于当前用户
        List<Long> groupIds = convertList(sortReqVO.getItems(), PmsKnowledgeGroupSortReqVO.Item::getId);
        if (groupMapper.selectListByIdsAndUserId(groupIds, userId).size() != groupIds.size()) {
            throw exception(KNOWLEDGE_GROUP_NOT_EXISTS);
        }

        // 2. 更新全部分组的显示顺序
        groupMapper.updateBatch(convertList(sortReqVO.getItems(),
                item -> new PmsKnowledgeGroupDO().setId(item.getId()).setSort(item.getSort())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long id, Long userId) {
        // 1.1 校验分组存在
        PmsKnowledgeGroupDO group = validateKnowledgeGroupExists(id, userId);
        // 1.2 校验分组为自定义分组
        if (notEqual(PmsKnowledgeGroupTypeEnum.CUSTOM.getType(), group.getType())) {
            throw exception(KNOWLEDGE_GROUP_DEFAULT_CANNOT_DELETE);
        }

        // 2. 删除分组及其知识库关系
        groupRelationMapper.deleteByUserIdAndGroupId(userId, id);
        groupMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PmsKnowledgeGroupDO> getGroupList(Long userId) {
        // 1. 查询当前用户的知识库分组
        List<PmsKnowledgeGroupDO> groups = groupMapper.selectListByUserId(userId);
        // 2. 仅在默认分组缺失时补齐，避免正常访问产生写操作
        ensureDefaultKnowledgeGroupList(userId, groups);
        return groups;
    }

    @Override
    public Map<Long, Integer> getGroupLibraryCountMap(Long userId, Collection<Long> groupIds) {
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        // 1. 只统计当前用户可读的知识库，公开库也应进入“全部知识库”数量
        List<Long> readableLibraryIds = libraryMemberService.getReadableLibraryIdList(userId);
        Map<Long, Integer> groupCountMap = CollUtil.isEmpty(readableLibraryIds)
                ? new LinkedHashMap<>()
                : groupRelationMapper.selectGroupLibraryCountMapByUserIdAndLibraryIds(userId, readableLibraryIds);
        int groupedLibraryCount = CollUtil.isEmpty(readableLibraryIds) ? 0
                : groupRelationMapper.selectGroupedLibraryCountByUserIdAndLibraryIds(userId, readableLibraryIds);

        // 2. 补充系统分组数量：全部=可读总数，未分组=可读总数-已分组数
        int readableLibraryCount = CollUtil.size(readableLibraryIds);
        List<PmsKnowledgeGroupDO> groups = groupMapper.selectByIds(groupIds);
        for (PmsKnowledgeGroupDO group : groups) {
            if (PmsKnowledgeGroupTypeEnum.ALL.getType().equals(group.getType())) {
                groupCountMap.put(group.getId(), readableLibraryCount);
            } else if (PmsKnowledgeGroupTypeEnum.UNGROUPED.getType().equals(group.getType())) {
                groupCountMap.put(group.getId(), Math.max(readableLibraryCount - groupedLibraryCount, 0));
            }
        }
        return groupCountMap;
    }

    @Override
    public PmsKnowledgeGroupDO getGroup(Long id, Long userId) {
        return validateKnowledgeGroupExists(id, userId);
    }

    /**
     * 确保用户拥有“全部知识库”和“未分组”两个系统分组
     *
     * @param userId 用户编号
     * @param groups 当前已查询到的分组
     */
    private void ensureDefaultKnowledgeGroupList(Long userId, List<PmsKnowledgeGroupDO> groups) {
        // 1. 检查两个系统分组是否已经存在
        boolean hasAllGroup = CollUtil.findOne(groups,
                group -> PmsKnowledgeGroupTypeEnum.ALL.getType().equals(group.getType())) != null;
        boolean hasUngroupedGroup = CollUtil.findOne(groups,
                group -> PmsKnowledgeGroupTypeEnum.UNGROUPED.getType().equals(group.getType())) != null;
        if (hasAllGroup && hasUngroupedGroup) {
            return;
        }

        // 2. 构造并插入缺失的系统分组
        List<PmsKnowledgeGroupDO> defaultGroups = new ArrayList<>();
        if (!hasAllGroup) {
            defaultGroups.add(new PmsKnowledgeGroupDO().setUserId(userId)
                    .setName(PmsKnowledgeGroupTypeEnum.ALL.getName()).setSort(0)
                    .setType(PmsKnowledgeGroupTypeEnum.ALL.getType()));
        }
        if (!hasUngroupedGroup) {
            defaultGroups.add(new PmsKnowledgeGroupDO().setUserId(userId)
                    .setName(PmsKnowledgeGroupTypeEnum.UNGROUPED.getName()).setSort(1)
                    .setType(PmsKnowledgeGroupTypeEnum.UNGROUPED.getType()));
        }
        groupMapper.insertBatch(defaultGroups);

        // 3. 直接合并本次新增记录并保持排序
        groups.addAll(defaultGroups);
        groups.sort(Comparator.comparing(PmsKnowledgeGroupDO::getSort,
                Comparator.nullsLast(Integer::compareTo)).thenComparing(PmsKnowledgeGroupDO::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveLibraryToGroup(PmsKnowledgeLibraryMoveGroupReqVO moveReqVO, Long userId) {
        // 1. 校验当前用户可以读取知识库；个人分组允许收藏公开知识库
        libraryMemberService.validateLibraryReadable(moveReqVO.getLibraryId(), userId);

        // 2. 移出分组时，删除已有的个人分组关系
        if (moveReqVO.getGroupId() == null) {
            groupRelationMapper.deleteByUserIdAndLibraryId(userId, moveReqVO.getLibraryId());
            return;
        }

        // 3. 校验目标分组；移动到“未分组”等同于移出分组，“全部知识库”不能作为目标分组
        PmsKnowledgeGroupDO targetGroup = validateKnowledgeGroupExists(moveReqVO.getGroupId(), userId);
        if (PmsKnowledgeGroupTypeEnum.UNGROUPED.getType().equals(targetGroup.getType())) {
            groupRelationMapper.deleteByUserIdAndLibraryId(userId, moveReqVO.getLibraryId());
            return;
        }
        if (PmsKnowledgeGroupTypeEnum.ALL.getType().equals(targetGroup.getType())) {
            throw exception(KNOWLEDGE_GROUP_DEFAULT_CANNOT_MODIFY);
        }

        // 4. 新增或更新个人知识库分组关系
        PmsKnowledgeGroupRelationDO relation = groupRelationMapper.selectByUserIdAndLibraryId(
                userId, moveReqVO.getLibraryId());
        if (relation == null) {
            groupRelationMapper.insert(BeanUtils.toBean(moveReqVO, PmsKnowledgeGroupRelationDO.class)
                    .setUserId(userId).setSort(0));
            return;
        }
        groupRelationMapper.updateById(BeanUtils.toBean(moveReqVO, PmsKnowledgeGroupRelationDO.class)
                .setId(relation.getId()).setUserId(null).setLibraryId(null).setSort(null));
    }

    @Override
    public List<Long> filterLibraryIdListByGroup(Long groupId, Long userId, Collection<Long> libraryIds) {
        if (CollUtil.isEmpty(libraryIds) || groupId == null) {
            return CollUtil.isEmpty(libraryIds) ? Collections.emptyList() : new ArrayList<>(new HashSet<>(libraryIds));
        }

        // 1. 校验分组存在；“全部知识库”不额外过滤
        PmsKnowledgeGroupDO group = validateKnowledgeGroupExists(groupId, userId);
        if (PmsKnowledgeGroupTypeEnum.ALL.getType().equals(group.getType())) {
            return new ArrayList<>(new HashSet<>(libraryIds));
        }

        // 2. 获得个人分组中的知识库编号
        Set<Long> candidateLibraryIds = new HashSet<>(libraryIds);
        if (PmsKnowledgeGroupTypeEnum.UNGROUPED.getType().equals(group.getType())) {
            Set<Long> groupedLibraryIds = convertSet(groupRelationMapper.selectListByUserIdAndLibraryIds(
                    userId, candidateLibraryIds), PmsKnowledgeGroupRelationDO::getLibraryId);
            candidateLibraryIds.removeAll(groupedLibraryIds);
            return new ArrayList<>(candidateLibraryIds);
        }
        // 自定义分组只保留已建立当前分组关系的候选知识库
        List<PmsKnowledgeGroupRelationDO> relations =
                groupRelationMapper.selectListByUserIdAndGroupId(userId, groupId);
        Set<Long> groupedLibraryIds = convertSet(relations, PmsKnowledgeGroupRelationDO::getLibraryId);
        candidateLibraryIds.retainAll(groupedLibraryIds);
        return new ArrayList<>(candidateLibraryIds);
    }

    @Override
    public void deleteKnowledgeGroupRelationsByLibraryId(Long libraryId) {
        groupRelationMapper.deleteByLibraryId(libraryId);
    }

    private PmsKnowledgeGroupDO validateKnowledgeGroupExists(Long id, Long userId) {
        PmsKnowledgeGroupDO group = groupMapper.selectByIdAndUserId(id, userId);
        if (group == null) {
            throw exception(KNOWLEDGE_GROUP_NOT_EXISTS);
        }
        return group;
    }

    private void validateKnowledgeGroupNameDuplicate(Long userId, String name, Long excludeId) {
        PmsKnowledgeGroupDO group = groupMapper.selectByUserIdAndName(userId, name, excludeId);
        if (group != null) {
            throw exception(KNOWLEDGE_GROUP_NAME_DUPLICATE);
        }
    }

}
