package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderSaveReqVO;
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
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
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
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_FOLDER_MOVE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeFolderServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsKnowledgeFolderServiceImpl.class, PmsKnowledgeRecycleServiceImpl.class})
public class PmsKnowledgeFolderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeFolderServiceImpl folderService;

    @Resource
    private PmsKnowledgeLibraryMapper libraryMapper;
    @Resource
    private PmsKnowledgeLibraryMemberMapper memberMapper;
    @Resource
    private PmsKnowledgeFolderMapper folderMapper;
    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;

    @MockitoBean
    private PermissionApi permissionApi;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private PmsKnowledgeLibraryService libraryService;
    @MockitoBean
    private PmsKnowledgeLibraryMemberService memberService;
    @MockitoBean
    private PmsKnowledgeGroupService knowledgeGroupService;
    @MockitoBean
    private PmsKnowledgeDocumentShareService knowledgeDocumentShareService;
    @MockitoBean
    private PmsKnowledgeDocumentCommentService knowledgeDocumentCommentService;
    @MockitoBean
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @MockitoBean
    private PmsKnowledgeDocumentService documentService;
    @MockitoBean
    private PmsKnowledgeFavoriteService favoriteService;
    @MockitoBean
    private PmsKnowledgeDocumentLikeService documentLikeService;
    @MockitoBean
    private PmsKnowledgeViewRecordService viewRecordService;

    @Test
    public void testCreateFolder_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        when(contentPermissionService.createDefaultContentPermission(library.getId(), userId)).thenReturn(randomLongId());
        // 准备参数
        PmsKnowledgeFolderSaveReqVO reqVO = new PmsKnowledgeFolderSaveReqVO()
                .setLibraryId(library.getId()).setParentId(0L).setTitle("  产品文档  ");

        // 调用
        Long folderId = folderService.createFolder(reqVO, userId);

        // 断言
        PmsKnowledgeFolderDO folder = folderMapper.selectById(folderId);
        assertEquals(library.getId(), folder.getLibraryId());
        assertEquals("  产品文档  ", folder.getTitle());
        assertEquals(1, folder.getStatus());
    }

    @Test
    public void testMoveFolder_toDescendant() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeFolderDO parent = randomFolderDO(library.getId(), 0L);
        folderMapper.insert(parent);
        PmsKnowledgeFolderDO child = randomFolderDO(library.getId(), parent.getId());
        folderMapper.insert(child);
        // 准备参数
        PmsKnowledgeFolderMoveReqVO reqVO = new PmsKnowledgeFolderMoveReqVO().setId(parent.getId())
                .setTargetLibraryId(library.getId()).setTargetParentId(child.getId());

        // 调用，并断言异常
        assertServiceException(() -> folderService.moveFolder(reqVO, userId), KNOWLEDGE_FOLDER_MOVE_INVALID);
    }

    @Test
    public void testMoveFolder_crossLibraryUpdatesInteractionLibraryId() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO sourceLibrary = randomLibraryDO();
        libraryMapper.insert(sourceLibrary);
        PmsKnowledgeLibraryDO targetLibrary = randomLibraryDO();
        libraryMapper.insert(targetLibrary);
        when(memberService.validateLibraryReadable(sourceLibrary.getId(), userId)).thenReturn(sourceLibrary);
        when(memberService.validateLibraryReadable(targetLibrary.getId(), userId)).thenReturn(targetLibrary);
        when(memberService.validateLibraryWritable(targetLibrary.getId(), userId)).thenReturn(targetLibrary);
        PmsKnowledgeFolderDO parent = randomFolderDO(sourceLibrary.getId(), 0L);
        folderMapper.insert(parent);
        PmsKnowledgeFolderDO child = randomFolderDO(sourceLibrary.getId(), parent.getId());
        folderMapper.insert(child);
        PmsKnowledgeDocumentDO document = randomDocumentDO(sourceLibrary.getId(), child.getId(), 0L)
                .setId(randomLongId());
        when(documentService.getDocumentListByFolderIds(anyCollection())).thenReturn(Arrays.asList(document));
        Map<Long, Long> clonedPermissionIdMap = new HashMap<>();
        clonedPermissionIdMap.put(parent.getPermissionId(), randomLongId());
        clonedPermissionIdMap.put(child.getPermissionId(), randomLongId());
        clonedPermissionIdMap.put(document.getPermissionId(), randomLongId());
        when(contentPermissionService.cloneContentPermissions(anyCollection(), eq(targetLibrary.getId())))
                .thenReturn(clonedPermissionIdMap);
        // 准备参数
        PmsKnowledgeFolderMoveReqVO reqVO = new PmsKnowledgeFolderMoveReqVO().setId(parent.getId())
                .setTargetLibraryId(targetLibrary.getId()).setTargetParentId(0L);

        // 调用
        folderService.moveFolder(reqVO, userId);

        // 断言
        Set<Long> folderIds = new HashSet<>(Arrays.asList(parent.getId(), child.getId()));
        Set<Long> documentIds = Collections.singleton(document.getId());
        assertEquals(targetLibrary.getId(), folderMapper.selectById(parent.getId()).getLibraryId());
        assertEquals(targetLibrary.getId(), folderMapper.selectById(child.getId()).getLibraryId());
        verify(memberService).validateLibraryAdmin(targetLibrary.getId(), userId);
        verify(contentPermissionService).validateContentPermissionManageable(
                parent.getPermissionId(), sourceLibrary.getId(), userId);
        verify(favoriteService).updateFavoriteLibraryIdByEntityIds(folderIds, documentIds, targetLibrary.getId());
        verify(viewRecordService).updateViewRecordLibraryIdByEntityIds(folderIds, documentIds,
                targetLibrary.getId());
    }

    @Test
    public void testDeleteFolder_recursive() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        memberMapper.insert(randomMemberDO(library.getId(), userId));
        when(memberService.validateLibraryReadable(library.getId(), userId)).thenReturn(library);
        when(memberService.validateLibraryWritable(library.getId(), userId)).thenReturn(library);
        PmsKnowledgeFolderDO parentFolder = randomFolderDO(library.getId(), 0L);
        folderMapper.insert(parentFolder);
        PmsKnowledgeFolderDO childFolder = randomFolderDO(library.getId(), parentFolder.getId());
        folderMapper.insert(childFolder);
        PmsKnowledgeDocumentDO parentDocument = randomDocumentDO(library.getId(), childFolder.getId(), 0L);
        documentMapper.insert(parentDocument);
        PmsKnowledgeDocumentDO childDocument = randomDocumentDO(library.getId(), 0L, parentDocument.getId());
        documentMapper.insert(childDocument);
        when(documentService.getDocumentListByFolderIds(anyCollection()))
                .thenReturn(Arrays.asList(parentDocument, childDocument));
        doAnswer(invocation -> {
            List<PmsKnowledgeDocumentDO> documents = invocation.getArgument(0);
            documentMapper.updateBatch(documents);
            return null;
        }).when(documentService).updateDocumentList(anyCollection());

        // 调用
        folderService.deleteFolder(parentFolder.getId(), userId);

        // 断言
        assertEquals(-1, folderMapper.selectById(parentFolder.getId()).getStatus());
        assertEquals(-1, folderMapper.selectById(childFolder.getId()).getStatus());
        assertEquals(-1, documentMapper.selectById(parentDocument.getId()).getStatus());
        assertEquals(-1, documentMapper.selectById(childDocument.getId()).getStatus());
        verify(memberService).validateLibraryReadable(library.getId(), userId);
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

}
