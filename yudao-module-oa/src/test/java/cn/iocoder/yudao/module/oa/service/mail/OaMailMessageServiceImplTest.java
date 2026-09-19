package cn.iocoder.yudao.module.oa.service.mail;

import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.*;
import cn.iocoder.yudao.module.oa.dal.mysql.mail.*;
import jakarta.annotation.Resource;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.imap.AppendUID;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.MAIL_ACCOUNT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaMailMessageServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaMailMessageServiceImpl.class, OaMailFolderServiceImpl.class})
public class OaMailMessageServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaMailMessageServiceImpl mailMessageService;

    @Resource
    private OaMailMessageMapper mailMessageMapper;
    @Resource
    private OaMailFolderMapper mailFolderMapper;

    @MockitoBean
    private OaMailAccountService mailAccountService;
    @MockitoBean
    private OaMailMessageClient mailMessageClient;

    @Test
    public void testRestoreMailMessage_failureKeepsIndex() throws Exception {
        // mock 数据
        OaMailFolderDO trash = randomMailFolderDO().setType("TRASH").setName("Trash");
        mailFolderMapper.insert(trash);
        OaMailMessageDO index = randomMailMessageDO(trash.getId(), 1L);
        mailMessageMapper.insert(index);
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message message = mock(Message.class);
        // mock 方法
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(mailMessageClient.getFolders(store)).thenReturn(Collections.singletonMap("INBOX", "INBOX"));
        when(store.getFolder("Trash")).thenReturn(folder);
        when(folder.isOpen()).thenReturn(true);
        when(mailMessageClient.getMessage(eq(folder), any())).thenReturn(message);
        doThrow(new MessagingException("连接中断")).when(mailMessageClient).moveMessage(store, folder, message, "INBOX");

        // 调用，并断言：失败保留本地索引，供同步核实
        assertThrows(ServiceException.class,
                () -> mailMessageService.restoreMailMessage(index.getId(), 1L));
        assertNotNull(mailMessageMapper.selectById(index.getId()));
        verify(folder).close(false);
        verify(folder, never()).close(true);
    }

    @Test
    public void testRestoreMailMessage_movesToInbox() throws Exception {
        // mock 数据
        OaMailFolderDO trash = randomMailFolderDO().setType("TRASH").setName("Trash");
        mailFolderMapper.insert(trash);
        OaMailMessageDO index = randomMailMessageDO(trash.getId(), 1L);
        mailMessageMapper.insert(index);
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message message = mock(Message.class);
        // mock 方法
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(mailMessageClient.getFolders(store)).thenReturn(Collections.singletonMap("INBOX", "INBOX"));
        when(store.getFolder("Trash")).thenReturn(folder);
        when(folder.isOpen()).thenReturn(true);
        when(mailMessageClient.getMessage(eq(folder), any())).thenReturn(message);

        // 调用
        mailMessageService.restoreMailMessage(index.getId(), 1L);
        // 断言
        verify(mailMessageClient).moveMessage(store, folder, message, "INBOX");
        assertNull(mailMessageMapper.selectById(index.getId()));
        verify(folder).close(false);
        verify(folder, never()).close(true);
    }

    @Test
    public void testGetMailMessageAttachment_ownership() {
        // mock 数据
        OaMailFolderDO folder = randomMailFolderDO();
        mailFolderMapper.insert(folder);
        OaMailMessageDO message = randomMailMessageDO(folder.getId(), 1L);
        mailMessageMapper.insert(message);
        // mock 方法：账号服务执行本人归属校验
        when(mailAccountService.validateMailAccount(1L, 2L)).thenThrow(
                exception(
                        MAIL_ACCOUNT_NOT_EXISTS));

        // 调用，并断言：他人不能连接该邮箱下载附件
        assertServiceException(
                () -> mailMessageService.getMailMessageAttachment(message.getId(), "1", 2L),
                MAIL_ACCOUNT_NOT_EXISTS);
        verifyNoInteractions(mailMessageClient);
    }

    @Test
    public void testGetMailMessageAttachment_readOnlyAndClosed() throws Exception {
        // mock 数据
        OaMailFolderDO localFolder = randomMailFolderDO();
        mailFolderMapper.insert(localFolder);
        OaMailMessageDO message = randomMailMessageDO(localFolder.getId(), 1L);
        mailMessageMapper.insert(message);
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder remoteFolder = mock(Folder.class);
        Message remoteMessage = mock(Message.class);
        // mock 方法
        when(mailAccountService.validateMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(store.getFolder("INBOX")).thenReturn(remoteFolder);
        when(mailMessageClient.getMessage(eq(remoteFolder), any())).thenReturn(remoteMessage);
        when(mailMessageClient.getAttachment(remoteMessage, "1"))
                .thenReturn(Pair.of("合同.txt", new byte[]{1, 2}));

        // 调用
        Pair<String, byte[]> result = mailMessageService.getMailMessageAttachment(message.getId(), "1", 1L);
        // 断言
        assertArrayEquals(new byte[]{1, 2}, result.getValue());
        verify(remoteFolder).open(Folder.READ_ONLY);
        verify(remoteFolder).close();
        verify(store).close();
    }

    @Test
    public void testSyncMailMessages_updateAndRemove() throws Exception {
        // mock 数据
        OaMailFolderDO localFolder = randomMailFolderDO();
        mailFolderMapper.insert(localFolder);
        OaMailMessageDO first = randomMailMessageDO(localFolder.getId(), 10L);
        OaMailMessageDO removed = randomMailMessageDO(localFolder.getId(), 11L);
        mailMessageMapper.insert(first);
        mailMessageMapper.insert(removed);
        // mock 方法
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class, withSettings().extraInterfaces(UIDFolder.class));
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(mailMessageClient.getFolders(store)).thenReturn(Collections.singletonMap("INBOX", "INBOX"));
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(((UIDFolder) folder).getUIDValidity()).thenReturn(100L);
        when(folder.isOpen()).thenReturn(true);
        when(mailMessageClient.getMessageList(folder)).thenReturn(Arrays.asList(
                randomMailMessageDO(null, 10L).setId(null).setReadStatus(true), randomMailMessageDO(null, 12L).setId(null)));

        // 调用
        assertEquals(2, mailMessageService.syncMailMessageList(1L, 1L));
        // 断言
        assertTrue(mailMessageMapper.selectById(first.getId()).getReadStatus());
        assertNull(mailMessageMapper.selectById(removed.getId()));
        assertEquals(2, mailMessageMapper.selectListByFolderId(localFolder.getId()).size());
        assertNotNull(mailFolderMapper.selectById(localFolder.getId()).getSyncTime());
        verify(folder).close(false);
        verify(store).close();
    }

    @Test
    public void testSyncMailMessages_uidValidityReset() throws Exception {
        // mock 数据
        OaMailFolderDO localFolder = randomMailFolderDO();
        mailFolderMapper.insert(localFolder);
        OaMailMessageDO old = randomMailMessageDO(localFolder.getId(), 1L);
        mailMessageMapper.insert(old);
        // mock 方法
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class, withSettings().extraInterfaces(UIDFolder.class));
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(mailMessageClient.getFolders(store)).thenReturn(Collections.singletonMap("INBOX", "INBOX"));
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(((UIDFolder) folder).getUIDValidity()).thenReturn(200L);
        when(mailMessageClient.getMessageList(folder)).thenReturn(Collections.singletonList(randomMailMessageDO(null, 1L)
                .setId(null).setUidValidity(200L)));

        // 调用
        mailMessageService.syncMailMessageList(1L, 1L);
        // 断言：UID 相同但有效期改变，不能复用旧详情编号
        assertNull(mailMessageMapper.selectById(old.getId()));
        assertEquals(200L, mailFolderMapper.selectById(localFolder.getId()).getUidValidity());
        assertEquals(1, mailMessageMapper.selectListByFolderId(localFolder.getId()).size());
    }

    @Test
    public void testGetMailMessagePage_filterAndScope() {
        // mock 数据
        OaMailFolderDO folder = randomMailFolderDO();
        mailFolderMapper.insert(folder);
        mailMessageMapper.insert(randomMailMessageDO(folder.getId(), 1L).setSubject("验收邮件").setReadStatus(false));
        mailMessageMapper.insert(randomMailMessageDO(folder.getId(), 2L).setSubject("验收邮件").setReadStatus(true));
        mailMessageMapper.insert(randomMailMessageDO(folder.getId(), 3L).setAccountId(2L).setSubject("验收邮件"));
        // 准备参数
        OaMailMessagePageReqVO reqVO = new OaMailMessagePageReqVO().setAccountId(1L).setFolderKey("UNREAD").setKeyword("验收");

        // 调用
        PageResult<OaMailMessageDO> page = mailMessageService.getMailMessagePage(reqVO, 1L);
        // 断言
        assertEquals(1L, page.getTotal());
        assertFalse(page.getList().get(0).getReadStatus());
        verify(mailAccountService).validateMailAccount(1L, 1L);
        assertEquals(2L, mailMessageService.getMailMessagePage(reqVO.setFolderKey("INBOX"), 1L).getTotal());
    }

    @Test
    public void testGetMailFolderList_dynamicAndCounts() {
        // mock 数据
        OaMailFolderDO inbox = randomMailFolderDO();
        mailFolderMapper.insert(inbox);
        OaMailFolderDO custom = randomMailFolderDO().setName("客户/合同").setType("CUSTOM");
        mailFolderMapper.insert(custom);
        mailFolderMapper.insert(randomMailFolderDO().setName("已移除").setType("CUSTOM").setAvailable(false));
        mailFolderMapper.insert(randomMailFolderDO().setName("他人目录").setType("CUSTOM").setAccountId(2L));
        mailMessageMapper.insert(randomMailMessageDO(inbox.getId(), 1L));
        mailMessageMapper.insert(randomMailMessageDO(custom.getId(), 2L));
        mailMessageMapper.insert(randomMailMessageDO(custom.getId(), 3L).setReadStatus(true));

        // 调用
        List<OaMailFolderRespVO> folders = mailMessageService.getMailFolderList(1L, 1L);
        // 断言
        assertEquals(3, folders.size());
        assertEquals("INBOX", folders.get(0).getKey());
        assertEquals("UNREAD", folders.get(1).getKey());
        assertEquals("客户/合同", folders.get(2).getName());
        assertEquals(1L, folders.get(2).getUnreadCount());
        OaMailMessagePageReqVO request = new OaMailMessagePageReqVO()
                .setAccountId(1L).setFolderKey(folders.get(2).getKey());
        assertEquals(2L, mailMessageService.getMailMessagePage(request, 1L).getTotal());
        assertEquals(0L, mailMessageService.getMailMessagePage(request.setAccountId(2L), 2L).getTotal());
    }

    @Test
    public void testGetMailMessage_cacheAndUpdate() throws Exception {
        // mock 数据
        OaMailFolderDO folder = randomMailFolderDO();
        mailFolderMapper.insert(folder);
        OaMailMessageDO index = randomMailMessageDO(folder.getId(), 1L);
        mailMessageMapper.insert(index);
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder remote = mock(Folder.class);
        OaMailMessageDO detail = new OaMailMessageDO();
        detail.setContent("<p>正文</p>");
        detail.setReplyTos(Arrays.asList("reply@example.com"));
        detail.setAttachments(Collections.singletonList(new OaMailMessageDO.Attachment()
                .setPart("1").setName("合同.pdf").setSize(123)));
        // mock 方法
        when(mailAccountService.validateMailAccount(1L, 1L)).thenReturn(account);
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(store.getFolder("INBOX")).thenReturn(remote);
        when(mailMessageClient.getDetail(any(), any())).thenReturn(detail);
        when(mailMessageClient.sanitizeHtml(anyString())).thenAnswer(invocation -> new OaMailMessageClientImpl().sanitizeHtml(invocation.getArgument(0)));

        // 调用：首次远端读取，第二次从数据库读取
        mailMessageService.getMailMessage(index.getId(), 1L);
        OaMailMessageDO cached = mailMessageService.getMailMessage(index.getId(), 1L);
        // 断言
        verify(mailMessageClient, times(1)).openStore(account);
        assertTrue(cached.getContent().contains("正文"));
        assertEquals(Collections.singletonList("reply@example.com"), cached.getReplyTos());
        assertEquals("合同.pdf", cached.getAttachments().get(0).getName());
        assertTrue(mailMessageMapper.selectById(index.getId()).getContent().contains("正文"));
        assertTrue(mailMessageService.getMailMessagePage(new OaMailMessagePageReqVO()
                .setAccountId(1L).setFolderKey("INBOX"), 1L).getList().get(0).getContent().contains("正文"));
        mailMessageMapper.updateById(new OaMailMessageDO().setId(index.getId()).setSubject("同步更新"));
        assertTrue(mailMessageMapper.selectById(index.getId()).getContent().contains("正文"));
    }

    @Test
    public void testGetMailMessage_emptyCacheAndOwnership() {
        // mock 数据
        OaMailFolderDO folder = randomMailFolderDO();
        mailFolderMapper.insert(folder);
        OaMailMessageDO index = randomMailMessageDO(folder.getId(), 1L).setContent("");
        mailMessageMapper.insert(index);

        // 调用，并断言
        assertEquals("", mailMessageService.getMailMessage(index.getId(), 1L).getContent());
        verifyNoInteractions(mailMessageClient);
        when(mailAccountService.validateMailAccount(1L, 2L)).thenThrow(new IllegalArgumentException("无权访问"));
        assertThrows(IllegalArgumentException.class, () -> mailMessageService.getMailMessage(index.getId(), 2L));
    }

    @Test
    public void testGetMailMessage_notOwner() {
        // mock 数据
        OaMailMessageDO index = randomMailMessageDO(1L, 1L);
        mailMessageMapper.insert(index);
        // mock 方法
        when(mailAccountService.validateMailAccount(1L, 2L)).thenThrow(new IllegalArgumentException("无权访问"));

        // 调用，并断言
        assertThrows(IllegalArgumentException.class, () -> mailMessageService.getMailMessage(index.getId(), 2L));
        verifyNoInteractions(mailMessageClient);
    }

    @Test
    public void testUpdateMailMessageRead_noExpunge() throws Exception {
        // mock 数据
        OaMailFolderDO localFolder = randomMailFolderDO();
        mailFolderMapper.insert(localFolder);
        OaMailMessageDO index = randomMailMessageDO(localFolder.getId(), 1L);
        mailMessageMapper.insert(index);
        // mock 方法
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message remote = mock(Message.class);
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(folder.isOpen()).thenReturn(true);
        when(mailMessageClient.getMessage(eq(folder), any())).thenReturn(remote);

        // 调用
        mailMessageService.updateMailMessageRead(index.getId(), true, 1L);
        // 断言
        verify(remote).setFlag(Flags.Flag.SEEN, true);
        assertTrue(mailMessageMapper.selectById(index.getId()).getReadStatus());
        verify(folder).close(false);
        verify(folder, never()).close(true);
    }

    @Test
    public void testSendMailMessage_sentCopyFailureNotResend() throws Exception {
        // 准备参数
        OaMailMessageSaveReqVO reqVO = new OaMailMessageSaveReqVO().setAccountId(1L).setMode("new");
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        MimeMessage mail = new MimeMessage(Session.getInstance(new Properties()));
        // mock 方法
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.createMessage(account, reqVO, true)).thenReturn(mail);
        when(mailMessageClient.openStore(account)).thenThrow(new MessagingException("IMAP unavailable"));

        // 调用
        String result = mailMessageService.sendMailMessage(reqVO, 1L);
        // 断言
        assertTrue(result.contains("服务器已接收"));
        assertTrue(result.contains("勿重发"));
        verify(mailMessageClient, times(1)).sendMessage(account, mail);
    }

    @ParameterizedTest
    @CsvSource({"主题, Re: 主题", "Re: 主题, Re: 主题", "re: 主题, re: 主题", "RE: 主题, RE: 主题"})
    public void testGetMailMessageCompose_replyAll(String subject, String expectedSubject) throws Exception {
        // mock 数据
        OaMailFolderDO localFolder = randomMailFolderDO();
        mailFolderMapper.insert(localFolder);
        OaMailMessageDO index = randomMailMessageDO(localFolder.getId(), 1L);
        mailMessageMapper.insert(index);
        OaMailAccountDO account = new OaMailAccountDO().setId(1L).setMail("self@example.com");
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        when(mailAccountService.validateMailAccount(1L, 1L)).thenReturn(account);
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(store.getFolder("INBOX")).thenReturn(folder);
        OaMailMessageDO detail = new OaMailMessageDO();
        detail.setAccountId(1L).setFolderId(localFolder.getId()).setSender("sender@example.com")
                .setRecipients(Arrays.asList("self@example.com", "other@example.com"))
                .setCcs(Arrays.asList("other@example.com", "cc@example.com")).setSubject(subject);
        detail.setReplyTos(Arrays.asList("reply@example.com"));
        detail.setContent("<style>body{display:none}</style><p style='background:url(https://tracking.invalid)'>正文</p>");
        when(mailMessageClient.getDetail(eq(folder), any())).thenReturn(detail);
        when(mailMessageClient.sanitizeHtml(anyString())).thenAnswer(invocation -> new OaMailMessageClientImpl().sanitizeHtml(invocation.getArgument(0)));
        when(mailMessageClient.parseAddresses(anyList())).thenAnswer(invocation -> new OaMailMessageClientImpl().parseAddresses(invocation.getArgument(0)));

        // 调用
        OaMailMessageSaveReqVO result = mailMessageService.getMailMessageCompose(index.getId(), "replyAll", 1L);
        // 断言
        assertEquals(expectedSubject, result.getSubject());
        assertEquals(Arrays.asList("reply@example.com", "other@example.com"), result.getRecipients());
        assertEquals(Collections.singletonList("cc@example.com"), result.getCcs());
        assertFalse(result.getContent().contains("style"));
        assertFalse(result.getContent().contains("tracking.invalid"));
    }

    @Test
    public void testGetMailMessageCompose_draftKeepCollapseImageAndLink() throws Exception {
        // mock 数据
        OaMailFolderDO localFolder = randomMailFolderDO().setType("DRAFTS");
        mailFolderMapper.insert(localFolder);
        OaMailMessageDO index = randomMailMessageDO(localFolder.getId(), 1L);
        mailMessageMapper.insert(index);
        OaMailAccountDO account = new OaMailAccountDO().setId(1L).setMail("self@example.com");
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        OaMailMessageDO detail = new OaMailMessageDO().setAccountId(1L).setFolderId(localFolder.getId()).setSubject("草稿")
                .setContent("<details ontoggle='alert(1)'><summary>详细说明</summary>"
                        + "<p>折叠正文</p></details><img src='https://example.com/image.png' onerror='alert(1)'>"
                        + "<a href='https://example.com/article'>原文</a><a href='javascript:alert(1)'>危险链接</a>");
        // mock 方法
        when(mailAccountService.validateMailAccount(1L, 1L)).thenReturn(account);
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(mailMessageClient.getDetail(eq(folder), any())).thenReturn(detail);
        when(mailMessageClient.sanitizeHtml(anyString())).thenAnswer(
                invocation -> new OaMailMessageClientImpl().sanitizeHtml(invocation.getArgument(0)));

        // 调用
        OaMailMessageSaveReqVO result = mailMessageService.getMailMessageCompose(index.getId(), "draft", 1L);
        // 断言：编辑草稿不把折叠结构降级成普通文本
        assertTrue(result.getContent().contains("<details>"));
        assertTrue(result.getContent().contains("<summary>详细说明</summary>"));
        assertTrue(result.getContent().contains("折叠正文"));
        assertFalse(result.getContent().contains("ontoggle"));
        assertTrue(result.getContent().contains("https://example.com/image.png"));
        assertTrue(result.getContent().contains("href=\"https://example.com/article\""));
        assertFalse(result.getContent().contains("onerror"));
        assertFalse(result.getContent().contains("javascript:"));
    }

    @Test
    public void testSaveMailMessageDraft_appendAndSync() throws Exception {
        // mock 数据
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        OaMailMessageSaveReqVO reqVO = new OaMailMessageSaveReqVO().setAccountId(1L).setMode("new");
        IMAPStore store = mock(IMAPStore.class);
        IMAPFolder folder = mock(IMAPFolder.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        // mock 方法
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(mailMessageClient.getFolders(store)).thenReturn(Collections.singletonMap("DRAFTS", "Drafts"));
        when(store.hasCapability("UIDPLUS")).thenReturn(true);
        when(store.getFolder("Drafts")).thenReturn(folder);
        when(mailMessageClient.createMessage(account, reqVO, false)).thenReturn(message);
        when(folder.appendUIDMessages(any())).thenReturn(new AppendUID[]{
                new AppendUID(100L, 20L)});
        when(folder.getUIDValidity()).thenReturn(100L);
        when(folder.isOpen()).thenReturn(true);
        when(mailMessageClient.getMessageList(folder)).thenReturn(Collections.singletonList(randomMailMessageDO(null, 20L)
                .setId(null)));

        // 调用
        Long id = mailMessageService.saveMailMessageDraft(reqVO, 1L);
        // 断言
        assertEquals(20L, mailMessageMapper.selectById(id).getUid());
        assertTrue(message.isSet(Flags.Flag.DRAFT));
        verify(folder, never()).close(true);
        verify(mailMessageClient, never()).sendMessage(any(), any());
    }

    @Test
    public void testSyncMailMessages_failureRetainsSnapshot() throws Exception {
        // mock 数据
        OaMailFolderDO localFolder = randomMailFolderDO();
        mailFolderMapper.insert(localFolder);
        OaMailMessageDO index = randomMailMessageDO(localFolder.getId(), 1L);
        mailMessageMapper.insert(index);
        OaMailAccountDO account = new OaMailAccountDO().setId(1L);
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        // mock 方法
        when(mailAccountService.lockMailAccount(1L, 1L)).thenReturn(account);
        when(mailMessageClient.openStore(account)).thenReturn(store);
        when(mailMessageClient.getFolders(store)).thenReturn(Collections.singletonMap("INBOX", "INBOX"));
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(mailMessageClient.getMessageList(folder)).thenThrow(new MessagingException("连接中断"));

        // 调用，并断言
        assertThrows(ServiceException.class, () -> mailMessageService.syncMailMessageList(1L, 1L));
        assertNotNull(mailMessageMapper.selectById(index.getId()));
        assertNull(mailFolderMapper.selectById(localFolder.getId()).getSyncTime());
    }

    // ========== 随机对象 ==========

    /**
     * 构造可用且尚未同步的收件箱。
     *
     * @return 未入库的测试对象
     */
    private static OaMailFolderDO randomMailFolderDO() {
        return randomPojo(OaMailFolderDO.class, folder -> {
            folder.setAccountId(1L).setName("INBOX").setType("INBOX").setUidValidity(100L).setAvailable(true);
            folder.setSyncTime(null);
        });
    }

    /**
     * 构造未读且尚未缓存正文的邮件索引。
     *
     * @param folderId 邮箱目录编号
     * @param uid 服务端邮件 UID
     * @return 未入库的测试对象
     */
    private static OaMailMessageDO randomMailMessageDO(Long folderId, Long uid) {
        return randomPojo(OaMailMessageDO.class, message -> {
            message.setContent(null).setReplyTos(null).setAttachments(null);
            message.setAccountId(1L).setFolderId(folderId).setUid(uid).setUidValidity(100L)
                    .setReadStatus(false).setHasAttach(false).setSize(100).setReceiveTime(LocalDateTime.of(2026, 9, 8, 10, 0));
        });
    }
}
