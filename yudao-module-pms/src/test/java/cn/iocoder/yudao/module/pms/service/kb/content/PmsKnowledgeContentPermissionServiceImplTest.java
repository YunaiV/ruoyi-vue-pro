package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionMemberDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeContentPermissionMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeContentPermissionMemberMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeFolderMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMemberMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeContentLevelEnum;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeLibraryMemberLevelEnum;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
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
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeContentPermissionServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeContentPermissionServiceImpl.class)
public class PmsKnowledgeContentPermissionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeContentPermissionServiceImpl permissionService;

    @Resource
    private PmsKnowledgeContentPermissionMapper permissionMapper;
    @Resource
    private PmsKnowledgeContentPermissionMemberMapper permissionMemberMapper;
    @Resource
    private PmsKnowledgeFolderMapper folderMapper;
    @Resource
    private PmsKnowledgeLibraryMemberMapper libraryMemberMapper;

    @MockitoBean
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @MockitoBean
    private PmsKnowledgeFolderService folderService;
    @MockitoBean
    private PmsKnowledgeDocumentService documentService;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private DeptApi deptApi;
    @MockitoBean
    private PermissionApi permissionApi;

    @Test
    public void testCreateDefaultPermission() {
        // 准备参数
        Long libraryId = randomLongId();
        Long userId = randomLongId();

        // 调用
        Long permissionId = permissionService.createDefaultContentPermission(libraryId, userId);

        // 断言
        PmsKnowledgeContentPermissionDO permission = permissionMapper.selectById(permissionId);
        assertNotNull(permission);
        assertEquals(libraryId, permission.getLibraryId());
        assertTrue(permission.getOpenStatus());
        assertEquals(PmsKnowledgeContentLevelEnum.PREVIEW.getLevel(), permission.getOpenLevel());
        PmsKnowledgeContentPermissionMemberDO owner = permissionMemberMapper.selectByPermissionIdAndUserId(permissionId, userId);
        assertNotNull(owner);
        assertEquals(PmsKnowledgeContentLevelEnum.MANAGE.getLevel(), owner.getLevel());
    }

    @Test
    public void testGetCurrentUserLevel_openBeforeUserAndDept() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        Long deptId = randomLongId();
        PmsKnowledgeContentPermissionDO permission = randomPermissionDO(libraryId, true,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(permission);
        permissionMemberMapper.insert(new PmsKnowledgeContentPermissionMemberDO().setPermissionId(permission.getId()).setDeptId(deptId)
                .setLevel(PmsKnowledgeContentLevelEnum.DOWNLOAD.getLevel()));
        permissionMemberMapper.insert(new PmsKnowledgeContentPermissionMemberDO().setPermissionId(permission.getId()).setUserId(userId)
                .setLevel(PmsKnowledgeContentLevelEnum.EDIT.getLevel()));

        // mock 方法
        when(libraryMemberService.isLibraryAdmin(libraryId, userId)).thenReturn(false);
        when(adminUserApi.getUser(userId)).thenReturn(new AdminUserRespDTO().setId(userId).setDeptId(deptId));

        // 调用
        Integer level = permissionService.getCurrentUserContentPermissionLevel(permission.getId(), libraryId, userId);

        // 断言
        assertEquals(PmsKnowledgeContentLevelEnum.PREVIEW.getLevel(), level);
    }

    @Test
    public void testGetContentPermissionMemberList_readableUser() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        Long collaboratorUserId = randomLongId();
        PmsKnowledgeContentPermissionDO permission = randomPermissionDO(libraryId, true,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(permission);
        permissionMemberMapper.insert(new PmsKnowledgeContentPermissionMemberDO()
                .setPermissionId(permission.getId()).setUserId(collaboratorUserId)
                .setLevel(PmsKnowledgeContentLevelEnum.EDIT.getLevel()));

        // mock 方法
        when(libraryMemberService.isLibraryAdmin(libraryId, userId)).thenReturn(false);
        when(adminUserApi.getUser(userId)).thenReturn(new AdminUserRespDTO().setId(userId));

        // 调用
        Set<Long> memberUserIds = new LinkedHashSet<>();
        permissionService.getContentPermissionMemberList(permission.getId(), userId)
                .forEach(member -> memberUserIds.add(member.getUserId()));

        // 断言
        assertEquals(Collections.singleton(collaboratorUserId), memberUserIds);
        verify(libraryMemberService).validateLibraryReadable(libraryId, userId);
    }

    @Test
    public void testGetCurrentUserLevel_privateDenied() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        PmsKnowledgeContentPermissionDO permission = randomPermissionDO(libraryId, false,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(permission);

        // mock 方法
        when(libraryMemberService.isLibraryAdmin(libraryId, userId)).thenReturn(false);
        when(adminUserApi.getUser(userId)).thenReturn(new AdminUserRespDTO().setId(userId));

        // 调用
        Integer level = permissionService.getCurrentUserContentPermissionLevel(permission.getId(), libraryId, userId);

        // 断言
        assertNull(level);
    }

    @Test
    public void testClonePermissions_copiesMembersAndChangesLibrary() {
        // mock 数据
        Long sourceLibraryId = randomLongId();
        Long targetLibraryId = randomLongId();
        Long userId = randomLongId();
        PmsKnowledgeContentPermissionDO source = randomPermissionDO(sourceLibraryId, false,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(source);
        permissionMemberMapper.insert(new PmsKnowledgeContentPermissionMemberDO().setPermissionId(source.getId()).setUserId(userId)
                .setLevel(PmsKnowledgeContentLevelEnum.MANAGE.getLevel()));

        // 调用
        Map<Long, Long> clonedPermissionIdMap = permissionService.cloneContentPermissions(
                Collections.singleton(source.getId()), targetLibraryId);

        // 断言
        Long targetPermissionId = clonedPermissionIdMap.get(source.getId());
        assertNotNull(targetPermissionId);
        assertNotEquals(source.getId(), targetPermissionId);
        PmsKnowledgeContentPermissionDO target = permissionMapper.selectById(targetPermissionId);
        assertEquals(targetLibraryId, target.getLibraryId());
        assertFalse(target.getOpenStatus());
        assertNotNull(permissionMemberMapper.selectByPermissionIdAndUserId(targetPermissionId, userId));
    }

    @Test
    public void testGetReadablePermissionIds_batchAcrossLibraries() {
        // mock 数据
        Long userId = randomLongId();
        Long deptId = randomLongId();
        Long adminLibraryId = randomLongId();
        Long collaboratorLibraryId = randomLongId();
        Long deniedLibraryId = randomLongId();
        PmsKnowledgeContentPermissionDO adminPermission = randomPermissionDO(adminLibraryId, false,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(adminPermission);
        PmsKnowledgeContentPermissionDO collaboratorPermission = randomPermissionDO(collaboratorLibraryId, false,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(collaboratorPermission);
        PmsKnowledgeContentPermissionDO deniedPermission = randomPermissionDO(deniedLibraryId, false,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(deniedPermission);
        libraryMemberMapper.insert(new PmsKnowledgeLibraryMemberDO().setLibraryId(adminLibraryId).setUserId(userId)
                .setLevel(PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel()));
        permissionMemberMapper.insert(new PmsKnowledgeContentPermissionMemberDO()
                .setPermissionId(collaboratorPermission.getId()).setDeptId(deptId)
                .setLevel(PmsKnowledgeContentLevelEnum.PREVIEW.getLevel()));

        // mock 方法
        when(adminUserApi.getUser(userId)).thenReturn(new AdminUserRespDTO().setId(userId).setDeptId(deptId));

        // 调用
        Set<Long> permissionIds = permissionService.getReadableContentPermissionIdSet(
                Arrays.asList(adminLibraryId, collaboratorLibraryId, deniedLibraryId), userId);

        // 断言
        assertEquals(new LinkedHashSet<>(Arrays.asList(adminPermission.getId(), collaboratorPermission.getId())),
                permissionIds);
        verify(libraryMemberService, never()).isLibraryAdmin(anyLong(), anyLong());
    }

    @Test
    public void testDeleteUnusedPermissions_preservesReferencedPermission() {
        // mock 数据
        Long libraryId = randomLongId();
        PmsKnowledgeContentPermissionDO referenced = randomPermissionDO(libraryId, true,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(referenced);
        PmsKnowledgeContentPermissionDO unused = randomPermissionDO(libraryId, true,
                PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());
        permissionMapper.insert(unused);
        folderMapper.insert(new PmsKnowledgeFolderDO().setLibraryId(libraryId).setPermissionId(referenced.getId())
                .setParentId(0L).setTitle("测试目录").setStatus(1));
        when(folderService.getExistingContentPermissionIdSet(anyCollection()))
                .thenReturn(Collections.singleton(referenced.getId()));
        when(documentService.getExistingContentPermissionIdSet(anyCollection()))
                .thenReturn(Collections.emptySet());

        // 调用
        permissionService.deleteUnusedContentPermissions(new LinkedHashSet<>(
                Arrays.asList(referenced.getId(), unused.getId())));

        // 断言
        assertNotNull(permissionMapper.selectById(referenced.getId()));
        assertNull(permissionMapper.selectById(unused.getId()));
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeContentPermissionDO randomPermissionDO(Long libraryId, boolean openStatus,
                                                               Integer openLevel) {
        return randomPojo(PmsKnowledgeContentPermissionDO.class, permission -> permission.setId(null)
                .setLibraryId(libraryId).setOpenStatus(openStatus).setOpenLevel(openLevel)
                .setCreator(String.valueOf(randomLongId())));
    }

}
