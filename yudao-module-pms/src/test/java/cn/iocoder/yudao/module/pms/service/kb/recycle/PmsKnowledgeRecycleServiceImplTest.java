package cn.iocoder.yudao.module.pms.service.kb.recycle;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.recycle.PmsKnowledgeRecycleRecordDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeFolderMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMemberMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.recycle.PmsKnowledgeRecycleRecordMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeContentLevelEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeLibraryMemberLevelEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentLabelService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentServiceImpl;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeFolderServiceImpl;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentCommentService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentLikeService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentShareService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeViewRecordService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeGroupService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryServiceImpl;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryTemplateService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_RECYCLE_TYPE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeRecycleServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsKnowledgeRecycleServiceImpl.class, PmsKnowledgeLibraryServiceImpl.class,
        PmsKnowledgeFolderServiceImpl.class, PmsKnowledgeDocumentServiceImpl.class})
public class PmsKnowledgeRecycleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeRecycleServiceImpl recycleService;

    @Resource
    private PmsKnowledgeLibraryMapper libraryMapper;
    @Resource
    private PmsKnowledgeLibraryMemberMapper memberMapper;
    @Resource
    private PmsKnowledgeFolderMapper folderMapper;
    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;
    @Resource
    private PmsKnowledgeRecycleRecordMapper recycleRecordMapper;

    @MockBean
    private PmsKnowledgeGroupService knowledgeGroupService;
    @MockBean
    private PmsKnowledgeFavoriteService favoriteService;
    @MockBean
    private PmsKnowledgeDocumentLikeService documentLikeService;
    @MockBean
    private PmsKnowledgeViewRecordService viewRecordService;
    @MockBean
    private PmsKnowledgeDocumentShareService documentShareService;
    @MockBean
    private PmsKnowledgeDocumentCommentService documentCommentService;
    @MockBean
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @MockBean
    private PmsKnowledgeDocumentLabelService documentLabelService;
    @MockBean
    private PermissionApi permissionApi;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private PmsKnowledgeLibraryTemplateService libraryTemplateService;
    @MockBean
    private PmsKnowledgeLibraryMemberService memberService;

    @Test
    public void testRestoreDocument_preservesExplicitlyDeletedChild() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeLibraryMemberDO member = randomMemberDO(library.getId(), userId);
        memberMapper.insert(member);
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.getMemberByLibraryIdAndUserId(library.getId(), userId)).thenReturn(member);
        PmsKnowledgeDocumentDO parent = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(parent);
        PmsKnowledgeDocumentDO child = randomDocumentDO(library.getId(), 0L, parent.getId());
        documentMapper.insert(child);
        recycleService.recycleDocument(child, Collections.singletonList(child), userId);
        recycleService.recycleDocument(parent, Arrays.asList(parent, child), userId);
        PmsKnowledgeRecycleRecordDO parentRecord = recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), parent.getId());

        // 调用
        recycleService.restoreContentRecycle(parentRecord.getId(), userId);

        // 断言
        assertEquals(1, documentMapper.selectById(parent.getId()).getStatus());
        assertEquals(-1, documentMapper.selectById(child.getId()).getStatus());
        assertNotNull(recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), child.getId()));
    }

    @Test
    public void testRestoreLibrary_preservesExplicitlyDeletedFolder() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeLibraryMemberDO member = randomMemberDO(library.getId(), userId);
        memberMapper.insert(member);
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.getMemberByLibraryIdAndUserId(library.getId(), userId)).thenReturn(member);
        PmsKnowledgeFolderDO deletedFolder = randomFolderDO(library.getId(), 0L);
        folderMapper.insert(deletedFolder);
        PmsKnowledgeFolderDO normalFolder = randomFolderDO(library.getId(), 0L);
        folderMapper.insert(normalFolder);
        recycleService.recycleFolder(deletedFolder, Collections.singletonList(deletedFolder),
                Collections.emptyList(), userId);
        recycleService.recycleLibrary(library, userId);
        PmsKnowledgeRecycleRecordDO libraryRecord = recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.LIBRARY.getType(), library.getId());

        // 调用
        recycleService.restoreContentRecycle(libraryRecord.getId(), userId);

        // 断言
        assertEquals(1, libraryMapper.selectById(library.getId()).getStatus());
        assertEquals(-1, folderMapper.selectById(deletedFolder.getId()).getStatus());
        assertEquals(1, folderMapper.selectById(normalFolder.getId()).getStatus());
    }

    @Test
    public void testPermanentDeleteDocument_recursive() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeLibraryMemberDO member = randomMemberDO(library.getId(), userId);
        memberMapper.insert(member);
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.getMemberByLibraryIdAndUserId(library.getId(), userId)).thenReturn(member);
        PmsKnowledgeDocumentDO parent = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(parent);
        PmsKnowledgeDocumentDO child = randomDocumentDO(library.getId(), 0L, parent.getId());
        documentMapper.insert(child);
        recycleService.recycleDocument(parent, Arrays.asList(parent, child), userId);
        PmsKnowledgeRecycleRecordDO record = recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), parent.getId());

        // 调用
        recycleService.deleteContentRecycle(record.getId(), userId);

        // 断言
        assertNull(documentMapper.selectById(parent.getId()));
        assertNull(documentMapper.selectById(child.getId()));
        assertNull(recycleRecordMapper.selectById(record.getId()));
    }

    @Test
    public void testRestoreDocument_repeatedRecycle() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeLibraryMemberDO member = randomMemberDO(library.getId(), userId);
        memberMapper.insert(member);
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.getMemberByLibraryIdAndUserId(library.getId(), userId)).thenReturn(member);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(document);
        recycleService.recycleDocument(document, Collections.singletonList(document), userId);
        PmsKnowledgeRecycleRecordDO firstRecord = recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), document.getId());
        recycleService.restoreContentRecycle(firstRecord.getId(), userId);

        // 调用
        document = documentMapper.selectById(document.getId());
        recycleService.recycleDocument(document, Collections.singletonList(document), userId);
        PmsKnowledgeRecycleRecordDO secondRecord = recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), document.getId());
        recycleService.restoreContentRecycle(secondRecord.getId(), userId);

        // 断言
        assertEquals(1, documentMapper.selectById(document.getId()).getStatus());
        assertNull(recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), document.getId()));
    }

    @Test
    public void testGetContentRecycleList_filtersByContentPermission() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeLibraryMemberDO member = randomMemberDO(library.getId(), userId);
        memberMapper.insert(member);
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.getMemberByLibraryIdAndUserId(library.getId(), userId)).thenReturn(member);
        PmsKnowledgeDocumentDO editableDocument = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(editableDocument);
        PmsKnowledgeDocumentDO previewDocument = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(previewDocument);
        recycleService.recycleDocument(editableDocument, Collections.singletonList(editableDocument), userId);
        recycleService.recycleDocument(previewDocument, Collections.singletonList(previewDocument), userId);
        Map<Long, Integer> levelMap = new LinkedHashMap<>();
        levelMap.put(editableDocument.getPermissionId(), PmsKnowledgeContentLevelEnum.EDIT.getLevel());
        levelMap.put(previewDocument.getPermissionId(), PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());

        // mock 方法
        when(contentPermissionService.getCurrentUserContentPermissionLevelMap(anyCollection(), eq(library.getId()), eq(userId)))
                .thenReturn(levelMap);

        // 调用
        List<PmsKnowledgeRecycleRecordDO> records = recycleService.getContentRecycleList(library.getId(), userId);

        // 断言
        assertEquals(1, records.size());
        assertEquals(editableDocument.getId(), CollUtil.getFirst(records).getEntityId());
    }

    @Test
    public void testGetContentRecycleList_sameEntityIdUsesObjectTypePermission() {
        // mock 数据：文件夹和文档来自不同表，刻意使用相同的业务 ID
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeLibraryMemberDO member = randomMemberDO(library.getId(), userId);
        memberMapper.insert(member);
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.getMemberByLibraryIdAndUserId(library.getId(), userId)).thenReturn(member);
        Long sharedEntityId = 100L;
        Long editablePermissionId = randomLongId();
        Long previewPermissionId = randomLongId();
        PmsKnowledgeFolderDO folder = randomFolderDO(library.getId(), 0L)
                .setId(sharedEntityId).setPermissionId(editablePermissionId);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId(), 0L, 0L)
                .setId(sharedEntityId).setPermissionId(previewPermissionId);
        folderMapper.insert(folder);
        documentMapper.insert(document);
        recycleService.recycleFolder(folder, Collections.singletonList(folder), Collections.emptyList(), userId);
        recycleService.recycleDocument(document, Collections.singletonList(document), userId);
        Map<Long, Integer> levelMap = new LinkedHashMap<>();
        levelMap.put(editablePermissionId, PmsKnowledgeContentLevelEnum.EDIT.getLevel());
        levelMap.put(previewPermissionId, PmsKnowledgeContentLevelEnum.PREVIEW.getLevel());

        // mock 方法
        when(contentPermissionService.getCurrentUserContentPermissionLevelMap(anyCollection(), eq(library.getId()), eq(userId)))
                .thenReturn(levelMap);

        // 调用
        List<PmsKnowledgeRecycleRecordDO> records = recycleService.getContentRecycleList(library.getId(), userId);

        // 断言：文件夹权限为可删除，文档权限为仅预览，不能因同号 ID 串用权限
        assertEquals(1, records.size());
        assertEquals(PmsKnowledgeObjectTypeEnum.FOLDER.getType(), CollUtil.getFirst(records).getType());
        assertEquals(sharedEntityId, CollUtil.getFirst(records).getEntityId());
    }

    @Test
    public void testRestoreContentRecycle_unknownType() {
        // mock 数据
        PmsKnowledgeRecycleRecordDO record = randomPojo(PmsKnowledgeRecycleRecordDO.class,
                item -> item.setId(null).setLibraryId(randomLongId()).setType(999)
                        .setEntityId(randomLongId()).setName("未知类型"));
        recycleRecordMapper.insert(record);

        // 调用并断言统一业务异常
        assertServiceException(() -> recycleService.restoreContentRecycle(record.getId(), randomLongId()),
                KNOWLEDGE_RECYCLE_TYPE_INVALID);
    }

    @Test
    public void testDeleteContentRecycle_unknownType() {
        // mock 数据
        PmsKnowledgeRecycleRecordDO record = randomPojo(PmsKnowledgeRecycleRecordDO.class,
                item -> item.setId(null).setLibraryId(randomLongId()).setType(999)
                        .setEntityId(randomLongId()).setName("未知类型"));
        recycleRecordMapper.insert(record);

        // 调用并断言统一业务异常
        assertServiceException(() -> recycleService.deleteContentRecycle(record.getId(), randomLongId()),
                KNOWLEDGE_RECYCLE_TYPE_INVALID);
    }

    @Test
    public void testCleanExpiredRecycleRecords_onlyDeletesExpired() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO expiredDocument = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(expiredDocument);
        recycleService.recycleDocument(expiredDocument, Collections.singletonList(expiredDocument), userId);
        PmsKnowledgeRecycleRecordDO expiredRecord = recycleRecordMapper.selectByTypeAndEntityId(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), expiredDocument.getId());
        recycleRecordMapper.updateById(new PmsKnowledgeRecycleRecordDO().setId(expiredRecord.getId())
                .setDeleteTime(LocalDateTime.now().minusDays(31)));
        PmsKnowledgeDocumentDO recentDocument = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(recentDocument);
        recycleService.recycleDocument(recentDocument, Collections.singletonList(recentDocument), userId);

        // 调用
        int cleanCount = recycleService.deleteExpiredRecycleRecords(LocalDateTime.now().minusDays(30));

        // 断言
        assertEquals(1, cleanCount);
        assertNull(documentMapper.selectById(expiredDocument.getId()));
        assertNotNull(documentMapper.selectById(recentDocument.getId()));
    }

    @Test
    public void testDeleteExpiredRecycleRecords_empty() {
        // 调用
        int cleanCount = recycleService.deleteExpiredRecycleRecords(LocalDateTime.now().minusDays(30));

        // 断言
        assertEquals(0, cleanCount);
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeLibraryDO randomLibraryDO() {
        return randomPojo(PmsKnowledgeLibraryDO.class, library -> library.setId(null).setName("测试知识库")
                .setOpenStatus(false).setStatus(1));
    }

    private PmsKnowledgeLibraryMemberDO randomMemberDO(Long libraryId, Long userId) {
        return randomPojo(PmsKnowledgeLibraryMemberDO.class, member -> member.setId(null).setLibraryId(libraryId)
                .setUserId(userId).setLevel(PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel()));
    }

    private PmsKnowledgeFolderDO randomFolderDO(Long libraryId, Long parentId) {
        return randomPojo(PmsKnowledgeFolderDO.class, folder -> folder.setId(null).setLibraryId(libraryId)
                .setPermissionId(randomLongId()).setParentId(parentId).setTitle("测试目录").setStatus(1));
    }

    private PmsKnowledgeDocumentDO randomDocumentDO(Long libraryId, Long folderId, Long parentId) {
        return randomPojo(PmsKnowledgeDocumentDO.class, document -> document.setId(null).setLibraryId(libraryId)
                .setPermissionId(randomLongId()).setFolderId(folderId).setParentId(parentId).setTitle("测试文档")
                .setType(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType()).setStatus(1));
    }

}
