package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectGroupDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectGroupRelationDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectGroupMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectGroupRelationMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectGroupTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_DEFAULT_CANNOT_MODIFY;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_GROUP_NOT_EXISTS;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsProjectGroupServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsProjectGroupServiceImpl.class)
public class PmsProjectGroupServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsProjectGroupServiceImpl projectGroupService;

    @Resource
    private PmsProjectGroupMapper projectGroupMapper;
    @Resource
    private PmsProjectGroupRelationMapper projectGroupRelationMapper;

    @MockitoBean
    private PmsProjectMemberService projectMemberService;

    @Test
    public void testCreateProjectGroup_success() {
        // 准备参数
        Long userId = randomLongId();
        PmsProjectGroupSaveReqVO reqVO = new PmsProjectGroupSaveReqVO().setName("重点项目");

        // 调用
        Long groupId = projectGroupService.createProjectGroup(reqVO, userId);

        // 断言
        PmsProjectGroupDO group = projectGroupMapper.selectById(groupId);
        assertNotNull(group);
        assertEquals(userId, group.getUserId());
        assertEquals("重点项目", group.getName());
        assertEquals(2, group.getSort());
        assertEquals(PmsProjectGroupTypeEnum.CUSTOM.getType(), group.getType());
        assertEquals(1, projectGroupMapper.selectListByUserId(userId).size());
    }

    @Test
    public void testCreateProjectGroup_nameDuplicate() {
        // mock 数据
        Long userId = randomLongId();
        projectGroupMapper.insert(randomProjectGroupDO(userId, "重点项目", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType()));
        // 准备参数
        PmsProjectGroupSaveReqVO reqVO = new PmsProjectGroupSaveReqVO().setName("重点项目");

        // 调用，并断言异常
        assertServiceException(() -> projectGroupService.createProjectGroup(reqVO, userId),
                PROJECT_GROUP_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateProjectGroup_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO group = randomProjectGroupDO(userId, "旧名称", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(group);
        // 准备参数
        PmsProjectGroupSaveReqVO reqVO = new PmsProjectGroupSaveReqVO()
                .setId(group.getId()).setName("新名称");

        // 调用
        projectGroupService.updateProjectGroup(reqVO, userId);

        // 断言
        assertEquals("新名称", projectGroupMapper.selectById(group.getId()).getName());
    }

    @Test
    public void testUpdateProjectGroup_defaultCannotModify() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO group = randomProjectGroupDO(userId, "全部项目", 0,
                PmsProjectGroupTypeEnum.ALL.getType());
        projectGroupMapper.insert(group);
        // 准备参数
        PmsProjectGroupSaveReqVO reqVO = new PmsProjectGroupSaveReqVO()
                .setId(group.getId()).setName("新名称");

        // 调用，并断言异常
        assertServiceException(() -> projectGroupService.updateProjectGroup(reqVO, userId),
                PROJECT_GROUP_DEFAULT_CANNOT_MODIFY);
    }

    @Test
    public void testUpdateProjectGroupSort_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO firstGroup = randomProjectGroupDO(userId, "分组一", 0,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(firstGroup);
        PmsProjectGroupDO secondGroup = randomProjectGroupDO(userId, "分组二", 1,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(secondGroup);
        // 准备参数
        PmsProjectGroupSortReqVO reqVO = new PmsProjectGroupSortReqVO().setItems(asList(
                new PmsProjectGroupSortReqVO.Item().setId(firstGroup.getId()).setSort(2),
                new PmsProjectGroupSortReqVO.Item().setId(secondGroup.getId()).setSort(1)));

        // 调用
        projectGroupService.updateProjectGroupSort(reqVO, userId);

        // 断言
        assertEquals(2, projectGroupMapper.selectById(firstGroup.getId()).getSort());
        assertEquals(1, projectGroupMapper.selectById(secondGroup.getId()).getSort());
    }

    @Test
    public void testUpdateProjectGroupSort_notOwner() {
        // mock 数据
        PmsProjectGroupDO group = randomProjectGroupDO(randomLongId(), "其他用户分组", 0,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(group);
        // 准备参数
        PmsProjectGroupSortReqVO reqVO = new PmsProjectGroupSortReqVO().setItems(asList(
                new PmsProjectGroupSortReqVO.Item().setId(group.getId()).setSort(1)));

        // 调用，并断言异常
        assertServiceException(() -> projectGroupService.updateProjectGroupSort(reqVO, randomLongId()),
                PROJECT_GROUP_NOT_EXISTS);
    }

    @Test
    public void testDeleteProjectGroup_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO group = randomProjectGroupDO(userId, "测试分组", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(group);
        PmsProjectGroupRelationDO relation = randomProjectGroupRelationDO(userId, group.getId(), randomLongId());
        projectGroupRelationMapper.insert(relation);

        // 调用
        projectGroupService.deleteProjectGroup(group.getId(), userId);

        // 断言
        assertNull(projectGroupMapper.selectById(group.getId()));
        assertNull(projectGroupRelationMapper.selectById(relation.getId()));
    }

    @Test
    public void testDeleteProjectGroup_defaultCannotDelete() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO group = randomProjectGroupDO(userId, "未分组", 1,
                PmsProjectGroupTypeEnum.UNGROUPED.getType());
        projectGroupMapper.insert(group);

        // 调用，并断言异常
        assertServiceException(() -> projectGroupService.deleteProjectGroup(group.getId(), userId),
                PROJECT_GROUP_DEFAULT_CANNOT_DELETE);
    }

    @Test
    public void testGetProjectGroupList_noRelations() {
        // mock 数据
        Long userId = randomLongId();

        // 调用
        List<PmsProjectGroupDO> groups = projectGroupService.getProjectGroupList(userId);
        Map<Long, Integer> groupCountMap = projectGroupService.getProjectGroupCountMap(
                userId, groups, asList(101L, 102L));

        // 断言
        PmsProjectGroupDO allGroup = CollUtil.getFirst(groups);
        PmsProjectGroupDO ungroupedGroup = groups.get(1);
        assertEquals(2, groupCountMap.get(allGroup.getId()));
        assertEquals(2, groupCountMap.get(ungroupedGroup.getId()));
    }

    @Test
    public void testGetProjectGroupList_customGroupCounts() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO customGroup = randomProjectGroupDO(userId, "重点项目", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(customGroup);
        projectGroupRelationMapper.insert(randomProjectGroupRelationDO(userId, customGroup.getId(), 101L));

        // 调用
        List<PmsProjectGroupDO> groups = projectGroupService.getProjectGroupList(userId);
        Map<Long, Integer> groupCountMap = projectGroupService.getProjectGroupCountMap(
                userId, groups, asList(101L, 102L, 103L));

        // 断言
        assertEquals(3, groupCountMap.get(CollUtil.getFirst(groups).getId()));
        assertEquals(2, groupCountMap.get(groups.get(1).getId()));
        assertEquals(1, groupCountMap.get(customGroup.getId()));
    }

    @Test
    public void testGetProjectGroupList_sameProjectInMultipleGroups() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO firstGroup = randomProjectGroupDO(userId, "重点项目", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(firstGroup);
        PmsProjectGroupDO secondGroup = randomProjectGroupDO(userId, "产品项目", 3,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(secondGroup);
        projectGroupRelationMapper.insert(randomProjectGroupRelationDO(userId, firstGroup.getId(), 101L));
        projectGroupRelationMapper.insert(randomProjectGroupRelationDO(userId, secondGroup.getId(), 101L));

        // 调用
        List<PmsProjectGroupDO> groups = projectGroupService.getProjectGroupList(userId);
        Map<Long, Integer> groupCountMap = projectGroupService.getProjectGroupCountMap(
                userId, groups, asList(101L, 102L));

        // 断言
        assertEquals(2, groupCountMap.get(CollUtil.getFirst(groups).getId()));
        assertEquals(1, groupCountMap.get(groups.get(1).getId()));
        assertEquals(1, groupCountMap.get(firstGroup.getId()));
        assertEquals(1, groupCountMap.get(secondGroup.getId()));
    }

    @Test
    public void testMoveProjectToGroup_createAndUpdate() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsProjectGroupDO firstGroup = randomProjectGroupDO(userId, "分组一", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(firstGroup);
        PmsProjectGroupDO secondGroup = randomProjectGroupDO(userId, "分组二", 3,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(secondGroup);

        // 调用
        projectGroupService.moveProjectToGroup(new PmsProjectGroupMoveReqVO()
                .setProjectId(projectId).setGroupId(firstGroup.getId()), userId);
        projectGroupService.moveProjectToGroup(new PmsProjectGroupMoveReqVO()
                .setProjectId(projectId).setGroupId(secondGroup.getId()), userId);

        // 断言
        PmsProjectGroupRelationDO relation =
                projectGroupRelationMapper.selectByUserIdAndProjectId(userId, projectId);
        assertNotNull(relation);
        assertEquals(secondGroup.getId(), relation.getGroupId());
        verify(projectMemberService, times(2)).validateProjectMember(projectId, userId);
    }

    @Test
    public void testMoveProjectToGroup_allCannotMove() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO group = randomProjectGroupDO(userId, "全部项目", 0,
                PmsProjectGroupTypeEnum.ALL.getType());
        projectGroupMapper.insert(group);
        // 准备参数
        PmsProjectGroupMoveReqVO reqVO = new PmsProjectGroupMoveReqVO()
                .setProjectId(randomLongId()).setGroupId(group.getId());

        // 调用，并断言异常
        assertServiceException(() -> projectGroupService.moveProjectToGroup(reqVO, userId),
                PROJECT_GROUP_DEFAULT_CANNOT_MODIFY);
    }

    @Test
    public void testFilterProjectIdListByGroup_ungrouped() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO ungrouped = randomProjectGroupDO(userId, "未分组", 1,
                PmsProjectGroupTypeEnum.UNGROUPED.getType());
        projectGroupMapper.insert(ungrouped);
        PmsProjectGroupDO customGroup = randomProjectGroupDO(userId, "重点项目", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(customGroup);
        projectGroupRelationMapper.insert(randomProjectGroupRelationDO(userId, customGroup.getId(), 101L));

        // 调用
        List<Long> projectIds = projectGroupService.filterProjectIdListByGroupId(
                ungrouped.getId(), userId, asList(101L, 102L));

        // 断言
        assertEquals(asList(102L), projectIds);
    }

    @Test
    public void testFilterProjectIdListByGroup_customGroup() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectGroupDO customGroup = randomProjectGroupDO(userId, "重点项目", 2,
                PmsProjectGroupTypeEnum.CUSTOM.getType());
        projectGroupMapper.insert(customGroup);
        projectGroupRelationMapper.insert(randomProjectGroupRelationDO(userId, customGroup.getId(), 101L));

        // 调用
        List<Long> projectIds = projectGroupService.filterProjectIdListByGroupId(
                customGroup.getId(), userId, asList(101L, 102L));

        // 断言
        assertEquals(asList(101L), projectIds);
    }

    // ========== 随机对象 ==========

    private static PmsProjectGroupDO randomProjectGroupDO(Long userId, String name, Integer sort, Integer type) {
        return randomPojo(PmsProjectGroupDO.class, group -> group.setUserId(userId)
                .setName(name).setSort(sort).setType(type));
    }

    private static PmsProjectGroupRelationDO randomProjectGroupRelationDO(
            Long userId, Long groupId, Long projectId) {
        return randomPojo(PmsProjectGroupRelationDO.class, relation -> relation.setUserId(userId)
                .setGroupId(groupId).setProjectId(projectId).setSort(0));
    }

}
