package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_INTERACTION_OBJECT_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeInteractionTargetServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class PmsKnowledgeInteractionTargetServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PmsKnowledgeInteractionTargetServiceImpl interactionTargetService;

    @Mock
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Mock
    private PmsKnowledgeLibraryService libraryService;
    @Mock
    private PmsKnowledgeFolderReadService folderReadService;
    @Mock
    private PmsKnowledgeDocumentService documentService;
    @Mock
    private PmsKnowledgeContentPermissionService contentPermissionService;

    @Test
    public void testGetReadableItemList_filtersContentPermission() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        Long readablePermissionId = randomLongId();
        Long unreadablePermissionId = randomLongId();
        PmsKnowledgeLibraryDO library = new PmsKnowledgeLibraryDO().setId(libraryId)
                .setName("产品知识库").setStatus(1);
        PmsKnowledgeFolderDO readableFolder = new PmsKnowledgeFolderDO().setId(randomLongId())
                .setLibraryId(libraryId).setPermissionId(readablePermissionId).setTitle("可读文件夹").setStatus(1);
        PmsKnowledgeFolderDO unreadableFolder = new PmsKnowledgeFolderDO().setId(randomLongId())
                .setLibraryId(libraryId).setPermissionId(unreadablePermissionId).setTitle("不可读文件夹").setStatus(1);
        when(libraryMemberService.getReadableLibraryIdList(userId)).thenReturn(Collections.singletonList(libraryId));
        when(libraryService.getLibraryList(Collections.singleton(libraryId)))
                .thenReturn(Collections.singletonList(library));
        when(folderReadService.getFolderList(anyCollection()))
                .thenReturn(Arrays.asList(readableFolder, unreadableFolder));
        when(documentService.getDocumentList(anyCollection())).thenReturn(Collections.emptyList());
        when(contentPermissionService.getReadableContentPermissionIdSet(Collections.singleton(libraryId), userId))
                .thenReturn(Collections.singleton(readablePermissionId));
        List<PmsKnowledgeInteractionItemRespVO> targets = Arrays.asList(
                new PmsKnowledgeInteractionItemRespVO().setId(randomLongId())
                        .setType(PmsKnowledgeObjectTypeEnum.FOLDER.getType())
                        .setEntityId(readableFolder.getId()).setLibraryId(libraryId),
                new PmsKnowledgeInteractionItemRespVO().setId(randomLongId())
                        .setType(PmsKnowledgeObjectTypeEnum.FOLDER.getType())
                        .setEntityId(unreadableFolder.getId()).setLibraryId(libraryId));

        // 调用
        List<PmsKnowledgeInteractionItemRespVO> items = interactionTargetService.getReadableItemList(targets, userId);

        // 断言
        assertEquals(1, items.size());
        assertEquals(readableFolder.getId(), CollUtil.getFirst(items).getEntityId());
    }

    @Test
    public void testValidateTargetReadable_documentTypeMismatch() {
        // mock 数据
        Long userId = randomLongId();
        Long documentId = randomLongId();
        when(documentService.getDocument(documentId, userId)).thenReturn(new PmsKnowledgeDocumentDO()
                .setId(documentId).setLibraryId(randomLongId()).setType(PmsKnowledgeObjectTypeEnum.FILE.getType()));

        // 调用，并断言
        assertServiceException(() -> interactionTargetService.validateTargetReadable(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId, userId),
                KNOWLEDGE_INTERACTION_OBJECT_INVALID);
    }

}
