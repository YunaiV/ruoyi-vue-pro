package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentSearchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentUpdateReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeFolderMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMemberMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeLibraryMemberLevelEnum;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentCommentService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentLikeService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentShareService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeViewRecordService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeGroupService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryService;
import cn.iocoder.yudao.module.pms.service.kb.recycle.PmsKnowledgeRecycleServiceImpl;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_MOVE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_MOVE_TARGET_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeDocumentServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsKnowledgeDocumentServiceImpl.class, PmsKnowledgeRecycleServiceImpl.class})
public class PmsKnowledgeDocumentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeDocumentServiceImpl documentService;

    @Resource
    private PmsKnowledgeLibraryMapper libraryMapper;
    @Resource
    private PmsKnowledgeLibraryMemberMapper memberMapper;
    @Resource
    private PmsKnowledgeFolderMapper folderMapper;
    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;
    @MockBean
    private PermissionApi permissionApi;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private PmsKnowledgeLibraryService libraryService;
    @MockBean
    private PmsKnowledgeLibraryMemberService memberService;
    @MockBean
    private PmsKnowledgeGroupService knowledgeGroupService;
    @MockBean
    private PmsKnowledgeDocumentShareService knowledgeDocumentShareService;
    @MockBean
    private PmsKnowledgeDocumentCommentService knowledgeDocumentCommentService;
    @MockBean
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @MockBean
    private PmsKnowledgeDocumentLabelService documentLabelService;
    @MockBean
    private PmsKnowledgeFolderService folderService;
    @MockBean
    private PmsKnowledgeFavoriteService favoriteService;
    @MockBean
    private PmsKnowledgeDocumentLikeService documentLikeService;
    @MockBean
    private PmsKnowledgeViewRecordService viewRecordService;

    @Test
    public void testCreateDocument_folderWinsParent() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeFolderDO folder = randomFolderDO(library.getId(), 0L);
        folderMapper.insert(folder);
        PmsKnowledgeDocumentDO parent = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(parent);
        when(folderService.getFolder(folder.getId(), userId)).thenReturn(folder);
        // 准备参数
        PmsKnowledgeDocumentCreateReqVO reqVO = new PmsKnowledgeDocumentCreateReqVO()
                .setLibraryId(library.getId()).setFolderId(folder.getId()).setParentId(parent.getId())
                .setTitle("说明文档").setType(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType());

        // 调用
        Long documentId = documentService.createDocument(reqVO, userId);

        // 断言
        PmsKnowledgeDocumentDO document = documentMapper.selectById(documentId);
        assertEquals(folder.getId(), document.getFolderId());
        assertEquals(0L, document.getParentId());
        assertEquals(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType(), document.getType());
        assertEquals(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(), document.getStatus());
        verify(folderService).getFolder(folder.getId(), userId);
    }

    @Test
    public void testCreateDocument_fileDocument() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(folderService.getFolder(0L, userId)).thenReturn(null);
        // 准备参数
        PmsKnowledgeDocumentCreateReqVO reqVO = new PmsKnowledgeDocumentCreateReqVO()
                .setLibraryId(library.getId()).setFolderId(0L).setParentId(0L).setTitle("产品附件")
                .setType(PmsKnowledgeDocumentTypeEnum.FILE.getType()).setContent("https://example.com/a.pdf")
                .setFileType("pdf").setFileSize(2048L);

        // 调用
        Long documentId = documentService.createDocument(reqVO, userId);

        // 断言
        PmsKnowledgeDocumentDO document = documentMapper.selectById(documentId);
        assertEquals(PmsKnowledgeDocumentTypeEnum.FILE.getType(), document.getType());
        assertEquals("https://example.com/a.pdf", document.getContent());
        assertEquals("pdf", document.getFileType());
        assertEquals(2048L, document.getFileSize());
    }

    @Test
    public void testUpdateDocument_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId(), 0L, 0L).setContent("旧正文");
        documentMapper.insert(document);
        // 准备参数
        PmsKnowledgeDocumentUpdateReqVO reqVO = new PmsKnowledgeDocumentUpdateReqVO()
                .setId(document.getId()).setContent("新正文");

        // 调用
        documentService.updateDocument(reqVO, userId);

        // 断言
        PmsKnowledgeDocumentDO updatedDocument = documentMapper.selectById(document.getId());
        assertEquals("新正文", updatedDocument.getContent());
        assertEquals(document.getTitle(), updatedDocument.getTitle());
        assertEquals(document.getType(), updatedDocument.getType());
        assertEquals(document.getStatus(), updatedDocument.getStatus());
        verify(memberService).validateLibraryReadable(document.getLibraryId(), userId);
        verify(contentPermissionService).validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId);
    }

    @Test
    public void testUpdateDocument_titleAndContent() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId(), 0L, 0L).setContent("旧正文");
        documentMapper.insert(document);
        // 准备参数
        PmsKnowledgeDocumentUpdateReqVO reqVO = new PmsKnowledgeDocumentUpdateReqVO()
                .setId(document.getId()).setTitle("新标题").setContent("新正文");

        // 调用
        documentService.updateDocument(reqVO, userId);

        // 断言
        PmsKnowledgeDocumentDO updatedDocument = documentMapper.selectById(document.getId());
        assertEquals("新标题", updatedDocument.getTitle());
        assertEquals("新正文", updatedDocument.getContent());
    }

    @Test
    public void testDeleteDocument_recursive() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeDocumentDO parent = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(parent);
        PmsKnowledgeDocumentDO child = randomDocumentDO(library.getId(), 0L, parent.getId());
        documentMapper.insert(child);

        // 调用
        documentService.deleteDocument(parent.getId(), userId);

        // 断言
        assertEquals(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus(),
                documentMapper.selectById(parent.getId()).getStatus());
        assertEquals(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus(),
                documentMapper.selectById(child.getId()).getStatus());
        verify(memberService).validateLibraryReadable(library.getId(), userId);
    }

    @Test
    public void testMoveDocument_toDescendant() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeDocumentDO parent = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(parent);
        PmsKnowledgeDocumentDO child = randomDocumentDO(library.getId(), 0L, parent.getId());
        documentMapper.insert(child);
        // 准备参数
        PmsKnowledgeDocumentMoveReqVO reqVO = new PmsKnowledgeDocumentMoveReqVO().setId(parent.getId())
                .setTargetLibraryId(library.getId()).setTargetFolderId(0L).setTargetParentId(child.getId());

        // 调用，并断言异常
        assertServiceException(() -> documentService.moveDocument(reqVO, userId), KNOWLEDGE_DOCUMENT_MOVE_INVALID);
    }

    @Test
    public void testMoveDocument_targetFolderAndParentBothSet() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(document);
        // 准备参数
        PmsKnowledgeDocumentMoveReqVO reqVO = new PmsKnowledgeDocumentMoveReqVO().setId(document.getId())
                .setTargetLibraryId(library.getId()).setTargetFolderId(randomLongId()).setTargetParentId(randomLongId());

        // 调用，并断言异常
        assertServiceException(() -> documentService.moveDocument(reqVO, userId), KNOWLEDGE_DOCUMENT_MOVE_TARGET_INVALID);
    }

    @Test
    public void testMoveDocument_crossLibraryClonesPermission() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO sourceLibrary = randomLibraryDO();
        libraryMapper.insert(sourceLibrary);
        memberMapper.insert(randomMemberDO(sourceLibrary.getId(), userId));
        when(memberService.validateLibraryReadable(sourceLibrary.getId(), userId)).thenReturn(sourceLibrary);
        when(memberService.validateLibraryWritable(sourceLibrary.getId(), userId)).thenReturn(sourceLibrary);
        PmsKnowledgeLibraryDO targetLibrary = randomLibraryDO();
        libraryMapper.insert(targetLibrary);
        memberMapper.insert(randomMemberDO(targetLibrary.getId(), userId));
        when(memberService.validateLibraryReadable(targetLibrary.getId(), userId)).thenReturn(targetLibrary);
        when(memberService.validateLibraryWritable(targetLibrary.getId(), userId)).thenReturn(targetLibrary);
        PmsKnowledgeDocumentDO document = randomDocumentDO(sourceLibrary.getId(), 0L, 0L);
        documentMapper.insert(document);
        Long sourcePermissionId = document.getPermissionId();
        Long targetPermissionId = randomLongId();
        // 准备参数
        PmsKnowledgeDocumentMoveReqVO reqVO = new PmsKnowledgeDocumentMoveReqVO().setId(document.getId())
                .setTargetLibraryId(targetLibrary.getId()).setTargetFolderId(0L).setTargetParentId(0L);
        // mock 方法
        when(contentPermissionService.cloneContentPermissions(Collections.singleton(sourcePermissionId), targetLibrary.getId()))
                .thenReturn(Collections.singletonMap(sourcePermissionId, targetPermissionId));

        // 调用
        documentService.moveDocument(reqVO, userId);

        // 断言
        PmsKnowledgeDocumentDO movedDocument = documentMapper.selectById(document.getId());
        assertEquals(targetLibrary.getId(), movedDocument.getLibraryId());
        assertEquals(targetPermissionId, movedDocument.getPermissionId());
        verify(memberService).validateLibraryReadable(sourceLibrary.getId(), userId);
        verify(memberService).validateLibraryReadable(targetLibrary.getId(), userId);
        verify(memberService).validateLibraryAdmin(targetLibrary.getId(), userId);
        verify(contentPermissionService).validateContentPermissionManageable(
                sourcePermissionId, sourceLibrary.getId(), userId);
        verify(favoriteService).updateFavoriteLibraryIdByEntityIds(Collections.emptySet(),
                Collections.singleton(document.getId()), targetLibrary.getId());
        verify(viewRecordService).updateViewRecordLibraryIdByEntityIds(Collections.emptySet(),
                Collections.singleton(document.getId()), targetLibrary.getId());
        verify(contentPermissionService).deleteUnusedContentPermissions(Collections.singleton(sourcePermissionId));
    }

    @Test
    public void testGetDocumentList_hidesChildWhenParentInvisible() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO parent = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(parent);
        PmsKnowledgeDocumentDO child = randomDocumentDO(library.getId(), 0L, parent.getId());
        documentMapper.insert(child);

        // mock 方法：只有子文档可读，父文档不可读
        when(contentPermissionService.getCurrentUserContentPermissionLevelMap(
                anyCollection(), eq(library.getId()), eq(userId)))
                .thenReturn(Collections.singletonMap(child.getPermissionId(), 3));

        // 调用并断言
        assertTrue(documentService.getDocumentList(library.getId(), userId).isEmpty());
        verify(memberService).validateLibraryReadable(library.getId(), userId);
    }

    @Test
    public void testGetDocument_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(document);

        // 调用
        PmsKnowledgeDocumentDO foundDocument = documentService.getDocument(document.getId(), userId);

        // 断言
        assertEquals(document.getId(), foundDocument.getId());
        verify(contentPermissionService).validateContentPermissionReadable(document.getPermissionId(), library.getId(), userId);
    }

    @Test
    public void testGetDocument_recycled() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId(), 0L, 0L);
        documentMapper.insert(document);
        documentMapper.updateById(new PmsKnowledgeDocumentDO().setId(document.getId())
                .setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> documentService.getDocument(document.getId(), userId),
                KNOWLEDGE_DOCUMENT_NOT_EXISTS);
    }

    @Test
    public void testGetDocumentTypeCountMap_success() {
        // mock 数据
        Long libraryId = randomLongId();
        Long otherLibraryId = randomLongId();
        documentMapper.insert(randomDocumentDO(libraryId, 0L, 0L));
        documentMapper.insert(randomDocumentDO(libraryId, 0L, 0L));
        documentMapper.insert(randomDocumentDO(libraryId, 0L, 0L)
                .setType(PmsKnowledgeDocumentTypeEnum.FILE.getType()));
        documentMapper.insert(randomDocumentDO(libraryId, 0L, 0L)
                .setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus()));
        documentMapper.insert(randomDocumentDO(otherLibraryId, 0L, 0L));

        // 调用
        Map<Long, Map<Integer, Long>> typeCountMap = documentService.getDocumentTypeCountMap(
                Arrays.asList(libraryId, otherLibraryId));

        // 断言
        assertEquals(2L, typeCountMap.get(libraryId).get(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType()));
        assertEquals(1L, typeCountMap.get(libraryId).get(PmsKnowledgeDocumentTypeEnum.FILE.getType()));
        assertEquals(1L, typeCountMap.get(otherLibraryId).get(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType()));
    }

    @Test
    public void testGetDocumentSearchPage_mysqlAndAccessScope() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        Long otherLibraryId = randomLongId();
        Long readablePermissionId = randomLongId();
        documentMapper.insert(randomSearchDocumentDO(libraryId, readablePermissionId, "产品说明", "普通正文", 1));
        documentMapper.insert(randomSearchDocumentDO(libraryId, readablePermissionId,
                "普通标题", "包含产品设计方案", 1));
        documentMapper.insert(randomSearchDocumentDO(libraryId, randomLongId(), "产品私密文档", "不可见", 1));
        documentMapper.insert(randomSearchDocumentDO(otherLibraryId, readablePermissionId,
                "产品已删除文档", "回收站", -1));
        when(memberService.getReadableLibraryIdList(userId)).thenReturn(Arrays.asList(libraryId, otherLibraryId));
        when(contentPermissionService.getReadableContentPermissionIdSet(Arrays.asList(libraryId, otherLibraryId), userId))
                .thenReturn(Collections.singleton(readablePermissionId));
        // 准备参数
        PmsKnowledgeDocumentSearchPageReqVO pageReqVO = new PmsKnowledgeDocumentSearchPageReqVO()
                .setKeyword("产品");
        pageReqVO.setPageNo(1).setPageSize(10);

        // 调用
        PageResult<PmsKnowledgeDocumentDO> pageResult = documentService.getDocumentSearchPage(pageReqVO, userId);

        // 断言
        assertEquals(2, pageResult.getTotal());
        assertEquals("产品", pageReqVO.getKeyword());
    }

    @Test
    public void testGetDocumentSearchPage_libraryDenied() {
        // mock 数据
        Long userId = randomLongId();
        Long readableLibraryId = randomLongId();
        when(memberService.getReadableLibraryIdList(userId))
                .thenReturn(Collections.singletonList(readableLibraryId));
        // 准备参数
        PmsKnowledgeDocumentSearchPageReqVO pageReqVO = new PmsKnowledgeDocumentSearchPageReqVO()
                .setKeyword("产品").setLibraryId(randomLongId());

        // 调用
        PageResult<PmsKnowledgeDocumentDO> pageResult = documentService.getDocumentSearchPage(pageReqVO, userId);

        // 断言
        assertEquals(0, pageResult.getTotal());
    }

    @Test
    public void testGetDocumentSearchPage_withoutKeyword() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        Long readablePermissionId = randomLongId();
        documentMapper.insert(randomSearchDocumentDO(libraryId, readablePermissionId, "产品说明", "普通正文", 1));
        documentMapper.insert(randomSearchDocumentDO(libraryId, readablePermissionId, "研发规范", "技术方案", 1));
        when(memberService.getReadableLibraryIdList(userId)).thenReturn(Collections.singletonList(libraryId));
        when(contentPermissionService.getReadableContentPermissionIdSet(
                Collections.singletonList(libraryId), userId))
                .thenReturn(Collections.singleton(readablePermissionId));
        // 准备参数
        PmsKnowledgeDocumentSearchPageReqVO pageReqVO = new PmsKnowledgeDocumentSearchPageReqVO();
        pageReqVO.setPageNo(1).setPageSize(10);

        // 调用
        PageResult<PmsKnowledgeDocumentDO> pageResult = documentService.getDocumentSearchPage(pageReqVO, userId);

        // 断言：空关键词按当前产品语义返回可读文档
        assertEquals(2, pageResult.getTotal());
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeLibraryDO randomLibraryDO() {
        return randomPojo(PmsKnowledgeLibraryDO.class, library -> library.setId(null).setName("测试知识库")
                .setDescription("测试").setOpenStatus(false));
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
                .setType(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType())
                .setStatus(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus()));
    }

    private PmsKnowledgeDocumentDO randomSearchDocumentDO(Long libraryId, Long permissionId, String title,
                                                          String content, Integer status) {
        return randomPojo(PmsKnowledgeDocumentDO.class, document -> document.setId(null).setLibraryId(libraryId)
                .setPermissionId(permissionId)
                .setFolderId(0L).setParentId(0L).setTitle(title).setContent(content)
                .setType(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType()).setStatus(status));
    }

}
