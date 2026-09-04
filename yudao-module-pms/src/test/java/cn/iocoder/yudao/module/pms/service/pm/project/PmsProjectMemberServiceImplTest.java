package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
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
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_MEMBER_CREATOR_CANNOT_REMOVE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_MEMBER_OWNER_CANNOT_EXIT;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_MEMBER_OWNER_LEVEL_CANNOT_ASSIGN;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_WRITE_ACCESS_DENIED;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * {@link PmsProjectMemberServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsProjectMemberServiceImpl.class)
public class PmsProjectMemberServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsProjectMemberServiceImpl projectMemberService;

    @Resource
    private PmsProjectMemberMapper projectMemberMapper;

    @MockBean
    private PmsProjectService projectService;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private PermissionApi permissionApi;

    @Test
    public void testCreateProjectMemberList_success() {
        // 准备参数
        Long projectId = randomLongId();
        Long creatorId = randomLongId();
        Long memberId = randomLongId();

        // 调用
        projectMemberService.createProjectMemberList(projectId, creatorId, asList(creatorId, memberId));

        // 断言
        List<PmsProjectMemberDO> members = projectMemberMapper.selectListByProjectId(projectId);
        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(member -> creatorId.equals(member.getUserId())
                && PmsProjectMemberLevelEnum.OWNER.getLevel().equals(member.getLevel())));
        assertTrue(members.stream().anyMatch(member -> memberId.equals(member.getUserId())
                && PmsProjectMemberLevelEnum.WRITE.getLevel().equals(member.getLevel())));
    }

    @Test
    public void testSaveProjectMembers_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long creatorId = randomLongId();
        Long operatorUserId = randomLongId();
        Long memberUserId = randomLongId();
        PmsProjectDO project = randomProjectDO(projectId, creatorId);
        when(projectService.getProject(projectId)).thenReturn(project);
        projectMemberMapper.insert(randomProjectMemberDO(projectId, operatorUserId,
                PmsProjectMemberLevelEnum.ADMIN));
        projectMemberMapper.insert(randomProjectMemberDO(projectId, memberUserId,
                PmsProjectMemberLevelEnum.READ));
        // 准备参数
        PmsProjectMemberSaveReqVO reqVO = new PmsProjectMemberSaveReqVO().setProjectId(projectId)
                .setMembers(singletonList(new PmsProjectMemberSaveReqVO.Member().setUserId(memberUserId)
                        .setLevel(PmsProjectMemberLevelEnum.WRITE.getLevel())));

        // 调用
        projectMemberService.updateProjectMemberList(reqVO, operatorUserId);

        // 断言
        PmsProjectMemberDO member = projectMemberMapper.selectByProjectIdAndUserId(projectId, memberUserId);
        assertEquals(PmsProjectMemberLevelEnum.WRITE.getLevel(), member.getLevel());
    }

    @Test
    public void testSaveProjectMembers_ownerLevelCannotAssign() {
        // mock 数据
        Long projectId = randomLongId();
        Long creatorId = randomLongId();
        Long operatorUserId = randomLongId();
        when(projectService.getProject(projectId)).thenReturn(randomProjectDO(projectId, creatorId));
        projectMemberMapper.insert(randomProjectMemberDO(projectId, operatorUserId,
                PmsProjectMemberLevelEnum.ADMIN));
        // 准备参数
        PmsProjectMemberSaveReqVO reqVO = new PmsProjectMemberSaveReqVO().setProjectId(projectId)
                .setMembers(singletonList(new PmsProjectMemberSaveReqVO.Member().setUserId(randomLongId())
                        .setLevel(PmsProjectMemberLevelEnum.OWNER.getLevel())));

        // 调用，并断言异常
        assertServiceException(() -> projectMemberService.updateProjectMemberList(reqVO, operatorUserId),
                PROJECT_MEMBER_OWNER_LEVEL_CANNOT_ASSIGN);
    }

    @Test
    public void testDeleteProjectMember_ownerCannotRemove() {
        // mock 数据
        Long projectId = randomLongId();
        Long creatorId = randomLongId();
        PmsProjectDO project = randomProjectDO(projectId, creatorId);
        when(projectService.getProject(projectId)).thenReturn(project);
        projectMemberMapper.insert(randomProjectMemberDO(projectId, creatorId,
                PmsProjectMemberLevelEnum.OWNER));

        // 调用，并断言异常
        assertServiceException(() -> projectMemberService.deleteProjectMember(projectId, creatorId, creatorId),
                PROJECT_MEMBER_CREATOR_CANNOT_REMOVE);
    }

    @Test
    public void testExitProject_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long creatorId = randomLongId();
        Long memberId = randomLongId();
        when(projectService.getProject(projectId)).thenReturn(randomProjectDO(projectId, creatorId));
        projectMemberMapper.insert(randomProjectMemberDO(projectId, memberId,
                PmsProjectMemberLevelEnum.WRITE));

        // 调用
        projectMemberService.exitProject(projectId, memberId);

        // 断言
        assertNull(projectMemberMapper.selectByProjectIdAndUserId(projectId, memberId));
    }

    @Test
    public void testExitProject_ownerCannotExit() {
        // mock 数据
        Long projectId = randomLongId();
        Long ownerId = randomLongId();
        when(projectService.getProject(projectId)).thenReturn(randomProjectDO(projectId, ownerId));
        projectMemberMapper.insert(randomProjectMemberDO(projectId, ownerId,
                PmsProjectMemberLevelEnum.OWNER));

        // 调用，并断言异常
        assertServiceException(() -> projectMemberService.exitProject(projectId, ownerId),
                PROJECT_MEMBER_OWNER_CANNOT_EXIT);
    }

    @Test
    public void testGetProjectMemberList_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long creatorId = randomLongId();
        Long memberId = randomLongId();
        PmsProjectDO project = randomProjectDO(projectId, creatorId);
        when(projectService.getProject(projectId)).thenReturn(project);
        projectMemberMapper.insert(randomProjectMemberDO(projectId, creatorId,
                PmsProjectMemberLevelEnum.OWNER));
        projectMemberMapper.insert(randomProjectMemberDO(projectId, memberId,
                PmsProjectMemberLevelEnum.WRITE));

        // 调用
        List<PmsProjectMemberRespVO> members = projectMemberService.getProjectMemberList(projectId, creatorId);

        // 断言
        assertEquals(2, members.size());
        PmsProjectMemberRespVO member = CollUtil.findOne(members, item -> memberId.equals(item.getUserId()));
        assertEquals(PmsProjectMemberLevelEnum.WRITE.getLevel(), member.getLevel());
    }

    @Test
    public void testGetActiveProjectIdList() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO activeProject = randomProjectDO("进行中项目", PmsProjectStatusEnum.ACTIVE.getStatus());
        PmsProjectDO archivedProject = randomProjectDO("已归档项目", PmsProjectStatusEnum.ARCHIVED.getStatus());
        projectMemberMapper.insert(randomProjectMemberDO(activeProject.getId(), userId));
        projectMemberMapper.insert(randomProjectMemberDO(activeProject.getId(), userId));
        projectMemberMapper.insert(randomProjectMemberDO(archivedProject.getId(), userId));
        when(projectService.getProjectList(anyCollection())).thenReturn(asList(activeProject, archivedProject));

        // 调用
        List<Long> projectIds = projectMemberService.getActiveProjectIdListByUserId(userId);

        // 断言
        assertEquals(singletonList(activeProject.getId()), projectIds);
    }

    @Test
    public void testGetActiveProjectIdList_empty() {
        // 调用
        List<Long> projectIds = projectMemberService.getActiveProjectIdListByUserId(randomLongId());

        // 断言
        assertTrue(CollUtil.isEmpty(projectIds));
    }

    @Test
    public void testValidateProjectMember_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO("测试项目", PmsProjectStatusEnum.ACTIVE.getStatus());
        when(projectService.getProject(project.getId())).thenReturn(project);
        projectMemberMapper.insert(randomProjectMemberDO(project.getId(), userId));

        // 调用
        PmsProjectDO memberProject = projectMemberService.validateProjectMember(project.getId(), userId);

        // 断言
        assertEquals(project.getId(), memberProject.getId());
    }

    @Test
    public void testValidateProjectMember_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> projectMemberService.validateProjectMember(randomLongId(), randomLongId()),
                PROJECT_NOT_EXISTS);
    }

    @Test
    public void testValidateProjectMember_accessDenied() {
        // mock 数据
        PmsProjectDO project = randomProjectDO("测试项目", PmsProjectStatusEnum.ACTIVE.getStatus());
        when(projectService.getProject(project.getId())).thenReturn(project);

        // 调用，并断言异常
        assertServiceException(() -> projectMemberService.validateProjectMember(project.getId(), randomLongId()),
                PROJECT_ACCESS_DENIED);
    }

    @Test
    public void testValidateProjectReadable_publicProject() {
        // mock 数据
        PmsProjectDO project = randomProjectDO("公开项目", PmsProjectStatusEnum.ACTIVE.getStatus())
                .setOpenStatus(true);
        when(projectService.getProject(project.getId())).thenReturn(project);

        // 调用
        PmsProjectDO readableProject = projectMemberService.validateProjectReadable(project.getId(), randomLongId());

        // 断言
        assertEquals(project.getId(), readableProject.getId());
    }

    @Test
    public void testValidateProjectReadable_privateProjectAccessDenied() {
        // mock 数据
        PmsProjectDO project = randomProjectDO("私有项目", PmsProjectStatusEnum.ACTIVE.getStatus())
                .setOpenStatus(false);
        when(projectService.getProject(project.getId())).thenReturn(project);

        // 调用，并断言异常
        assertServiceException(() -> projectMemberService.validateProjectReadable(project.getId(), randomLongId()),
                PROJECT_ACCESS_DENIED);
    }

    @Test
    public void testValidateProjectReadable_superAdmin() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO("私有项目", PmsProjectStatusEnum.ACTIVE.getStatus())
                .setOpenStatus(false);
        when(projectService.getProject(project.getId())).thenReturn(project);
        when(permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode())).thenReturn(true);

        // 调用
        PmsProjectDO readableProject = projectMemberService.validateProjectReadable(project.getId(), userId);

        // 断言
        assertEquals(project.getId(), readableProject.getId());
    }

    @Test
    public void testValidateProjectWritable_writeLevel() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO("可编辑项目", PmsProjectStatusEnum.ACTIVE.getStatus());
        when(projectService.getProject(project.getId())).thenReturn(project);
        projectMemberMapper.insert(randomProjectMemberDO(project.getId(), userId)
                .setLevel(PmsProjectMemberLevelEnum.WRITE.getLevel()));

        // 调用
        PmsProjectDO writableProject = projectMemberService.validateProjectWritable(project.getId(), userId);

        // 断言
        assertEquals(project.getId(), writableProject.getId());
    }

    @Test
    public void testValidateProjectWritable_readLevel() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO("只读项目", PmsProjectStatusEnum.ACTIVE.getStatus());
        when(projectService.getProject(project.getId())).thenReturn(project);
        projectMemberMapper.insert(randomProjectMemberDO(project.getId(), userId)
                .setLevel(PmsProjectMemberLevelEnum.READ.getLevel()));

        // 调用，并断言异常
        assertServiceException(() -> projectMemberService.validateProjectWritable(project.getId(), userId),
                PROJECT_WRITE_ACCESS_DENIED);
    }

    // ========== 随机对象 ==========

    private static PmsProjectDO randomProjectDO(Long projectId, Long creatorId) {
        return randomPojo(PmsProjectDO.class, project -> project.setId(projectId).setName("测试项目")
                .setStatus(1).setType(1).setLevel(3).setOpenStatus(false).setCreator(String.valueOf(creatorId)));
    }

    private static PmsProjectMemberDO randomProjectMemberDO(Long projectId, Long userId,
                                                             PmsProjectMemberLevelEnum level) {
        return randomPojo(PmsProjectMemberDO.class, member -> member.setProjectId(projectId)
                .setUserId(userId).setLevel(level.getLevel()));
    }

    private static PmsProjectDO randomProjectDO(String name, Integer status) {
        return randomPojo(PmsProjectDO.class, project -> project.setName(name).setStatus(status)
                .setType(1).setLevel(3).setOpenStatus(true));
    }

    private static PmsProjectMemberDO randomProjectMemberDO(Long projectId, Long userId) {
        return randomPojo(PmsProjectMemberDO.class, member -> member.setProjectId(projectId)
                .setUserId(userId).setLevel(PmsProjectMemberLevelEnum.WRITE.getLevel()));
    }

}
