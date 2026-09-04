package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeGroupSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group.PmsKnowledgeLibraryMoveGroupReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupRelationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeGroupMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeGroupRelationMapper;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeGroupTypeEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_GROUP_DEFAULT_CANNOT_DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeGroupServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeGroupServiceImpl.class)
public class PmsKnowledgeGroupServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeGroupServiceImpl groupService;

    @Resource
    private PmsKnowledgeGroupMapper groupMapper;
    @Resource
    private PmsKnowledgeGroupRelationMapper relationMapper;

    @MockBean
    private PmsKnowledgeLibraryMemberService libraryMemberService;

    @Test
    public void testCreateKnowledgeGroup_success() {
        // 准备参数
        Long userId = randomLongId();
        PmsKnowledgeGroupSaveReqVO reqVO = new PmsKnowledgeGroupSaveReqVO().setName("  产品资料  ");

        // 调用
        Long groupId = groupService.createGroup(reqVO, userId);

        // 断言
        PmsKnowledgeGroupDO group = groupMapper.selectById(groupId);
        assertEquals("产品资料", group.getName());
        assertEquals(PmsKnowledgeGroupTypeEnum.CUSTOM.getType(), group.getType());
        assertEquals(1, groupMapper.selectListByUserId(userId).size());
    }

    @Test
    public void testGetKnowledgeGroupList_defaultGroups() {
        // mock 数据
        Long userId = randomLongId();
        groupMapper.insert(randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.CUSTOM.getType()));

        // 调用
        List<PmsKnowledgeGroupDO> groups = groupService.getGroupList(userId);

        // 断言
        assertEquals(1, groupMapper.selectListByUserId(userId).stream()
                .filter(item -> PmsKnowledgeGroupTypeEnum.ALL.getType().equals(item.getType())).count());
        assertEquals(1, groupMapper.selectListByUserId(userId).stream()
                .filter(item -> PmsKnowledgeGroupTypeEnum.UNGROUPED.getType().equals(item.getType())).count());
        assertEquals(1, groups.stream()
                .filter(item -> PmsKnowledgeGroupTypeEnum.ALL.getType().equals(item.getType())).count());
        assertEquals(1, groups.stream()
                .filter(item -> PmsKnowledgeGroupTypeEnum.UNGROUPED.getType().equals(item.getType())).count());
        assertEquals(3, groups.size());
    }

    @Test
    public void testMoveKnowledgeGroup_success() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        PmsKnowledgeGroupDO customGroup = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.CUSTOM.getType());
        groupMapper.insert(customGroup);
        when(libraryMemberService.validateLibraryReadable(libraryId, userId))
                .thenReturn(new PmsKnowledgeLibraryDO().setId(libraryId));
        // 准备参数
        PmsKnowledgeLibraryMoveGroupReqVO reqVO = new PmsKnowledgeLibraryMoveGroupReqVO()
                .setLibraryId(libraryId).setGroupId(customGroup.getId());

        // 调用
        groupService.moveLibraryToGroup(reqVO, userId);

        // 断言
        PmsKnowledgeGroupRelationDO relation = relationMapper.selectByUserIdAndLibraryId(userId, libraryId);
        assertNotNull(relation);
        assertEquals(customGroup.getId(), relation.getGroupId());
    }

    @Test
    public void testGetGroupLibraryCountMap_success() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId1 = randomLongId();
        Long libraryId2 = randomLongId();
        Long libraryId3 = randomLongId();
        PmsKnowledgeGroupDO allGroup = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.ALL.getType());
        PmsKnowledgeGroupDO ungroupedGroup = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.UNGROUPED.getType());
        PmsKnowledgeGroupDO customGroup = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.CUSTOM.getType());
        groupMapper.insert(allGroup);
        groupMapper.insert(ungroupedGroup);
        groupMapper.insert(customGroup);
        relationMapper.insert(new PmsKnowledgeGroupRelationDO().setUserId(userId).setGroupId(customGroup.getId())
                .setLibraryId(libraryId1).setSort(0));
        relationMapper.insert(new PmsKnowledgeGroupRelationDO().setUserId(userId).setGroupId(customGroup.getId())
                .setLibraryId(libraryId1).setSort(1));
        when(libraryMemberService.getReadableLibraryIdList(userId))
                .thenReturn(Arrays.asList(libraryId1, libraryId2, libraryId3));

        // 调用
        Map<Long, Integer> countMap = groupService.getGroupLibraryCountMap(userId,
                Arrays.asList(allGroup.getId(), ungroupedGroup.getId(), customGroup.getId()));

        // 断言：重复关系按知识库去重，未分组数量不被重复关系影响
        assertEquals(3, countMap.get(allGroup.getId()));
        assertEquals(2, countMap.get(ungroupedGroup.getId()));
        assertEquals(1, countMap.get(customGroup.getId()));
    }

    @Test
    public void testMoveKnowledgeGroup_ungrouped() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        PmsKnowledgeGroupDO customGroup = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.CUSTOM.getType());
        groupMapper.insert(customGroup);
        PmsKnowledgeGroupDO ungrouped = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.UNGROUPED.getType());
        groupMapper.insert(ungrouped);
        relationMapper.insert(new PmsKnowledgeGroupRelationDO().setUserId(userId).setGroupId(customGroup.getId())
                .setLibraryId(libraryId).setSort(0));
        when(libraryMemberService.validateLibraryReadable(libraryId, userId))
                .thenReturn(new PmsKnowledgeLibraryDO().setId(libraryId));
        // 准备参数
        PmsKnowledgeLibraryMoveGroupReqVO reqVO = new PmsKnowledgeLibraryMoveGroupReqVO()
                .setLibraryId(libraryId).setGroupId(ungrouped.getId());

        // 调用
        groupService.moveLibraryToGroup(reqVO, userId);

        // 断言
        assertNull(relationMapper.selectByUserIdAndLibraryId(userId, libraryId));
    }

    @Test
    public void testFilterLibraryIdListByGroup_ungrouped() {
        // mock 数据
        Long userId = randomLongId();
        Long groupedLibraryId = randomLongId();
        Long ungroupedLibraryId = randomLongId();
        PmsKnowledgeGroupDO customGroup = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.CUSTOM.getType());
        groupMapper.insert(customGroup);
        PmsKnowledgeGroupDO ungrouped = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.UNGROUPED.getType());
        groupMapper.insert(ungrouped);
        relationMapper.insert(new PmsKnowledgeGroupRelationDO().setUserId(userId).setGroupId(customGroup.getId())
                .setLibraryId(groupedLibraryId).setSort(0));

        // 调用
        List<Long> filteredLibraryIds = groupService.filterLibraryIdListByGroup(ungrouped.getId(), userId,
                Arrays.asList(groupedLibraryId, ungroupedLibraryId));

        // 断言
        assertEquals(Collections.singletonList(ungroupedLibraryId), filteredLibraryIds);
    }

    @Test
    public void testDeleteKnowledgeGroup_defaultGroup() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeGroupDO allGroup = randomGroupDO(userId, PmsKnowledgeGroupTypeEnum.ALL.getType());
        groupMapper.insert(allGroup);

        // 调用，并断言异常
        assertServiceException(() -> groupService.deleteGroup(allGroup.getId(), userId),
                KNOWLEDGE_GROUP_DEFAULT_CANNOT_DELETE);
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeGroupDO randomGroupDO(Long userId, Integer type) {
        return randomPojo(PmsKnowledgeGroupDO.class, group -> group.setId(null).setUserId(userId)
                .setName(PmsKnowledgeGroupTypeEnum.valueOf(type).getName()).setSort(type).setType(type));
    }

}
