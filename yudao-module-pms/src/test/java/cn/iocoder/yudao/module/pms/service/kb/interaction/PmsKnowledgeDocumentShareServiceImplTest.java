package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentShareDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeDocumentShareMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMapper;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_SHARE_ALREADY_OPEN;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_SHARE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.MessageTemplateConstants.KNOWLEDGE_DOCUMENT_SHARED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeDocumentShareServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeDocumentShareServiceImpl.class)
public class PmsKnowledgeDocumentShareServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeDocumentShareServiceImpl documentShareService;

    @Resource
    private PmsKnowledgeDocumentShareMapper documentShareMapper;
    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;
    @Resource
    private PmsKnowledgeLibraryMapper libraryMapper;

    @MockBean
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @MockBean
    private NotifyMessageSendApi notifyMessageSendApi;
    @MockBean
    private AdminUserApi adminUserApi;

    @Test
    public void testOpenShare_duplicateAndReopen() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId());
        documentMapper.insert(document);
        when(contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId)).thenReturn(2);

        // 调用开启分享，并断言重复开启异常
        PmsKnowledgeDocumentShareDO firstShare = documentShareService.openShare(document.getId(),
                Arrays.asList(userId, userId), userId);
        assertEquals(Collections.singletonList(userId), firstShare.getShareUserIds());
        assertServiceException(() -> documentShareService.openShare(document.getId(), null, userId),
                KNOWLEDGE_DOCUMENT_SHARE_ALREADY_OPEN);

        // 调用关闭和重新开启，并断言令牌已更新
        documentShareService.closeShare(document.getId(), userId);
        PmsKnowledgeDocumentShareDO reopenedShare = documentShareService.openShare(document.getId(), null, userId);
        assertEquals(firstShare.getId(), reopenedShare.getId());
        assertNotEquals(firstShare.getToken(), reopenedShare.getToken());
        assertNull(reopenedShare.getCloseTime());
    }

    @Test
    public void testUpdateShareMembers() {
        // mock 数据
        Long userId = randomLongId();
        Long memberUserId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId());
        documentMapper.insert(document);
        when(contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId)).thenReturn(2);
        documentShareService.openShare(document.getId(), null, userId);

        // 调用
        documentShareService.updateShareMemberList(document.getId(), Collections.singletonList(memberUserId), userId);

        // 断言
        assertEquals(Collections.singletonList(memberUserId),
                documentShareMapper.selectByDocumentId(document.getId()).getShareUserIds());
        ArgumentCaptor<NotifySendSingleToUserReqDTO> captor = ArgumentCaptor
                .forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(captor.capture());
        assertEquals(memberUserId, captor.getValue().getUserId());
        assertEquals(KNOWLEDGE_DOCUMENT_SHARED, captor.getValue().getTemplateCode());
        assertEquals(document.getTitle(), captor.getValue().getTemplateParams().get("documentTitle"));
        String shareToken = documentShareMapper.selectByDocumentId(document.getId()).getToken();
        assertEquals("/pms/kb/document/share/" + shareToken,
                captor.getValue().getTemplateParams().get("route"));
    }

    @Test
    public void testUpdateShareMembers_deduplicateAndValidateUsers() {
        // mock 数据
        Long userId = randomLongId();
        Long memberUserId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId());
        documentMapper.insert(document);
        when(contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId)).thenReturn(2);
        documentShareService.openShare(document.getId(), null, userId);

        // 调用
        documentShareService.updateShareMemberList(document.getId(),
                Arrays.asList(memberUserId, memberUserId), userId);

        // 断言：持久化和用户校验均使用去重后的名单
        List<Long> expectedUserIds = Collections.singletonList(memberUserId);
        assertEquals(expectedUserIds,
                documentShareMapper.selectByDocumentId(document.getId()).getShareUserIds());
        verify(adminUserApi).validateUserList(expectedUserIds);
    }

    @Test
    public void testGetDocumentByShareToken_invalidAfterRecycle() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId());
        documentMapper.insert(document);
        when(contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId)).thenReturn(2);
        PmsKnowledgeDocumentShareDO share = documentShareService.openShare(document.getId(), null, userId);
        documentMapper.updateById(new PmsKnowledgeDocumentDO().setId(document.getId()).setStatus(-1));

        // 调用并断言
        assertServiceException(() -> documentShareService.getDocumentByShareToken(share.getToken()),
                KNOWLEDGE_DOCUMENT_SHARE_INVALID);
    }

    @Test
    public void testGetActiveDocumentShare_requiresContentReadable() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId());
        documentMapper.insert(document);
        PmsKnowledgeDocumentShareDO share = new PmsKnowledgeDocumentShareDO().setDocumentId(document.getId())
                .setToken("share-token").setStatus(CommonStatusEnum.ENABLE.getStatus());
        documentShareMapper.insert(share);
        when(contentPermissionService.validateContentPermissionReadable(
                document.getPermissionId(), document.getLibraryId(), userId)).thenReturn(1);

        // 调用
        PmsKnowledgeDocumentShareDO result = documentShareService.getActiveDocumentShare(document.getId(), userId);

        // 断言
        assertEquals(share.getId(), result.getId());
        verify(contentPermissionService).validateContentPermissionReadable(
                document.getPermissionId(), document.getLibraryId(), userId);
    }

    @Test
    public void testDeleteSharesByDocumentIds() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO library = randomLibraryDO();
        libraryMapper.insert(library);
        PmsKnowledgeDocumentDO document = randomDocumentDO(library.getId());
        documentMapper.insert(document);
        when(contentPermissionService.validateContentPermissionWritable(
                document.getPermissionId(), document.getLibraryId(), userId)).thenReturn(2);
        documentShareService.openShare(document.getId(), null, userId);

        // 调用
        documentShareService.deleteSharesByDocumentIds(Collections.singleton(document.getId()));

        // 断言
        assertNull(documentShareMapper.selectByDocumentId(document.getId()));
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeLibraryDO randomLibraryDO() {
        return randomPojo(PmsKnowledgeLibraryDO.class, library -> library.setId(null).setName("测试知识库")
                .setOpenStatus(true).setStatus(1));
    }

    private PmsKnowledgeDocumentDO randomDocumentDO(Long libraryId) {
        return randomPojo(PmsKnowledgeDocumentDO.class, document -> document.setId(null).setLibraryId(libraryId)
                .setPermissionId(randomLongId()).setFolderId(0L).setParentId(0L).setTitle("测试文档")
                .setType(3).setStatus(1));
    }

}
