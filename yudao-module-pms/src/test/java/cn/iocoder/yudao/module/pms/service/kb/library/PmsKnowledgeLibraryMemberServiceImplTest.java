package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
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
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_ACCESS_DENIED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_ADMIN_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_CREATOR_CANNOT_REMOVE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_MEMBERS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeLibraryMemberServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeLibraryMemberServiceImpl.class)
public class PmsKnowledgeLibraryMemberServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeLibraryMemberServiceImpl memberService;

    @Resource
    private PmsKnowledgeLibraryMemberMapper memberMapper;

    @MockitoBean
    private PmsKnowledgeLibraryService libraryService;
    @MockitoBean
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @MockitoBean
    private PermissionApi permissionApi;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private DeptApi deptApi;

    @Test
    public void testValidateLibraryReadable_privateDenied() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, false));

        // 调用，并断言异常
        assertServiceException(() -> memberService.validateLibraryReadable(libraryId, userId),
                KNOWLEDGE_LIBRARY_ACCESS_DENIED);
    }

    @Test
    public void testValidateLibraryReadable_privateMember() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO(libraryId, false);
        when(libraryService.getLibrary(libraryId)).thenReturn(library);
        memberMapper.insert(randomMemberDO(libraryId, userId, null,
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));

        // 调用
        PmsKnowledgeLibraryDO readableLibrary = memberService.validateLibraryReadable(libraryId, userId);

        // 断言
        assertEquals(library, readableLibrary);
    }

    @Test
    public void testValidateLibraryReadable_privateDeptMember() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        Long deptId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO(libraryId, false);
        when(libraryService.getLibrary(libraryId)).thenReturn(library);
        when(adminUserApi.getUser(userId)).thenReturn(new AdminUserRespDTO().setId(userId).setDeptId(deptId));
        memberMapper.insert(randomMemberDO(libraryId, null, deptId,
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));

        // 调用
        PmsKnowledgeLibraryDO readableLibrary = memberService.validateLibraryReadable(libraryId, userId);

        // 断言
        assertEquals(library, readableLibrary);
    }

    @Test
    public void testValidateLibraryReadable_parentDeptMemberDenied() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        Long deptId = randomLongId();
        Long parentDeptId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, false));
        when(adminUserApi.getUser(userId)).thenReturn(new AdminUserRespDTO().setId(userId).setDeptId(deptId));
        memberMapper.insert(randomMemberDO(libraryId, null, parentDeptId,
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));

        // 调用，并断言异常
        assertServiceException(() -> memberService.validateLibraryReadable(libraryId, userId),
                KNOWLEDGE_LIBRARY_ACCESS_DENIED);
    }

    @Test
    public void testGetJoinedLibraryIdList_filterInvalidLibrary() {
        // mock 数据
        Long userId = randomLongId();
        Long invalidLibraryId = randomLongId();
        Long validLibraryId = randomLongId();
        memberMapper.insert(randomMemberDO(invalidLibraryId, userId, null,
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));
        memberMapper.insert(randomMemberDO(validLibraryId, userId, null,
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));
        when(libraryService.getLibraryIdList(null)).thenReturn(Collections.singletonList(validLibraryId));

        // 调用
        List<Long> joinedLibraryIds = memberService.getJoinedLibraryIdList(userId);

        // 断言
        assertEquals(Collections.singletonList(validLibraryId), joinedLibraryIds);
    }

    @Test
    public void testCreateMemberList_success() {
        // 准备参数
        Long libraryId = randomLongId();
        Long creatorUserId = randomLongId();
        Long adminUserId = randomLongId();
        Long memberUserId = randomLongId();
        // 调用
        memberService.createLibraryMemberList(libraryId, creatorUserId,
                Collections.singletonList(adminUserId), Collections.singletonList(memberUserId));

        // 断言
        List<PmsKnowledgeLibraryMemberDO> members = memberMapper.selectListByLibraryId(libraryId);
        assertEquals(3, members.size());
        assertNotNull(CollUtil.findOne(members, member ->
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel().equals(member.getLevel())));
        assertNotNull(CollUtil.findOne(members, member ->
                adminUserId.equals(member.getUserId())
                        && PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel().equals(member.getLevel())));
        assertNotNull(CollUtil.findOne(members, member ->
                memberUserId.equals(member.getUserId())
                        && PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel().equals(member.getLevel())));
        verify(adminUserApi).validateUserList(new LinkedHashSet<>(Arrays.asList(adminUserId, memberUserId)));
    }

    @Test
    public void testCreateMemberList_duplicateMember_invalid() {
        // 准备参数
        Long libraryId = randomLongId();
        Long creatorUserId = randomLongId();
        Long memberUserId = randomLongId();
        // 调用，并断言异常
        assertServiceException(() -> memberService.createLibraryMemberList(libraryId, creatorUserId,
                        Arrays.asList(memberUserId, memberUserId), Collections.emptyList()),
                KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
    }

    @Test
    public void testCreateMemberList_duplicateRoleMember_invalid() {
        // 准备参数
        Long libraryId = randomLongId();
        Long creatorUserId = randomLongId();
        Long memberUserId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> memberService.createLibraryMemberList(libraryId, creatorUserId,
                        Collections.singletonList(memberUserId), Collections.singletonList(memberUserId)),
                KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
    }

    @Test
    public void testIsLibraryAdmin_publicAdminMemberAllowed() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, true));
        memberMapper.insert(randomMemberDO(libraryId, userId, null,
                PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel()));

        // 调用，并断言：公开库管理员可以参与库和内容协作
        assertTrue(memberService.isLibraryAdmin(libraryId, userId));
    }

    @Test
    public void testValidateLibraryAdmin_publicAdminMemberAllowed() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, true));
        memberMapper.insert(randomMemberDO(libraryId, userId, null,
                PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel()));

        // 调用
        assertNotNull(memberService.validateLibraryAdmin(libraryId, userId));
    }

    @Test
    public void testValidateLibraryCreator_adminMemberDenied() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, true));
        memberMapper.insert(randomMemberDO(libraryId, userId, null,
                PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel()));

        // 调用，并断言：管理员可以协作，但不能执行创建人级别的高危操作
        assertServiceException(() -> memberService.validateLibraryCreator(libraryId, userId),
                KNOWLEDGE_LIBRARY_ADMIN_REQUIRED);
    }

    @Test
    public void testUpdateMemberList_publicAllowsMembers() {
        // mock 数据
        Long libraryId = randomLongId();
        Long creatorUserId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, true));
        memberMapper.insert(randomMemberDO(libraryId, creatorUserId, null,
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel()));
        PmsKnowledgeLibraryUpdateMemberListReqVO reqVO = new PmsKnowledgeLibraryUpdateMemberListReqVO()
                .setLibraryId(libraryId).setMembers(Collections.singletonList(
                        new PmsKnowledgeLibraryUpdateMemberListReqVO.Member().setUserId(randomLongId())
                                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel())));

        // 调用
        memberService.updateLibraryMemberList(reqVO, creatorUserId);

        // 断言
        List<PmsKnowledgeLibraryMemberDO> members = memberMapper.selectListByLibraryId(libraryId);
        assertEquals(2, members.size());
    }

    @Test
    public void testUpdateMemberList_deptMember() {
        // mock 数据
        Long creatorUserId = randomLongId();
        Long libraryId = randomLongId();
        Long deptId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, false));
        memberMapper.insert(randomMemberDO(libraryId, creatorUserId, null,
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel()));
        // 准备参数
        PmsKnowledgeLibraryUpdateMemberListReqVO reqVO = new PmsKnowledgeLibraryUpdateMemberListReqVO()
                .setLibraryId(libraryId).setMembers(Collections.singletonList(
                        new PmsKnowledgeLibraryUpdateMemberListReqVO.Member().setDeptId(deptId)
                                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel())));

        // 调用
        memberService.updateLibraryMemberList(reqVO, creatorUserId);

        // 断言
        List<PmsKnowledgeLibraryMemberDO> members = memberMapper.selectListByLibraryId(libraryId);
        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(member -> deptId.equals(member.getDeptId())));
    }

    @Test
    public void testUpdateMemberList_retainCreator() {
        // mock 数据
        Long creatorUserId = randomLongId();
        Long libraryId = randomLongId();
        Long memberUserId = randomLongId();
        Long oldMemberUserId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, false));
        memberMapper.insert(randomMemberDO(libraryId, creatorUserId, null,
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel()));
        memberMapper.insert(randomMemberDO(libraryId, oldMemberUserId, null,
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));
        // 准备参数
        PmsKnowledgeLibraryUpdateMemberListReqVO reqVO = new PmsKnowledgeLibraryUpdateMemberListReqVO()
                .setLibraryId(libraryId).setMembers(Collections.singletonList(
                        new PmsKnowledgeLibraryUpdateMemberListReqVO.Member().setUserId(memberUserId)
                                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel())));

        // 调用
        memberService.updateLibraryMemberList(reqVO, creatorUserId);

        // 断言
        List<PmsKnowledgeLibraryMemberDO> members = memberMapper.selectListByLibraryId(libraryId);
        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(item -> item.getUserId().equals(creatorUserId)));
        assertTrue(members.stream().anyMatch(item -> item.getUserId().equals(memberUserId)));
        assertFalse(members.stream().anyMatch(item -> item.getUserId().equals(oldMemberUserId)));
    }

    @Test
    public void testUpdateMemberList_creatorDuplicated() {
        // mock 数据
        Long creatorUserId = randomLongId();
        Long libraryId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, false));
        memberMapper.insert(randomMemberDO(libraryId, creatorUserId, null,
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel()));
        // 准备参数
        PmsKnowledgeLibraryUpdateMemberListReqVO reqVO = new PmsKnowledgeLibraryUpdateMemberListReqVO()
                .setLibraryId(libraryId).setMembers(Collections.singletonList(
                        new PmsKnowledgeLibraryUpdateMemberListReqVO.Member().setUserId(creatorUserId)
                                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel())));

        // 调用，并断言异常
        assertServiceException(() -> memberService.updateLibraryMemberList(reqVO, creatorUserId),
                KNOWLEDGE_LIBRARY_MEMBERS_INVALID);
    }

    @Test
    public void testExitLibrary_success() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, false));
        memberMapper.insert(randomMemberDO(libraryId, userId, null,
                PmsKnowledgeLibraryMemberLevelEnum.MEMBER.getLevel()));

        // 调用
        memberService.exitLibrary(libraryId, userId);

        // 断言
        assertNull(memberMapper.selectByLibraryIdAndUserId(libraryId, userId));
        verify(contentPermissionService).deleteContentPermissionMembersByLibraryId(libraryId, Collections.singleton(userId),
                Collections.emptySet());
    }

    @Test
    public void testExitLibrary_creatorDenied() {
        // mock 数据
        Long creatorUserId = randomLongId();
        Long libraryId = randomLongId();
        when(libraryService.getLibrary(libraryId)).thenReturn(randomLibraryDO(libraryId, false));
        memberMapper.insert(randomMemberDO(libraryId, creatorUserId, null,
                PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel()));

        // 调用，并断言异常
        assertServiceException(() -> memberService.exitLibrary(libraryId, creatorUserId),
                KNOWLEDGE_LIBRARY_CREATOR_CANNOT_REMOVE);
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeLibraryMemberDO randomMemberDO(Long libraryId, Long userId, Long deptId, Integer level) {
        return randomPojo(PmsKnowledgeLibraryMemberDO.class, member -> member.setId(null).setLibraryId(libraryId)
                .setUserId(userId).setDeptId(deptId).setLevel(level));
    }

    private static PmsKnowledgeLibraryDO randomLibraryDO(Long libraryId, boolean openStatus) {
        return randomPojo(PmsKnowledgeLibraryDO.class, library -> library.setId(libraryId)
                .setOpenStatus(openStatus).setStatus(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus()));
    }

}
