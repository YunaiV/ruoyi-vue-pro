package cn.iocoder.yudao.module.oa.service.mail;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.*;
import cn.iocoder.yudao.module.oa.dal.mysql.mail.*;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailCapabilityEnum;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailComposeModeEnum;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailFolderKeyEnum;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailFolderTypeEnum;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.HeaderTerm;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 企业邮箱邮件 Service 实现类
 *
 * 本地保存邮件索引；同步与写操作按账号加锁。远端操作不可回滚，失败后通过再次同步恢复索引。
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class OaMailMessageServiceImpl implements OaMailMessageService {

    private static final Map<String, String> STANDARD_FOLDER_NAMES = new LinkedHashMap<>();
    private static final List<String> STANDARD_FOLDER_TYPES;

    static {
        for (OaMailFolderTypeEnum type : OaMailFolderTypeEnum.values()) {
            if (type != OaMailFolderTypeEnum.CUSTOM) {
                STANDARD_FOLDER_NAMES.put(type.getType(), type.getName());
            }
        }
        STANDARD_FOLDER_TYPES = new ArrayList<>(STANDARD_FOLDER_NAMES.keySet());
    }

    @Resource
    private OaMailMessageMapper mailMessageMapper;

    @Resource
    private OaMailAccountService mailAccountService;
    @Resource
    private OaMailFolderService mailFolderService;

    @Resource
    private OaMailMessageClient mailMessageClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaMailMessageSaveReqVO getMailMessageCompose(Long id, String mode, Long userId) {
        // 1. 校验模式与归属并读取原邮件
        OaMailMessageDO source = getMailMessage(id, userId);
        // 编辑器处于主页面中，引用内容额外移除邮件 CSS，避免样式泄漏和外链资源请求。
        String editableContent = getEditableContent(source.getContent());
        OaMailAccountDO account = mailAccountService.validateMailAccount(source.getAccountId(), userId);
        OaMailMessageSaveReqVO result = new OaMailMessageSaveReqVO().setAccountId(source.getAccountId())
                .setMode(OaMailComposeModeEnum.DRAFT.getValue().equals(mode) ? OaMailComposeModeEnum.NEW.getValue() : mode);
        if (StrUtil.equalsAny(mode, OaMailComposeModeEnum.DRAFT.getValue(), OaMailComposeModeEnum.FORWARD.getValue())) {
            result.setAttachments(source.getAttachments()).setAttachmentParts(
                    convertList(source.getAttachments(), OaMailMessageDO.Attachment::getPart));
        }
        if (OaMailComposeModeEnum.DRAFT.getValue().equals(mode)) {
            if (ObjUtil.notEqual(OaMailFolderTypeEnum.DRAFTS.getType(), validateFolder(source).getType())) {
                throw exception(MAIL_NOT_DRAFT);
            }
            return result.setDraftId(id).setRecipients(source.getRecipients()).setCcs(source.getCcs())
                    .setSubject(source.getSubject()).setContent(editableContent);
        }

        // 2. 回复地址使用 Reply-To；回复全部排除当前邮箱并去重
        result.setSourceId(id).setSubject(source.getSubject());
        if (OaMailComposeModeEnum.FORWARD.getValue().equals(mode)) {
            result.setSubject("Fwd: " + source.getSubject());
        } else if (!StrUtil.startWithIgnoreCase(source.getSubject(), "Re:")) {
            result.setSubject("Re: " + source.getSubject());
        }
        try {
            Map<String, InternetAddress> recipients = new LinkedHashMap<>();
            Map<String, InternetAddress> cc = new LinkedHashMap<>();
            if (ObjUtil.notEqual(OaMailComposeModeEnum.FORWARD.getValue(), mode)) {
                addReplyAddresses(recipients, source.getReplyTos(), account.getMail());
                if (MapUtil.isEmpty(recipients)) {
                    addReplyAddresses(recipients, Collections.singletonList(source.getSender()), account.getMail());
                }
            }
            if (OaMailComposeModeEnum.REPLY_ALL.getValue().equals(mode)) {
                addReplyAddresses(recipients, source.getRecipients(), account.getMail());
                addReplyAddresses(cc, source.getCcs(), account.getMail());
                recipients.keySet().forEach(cc::remove);
            }
            result.setRecipients(convertList(recipients.values(), InternetAddress::toUnicodeString))
                    .setCcs(convertList(cc.values(), InternetAddress::toUnicodeString));
        } catch (MessagingException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_ADDRESS_INVALID);
        }

        // 3. 引用正文保留安全 HTML，原始邮件头以文本转义
        return result.setContent("<p><br></p><blockquote><p>发件人：" + Entities.escape(source.getSender())
                + "</p>" + editableContent + "</blockquote>");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncMailMessageList(Long accountId, Long userId) {
        // 1. 校验归属并锁定账号，避免多个同步请求覆盖更新结果
        OaMailAccountDO account = mailAccountService.lockMailAccount(accountId, userId);

        // 2. 获取标准文件夹映射，完整成功后才提交本地快照
        try (Store store = mailMessageClient.openStore(account)) {
            Map<String, String> remoteFolders = mailMessageClient.getFolders(store);
            int total = 0;
            for (Map.Entry<String, String> entry : remoteFolders.entrySet()) {
                total += syncFolder(store, accountId,
                        entry.getKey().startsWith("CUSTOM:") ? OaMailFolderTypeEnum.CUSTOM.getType() : entry.getKey(), entry.getValue());
            }
            // 3. 不再存在的远端文件夹标记不可用，保留旧索引但不再展示
            for (OaMailFolderDO folder : mailFolderService.getMailFolderList(accountId)) {
                if (!remoteFolders.containsValue(folder.getName())) {
                    mailFolderService.updateMailFolder(new OaMailFolderDO().setId(folder.getId()).setAvailable(false));
                }
            }
            return total;
        } catch (MessagingException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_SYNC_FAILED);
        }
    }

    @Override
    public List<OaMailFolderRespVO> getMailFolderList(Long accountId, Long userId) {
        // 1. 校验账号归属，批量查询目录及未读数量
        mailAccountService.validateMailAccount(accountId, userId);
        List<OaMailFolderDO> folders = mailFolderService.getMailFolderList(accountId);
        Map<Long, Long> counts = mailMessageMapper.selectCountMapByAccountIdAndReadStatus(accountId, false);

        // 2. 标准目录统一中文展示，自建目录保留完整名称；未读是虚拟入口
        Map<String, String> names = STANDARD_FOLDER_NAMES;
        List<String> types = STANDARD_FOLDER_TYPES;
        folders.sort(Comparator.comparingInt((OaMailFolderDO f) ->
                types.contains(f.getType()) ? types.indexOf(f.getType()) : types.size())
                .thenComparing(OaMailFolderDO::getName));
        List<OaMailFolderRespVO> result = new ArrayList<>();
        for (OaMailFolderDO folder : folders) {
            if (Boolean.FALSE.equals(folder.getAvailable())) {
                continue;
            }
            Long unreadCount = counts.getOrDefault(folder.getId(), 0L);
            result.add(new OaMailFolderRespVO()
                    .setKey(names.containsKey(folder.getType()) ? folder.getType() : folder.getId().toString())
                    .setName(names.getOrDefault(folder.getType(), folder.getName())).setUnreadCount(unreadCount));
            if (OaMailFolderTypeEnum.INBOX.getType().equals(folder.getType())) {
                result.add(new OaMailFolderRespVO().setKey(OaMailFolderKeyEnum.UNREAD.getKey()).setName("未读邮件").setUnreadCount(unreadCount));
            }
        }
        return result;
    }

    @Override
    public PageResult<OaMailMessageDO> getMailMessagePage(OaMailMessagePageReqVO reqVO, Long userId) {
        // 1. 校验本人归属，未读入口固定在收件箱内筛选
        mailAccountService.validateMailAccount(reqVO.getAccountId(), userId);
        String type = OaMailFolderKeyEnum.UNREAD.getKey().equals(reqVO.getFolderKey()) ? OaMailFolderTypeEnum.INBOX.getType() : reqVO.getFolderKey();
        OaMailFolderDO folder;
        if (StrUtil.isNumeric(type)) {
            folder = mailFolderService.getMailFolder(Long.valueOf(type));
            if (folder != null && (ObjUtil.notEqual(folder.getAccountId(), reqVO.getAccountId())
                    || Boolean.FALSE.equals(folder.getAvailable()))) {
                folder = null;
            }
        } else {
            folder = getFolder(reqVO.getAccountId(), type);
        }

        // 2.1 尚未同步或远端文件夹不可用时返回空列表
        if (folder == null) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        // 2.2 转换未读筛选条件并查询邮件分页
        OaMailMessagePageReqVO pageReqVO = BeanUtils.toBean(reqVO, OaMailMessagePageReqVO.class);
        if (OaMailFolderKeyEnum.UNREAD.getKey().equals(reqVO.getFolderKey())) {
            pageReqVO.setReadStatus(false);
        }
        return mailMessageMapper.selectPage(pageReqVO, folder.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaMailMessageDO getMailMessage(Long id, Long userId) {
        // 1. 校验索引归属和文件夹状态
        OaMailMessageDO index = validateMessage(id, userId);
        // 与同步、草稿替换共用账号锁，锁内重查，避免使用已失效的旧索引。
        OaMailAccountDO account = mailAccountService.lockMailAccount(index.getAccountId(), userId);
        index = mailMessageMapper.selectByIdForUpdate(id);
        if (index == null) {
            throw exception(MAIL_MESSAGE_NOT_EXISTS);
        }
        OaMailFolderDO localFolder = validateFolder(index);

        // 2.1 情况一：命中正文缓存时直接返回，空正文也属于已缓存
        if (index.getContent() != null) {
            return index;
        }

        // 2.2 情况二：首次读取远端详情，缓存正文及附件目录，不修改远端状态
        try (Store store = mailMessageClient.openStore(account)) {
            Folder folder = store.getFolder(localFolder.getName());
            try {
                folder.open(Folder.READ_ONLY);
                OaMailMessageDO detail = mailMessageClient.getDetail(folder, index);
                detail.setContent(detail.getContent() == null ? "" : mailMessageClient.sanitizeHtml(detail.getContent()));
                mailMessageMapper.updateById(new OaMailMessageDO().setId(id)
                        .setContent(detail.getContent())
                        .setReplyTos(detail.getReplyTos()).setAttachments(detail.getAttachments()));
                return detail;
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }
        } catch (MessagingException | IOException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_DETAIL_READ_FAILED);
        }
    }

    @Override
    public Pair<String, byte[]> getMailMessageAttachment(Long id, String part, Long userId) {
        // 1. 校验本人邮件和所属邮箱、文件夹
        OaMailMessageDO message = validateMessage(id, userId);
        OaMailAccountDO account = mailAccountService.validateMailAccount(message.getAccountId(), userId);
        OaMailFolderDO localFolder = validateFolder(message);

        // 2. 校验远端 UID 并读取附件；文件夹关闭前读取完整内容
        try (Store store = mailMessageClient.openStore(account);
             Folder folder = store.getFolder(localFolder.getName())) {
            folder.open(Folder.READ_ONLY);
            return mailMessageClient.getAttachment(mailMessageClient.getMessage(folder, message), part);
        } catch (MessagingException | IOException e) {
            log.warn("[mail][附件读取失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_ATTACHMENT_READ_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMailMessageRead(Long id, Boolean readStatus, Long userId) {
        // 1. 校验并串行化本人账号上的写操作
        OaMailMessageDO index = validateMessage(id, userId);
        OaMailAccountDO account = mailAccountService.lockMailAccount(index.getAccountId(), userId);
        OaMailFolderDO localFolder = validateFolder(index);

        // 2. 远端成功后更新索引，关闭文件夹时不清除其他已删除邮件
        try (Store store = mailMessageClient.openStore(account)) {
            Folder folder = store.getFolder(localFolder.getName());
            try {
                folder.open(Folder.READ_WRITE);
                mailMessageClient.getMessage(folder, index).setFlag(Flags.Flag.SEEN, Boolean.TRUE.equals(readStatus));
                mailMessageMapper.updateById(new OaMailMessageDO().setId(id).setReadStatus(readStatus));
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }
        } catch (MessagingException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_READ_STATUS_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMailMessage(Long id, Long userId) {
        // 1. 校验归属、文件夹和远端 UID
        OaMailMessageDO index = validateMessage(id, userId);
        OaMailAccountDO account = mailAccountService.lockMailAccount(index.getAccountId(), userId);
        OaMailFolderDO localFolder = validateFolder(index);

        // 2. 普通文件夹移至已删除；已删除中只清除用户指定的一封邮件
        try (Store store = mailMessageClient.openStore(account)) {
            Folder folder = store.getFolder(localFolder.getName());
            try {
                folder.open(Folder.READ_WRITE);
                mailMessageClient.deleteMessage(store, folder, mailMessageClient.getMessage(folder, index),
                        mailMessageClient.getFolders(store).get(OaMailFolderTypeEnum.TRASH.getType()), OaMailFolderTypeEnum.TRASH.getType().equals(localFolder.getType()));
                mailMessageMapper.deleteById(id);
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }
        } catch (MessagingException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_DELETE_UNCONFIRMED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreMailMessage(Long id, Long userId) {
        // 1. 校验归属及垃圾箱，仅恢复用户指定的邮件
        OaMailMessageDO index = validateMessage(id, userId);
        OaMailAccountDO account = mailAccountService.lockMailAccount(index.getAccountId(), userId);
        OaMailFolderDO localFolder = validateFolder(index);
        if (ObjUtil.notEqual(OaMailFolderTypeEnum.TRASH.getType(), localFolder.getType())) {
            throw exception(MAIL_NOT_TRASH);
        }

        // 2. 移回收件箱，旧 UID 索引删除，新索引由同步生成
        try (Store store = mailMessageClient.openStore(account)) {
            String inbox = mailMessageClient.getFolders(store).get(OaMailFolderTypeEnum.INBOX.getType());
            if (inbox == null) {
                throw exception(MAIL_FOLDER_NOT_AVAILABLE);
            }
            Folder folder = store.getFolder(localFolder.getName());
            try {
                folder.open(Folder.READ_WRITE);
                mailMessageClient.moveMessage(store, folder, mailMessageClient.getMessage(folder, index), inbox);
                mailMessageMapper.deleteById(id);
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }
        } catch (MessagingException e) {
            log.warn("[mail][远端恢复失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_RESTORE_UNCONFIRMED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveMailMessageDraft(OaMailMessageSaveReqVO reqVO, Long userId) {
        // 1. 校验本人账号及原草稿，不允许将普通邮件当作草稿覆盖
        OaMailAccountDO account = mailAccountService.lockMailAccount(reqVO.getAccountId(), userId);
        OaMailMessageDO oldDraft = validateDraft(reqVO, userId);

        // 2. 先追加新草稿，再按 UID 清理旧草稿；不允许全文件夹 expunge
        try (Store store = mailMessageClient.openStore(account)) {
            // 2.1 校验草稿目录及按 UID 安全替换能力
            String name = mailMessageClient.getFolders(store).get(OaMailFolderTypeEnum.DRAFTS.getType());
            if (name == null) {
                throw exception(MAIL_DRAFT_FOLDER_NOT_EXISTS);
            }
            if (!((IMAPStore) store).hasCapability(OaMailCapabilityEnum.UID_PLUS.getValue())) {
                throw exception(MAIL_DRAFT_REPLACE_UNSUPPORTED);
            }
            // 2.2 构建并追加新草稿
            MimeMessage message = buildOutgoingMessage(account, reqVO, userId, false);
            message.setFlag(Flags.Flag.DRAFT, true);
            IMAPFolder folder = (IMAPFolder) store.getFolder(name);
            long uid;
            try {
                folder.open(Folder.READ_WRITE);
                if (oldDraft != null) {
                    mailMessageClient.getMessage(folder, oldDraft);
                }
                AppendUID[] appended = folder.appendUIDMessages(new Message[]{message});
                if (appended == null || appended.length != 1 || appended[0] == null) {
                    throw exception(MAIL_DRAFT_SAVE_UNCONFIRMED);
                }
                uid = appended[0].uid;
                // 2.3 新草稿成功追加后，才清理指定旧草稿
                if (oldDraft != null) {
                    mailMessageClient.deleteMessage(store, folder, mailMessageClient.getMessage(folder, oldDraft), null, true);
                }
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }

            // 3. 同步草稿索引并返回新编号
            syncFolder(store, account.getId(), OaMailFolderTypeEnum.DRAFTS.getType(), name);
            OaMailFolderDO savedFolder = getFolder(account.getId(), OaMailFolderTypeEnum.DRAFTS.getType());
            List<OaMailMessageDO> drafts = mailMessageMapper.selectListByFolderId(savedFolder.getId());
            OaMailMessageDO savedDraft = CollUtil.findOne(drafts, item -> item.getUid() == uid);
            if (savedDraft == null) {
                throw exception(MAIL_DRAFT_INDEX_NOT_FOUND);
            }
            return savedDraft.getId();
        } catch (MessagingException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_DRAFT_SAVE_UNCONFIRMED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendMailMessage(OaMailMessageSaveReqVO reqVO, Long userId) {
        // 1. 校验账号、草稿及引用邮件归属
        OaMailAccountDO account = mailAccountService.lockMailAccount(reqVO.getAccountId(), userId);
        OaMailMessageDO draft = validateDraft(reqVO, userId);
        if (reqVO.getSourceId() != null) {
            OaMailMessageDO source = validateMessage(reqVO.getSourceId(), userId);
            if (ObjUtil.notEqual(source.getAccountId(), account.getId())) {
                throw exception(MAIL_SOURCE_NOT_OWNED);
            }
        }

        // 2. 发送只执行一次；超时不能视为一定未发出
        MimeMessage message;
        try {
            message = buildOutgoingMessage(account, reqVO, userId, true);
            mailMessageClient.sendMessage(account, message);
        } catch (MessagingException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_SEND_UNCONFIRMED);
        }

        // 3. SMTP 成功与 IMAP 副本处理分开反馈，副本失败绝不能导致用户重复发信
        try (Store store = mailMessageClient.openStore(account)) {
            Map<String, String> folders = mailMessageClient.getFolders(store);
            String sent = folders.get(OaMailFolderTypeEnum.SENT.getType());
            if (sent == null) {
                return "服务器已接收邮件；未识别到已发送文件夹，请在原邮箱核实副本";
            }
            Folder sentFolder = store.getFolder(sent);
            // 部分服务商自动保存已发送副本，按 Message-ID 检查后再追加。
            try {
                sentFolder.open(Folder.READ_ONLY);
                if (sentFolder.search(new HeaderTerm("Message-ID", message.getMessageID())).length == 0) {
                    sentFolder.appendMessages(new Message[]{message});
                }
            } finally {
                if (sentFolder.isOpen()) {
                    sentFolder.close(false);
                }
            }
            if (draft != null) {
                Folder draftFolder = store.getFolder(validateFolder(draft).getName());
                try {
                    draftFolder.open(Folder.READ_WRITE);
                    mailMessageClient.deleteMessage(store, draftFolder, mailMessageClient.getMessage(draftFolder, draft), null, true);
                    mailMessageMapper.deleteById(draft.getId());
                } finally {
                    if (draftFolder.isOpen()) {
                        draftFolder.close(false);
                    }
                }
            }
            return "服务器已接收邮件，请同步查看已发送";
        } catch (Exception e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            return "服务器已接收邮件；副本保存或草稿清理未完成，请在原邮箱核实，勿重发";
        }
    }

    /**
     * 同步邮件文件夹及其索引
     *
     * 完整抓取成功后对比 UID 快照，保留稳定的本地主键并更新远端状态。
     *
     * @param store 已建立的邮箱连接
     * @param accountId 邮箱账号编号
     * @param type 本地文件夹类型
     * @param name 远端文件夹完整名称
     * @return 本次文件夹同步的邮件索引数量
     * @throws MessagingException 读取远端文件夹或邮件失败
     */
    private int syncFolder(Store store, Long accountId, String type, String name) throws MessagingException {
        // 1. 查询并定位远端目录对应的本地索引
        List<OaMailFolderDO> folders = mailFolderService.getMailFolderList(accountId);
        OaMailFolderDO localFolder = CollUtil.findOne(folders, folder -> name.equals(folder.getName()));
        if (localFolder == null) {
            localFolder = new OaMailFolderDO().setAccountId(accountId).setName(name).setType(type).setAvailable(true);
            mailFolderService.createMailFolder(localFolder);
        }
        Folder folder = store.getFolder(name);
        try {
            folder.open(Folder.READ_ONLY);
            List<OaMailMessageDO> remote = mailMessageClient.getMessageList(folder);
            long validity = ((UIDFolder) folder).getUIDValidity();
            Map<String, OaMailMessageDO> existing = mailMessageMapper.selectListByFolderId(localFolder.getId()).stream()
                    .collect(Collectors.toMap(index -> index.getUidValidity() + ":" + index.getUid(), Function.identity()));
            for (OaMailMessageDO message : remote) {
                OaMailMessageDO old = existing.remove(message.getUidValidity() + ":" + message.getUid());
                message.setAccountId(accountId).setFolderId(localFolder.getId());
                if (old == null) {
                    mailMessageMapper.insert(message);
                } else {
                    mailMessageMapper.updateById(message.setId(old.getId()));
                }
            }
            // 仅完整快照中缺失的索引失效；实际邮件不会在同步过程中被删除。
            for (OaMailMessageDO removed : existing.values()) {
                mailMessageMapper.deleteById(removed.getId());
            }
            mailFolderService.updateMailFolder(new OaMailFolderDO().setId(localFolder.getId()).setType(type).setAvailable(true)
                    .setUidValidity(validity).setSyncTime(LocalDateTime.now()));
            return remote.size();
        } finally {
            if (folder.isOpen()) {
                folder.close(false);
            }
        }
    }

    /**
     * 校验邮件索引存在及账号归属
     *
     * @param id 本地邮件编号
     * @param userId 当前用户编号
     * @return 邮件索引
     */
    private OaMailMessageDO validateMessage(Long id, Long userId) {
        OaMailMessageDO message = mailMessageMapper.selectById(id);
        if (message == null) {
            throw exception(MAIL_MESSAGE_NOT_EXISTS);
        }
        mailAccountService.validateMailAccount(message.getAccountId(), userId);
        return message;
    }

    /**
     * 校验邮件所属文件夹可用
     *
     * 文件夹须与邮件属于同一邮箱账号。
     *
     * @param message 邮件索引
     * @return 邮件文件夹
     */
    private OaMailFolderDO validateFolder(OaMailMessageDO message) {
        return mailFolderService.validateMailFolder(message.getFolderId(), message.getAccountId());
    }

    /**
     * 获得账号下可用的指定类型文件夹
     *
     * @param accountId 邮箱账号编号
     * @param type 文件夹类型
     * @return 邮件文件夹，不存在时返回 null
     */
    private OaMailFolderDO getFolder(Long accountId, String type) {
        List<OaMailFolderDO> folders = mailFolderService.getMailFolderList(accountId);
        return CollUtil.findOne(folders, folder -> type.equals(folder.getType()) && Boolean.TRUE.equals(folder.getAvailable()));
    }

    /**
     * 校验待替换草稿存在及账号归属
     *
     * @param reqVO 邮件保存信息
     * @param userId 当前用户编号
     * @return 原草稿索引，未指定草稿编号时返回 null
     */
    private OaMailMessageDO validateDraft(OaMailMessageSaveReqVO reqVO, Long userId) {
        if (reqVO.getDraftId() == null) {
            return null;
        }
        OaMailMessageDO draft = validateMessage(reqVO.getDraftId(), userId);
        if (ObjUtil.notEqual(draft.getAccountId(), reqVO.getAccountId())
                || ObjUtil.notEqual(OaMailFolderTypeEnum.DRAFTS.getType(), validateFolder(draft).getType())) {
            throw exception(MAIL_DRAFT_NOT_OWNED);
        }
        return draft;
    }

    /**
     * 构建待保存或发送的邮件
     *
     * 回复时添加线程标识，转发或修改草稿时保留原附件。
     *
     * @param account 发件邮箱账号
     * @param reqVO 邮件内容
     * @param userId 当前用户编号
     * @param send 是否用于发送，false 时用于保存草稿
     * @return MIME 邮件
     * @throws MessagingException 构建邮件或读取原邮件失败
     */
    private MimeMessage buildOutgoingMessage(OaMailAccountDO account, OaMailMessageSaveReqVO reqVO,
                                            Long userId, boolean send) throws MessagingException {
        // 1.1 构建本次邮件正文
        MimeMessage message = mailMessageClient.createMessage(account, reqVO, send);
        Long originalId = ObjUtil.defaultIfNull(reqVO.getDraftId(), reqVO.getSourceId());
        if (originalId == null) {
            return message;
        }
        // 1.2 校验原邮件存在及归属
        OaMailMessageDO source = validateMessage(originalId, userId);
        if (ObjUtil.notEqual(source.getAccountId(), account.getId())) {
            throw exception(MAIL_SOURCE_NOT_OWNED);
        }

        // 2. 读取原邮件并处理转发及回复信息
        try (Store store = mailMessageClient.openStore(account)) {
            Folder folder = store.getFolder(validateFolder(source).getName());
            try {
                folder.open(Folder.READ_ONLY);
                // 2.1 读取原邮件，转发和编辑草稿时保留附件
                MimeMessage original = mailMessageClient.copyMessage(mailMessageClient.getMessage(folder, source));
                if (reqVO.getDraftId() != null || OaMailComposeModeEnum.FORWARD.getValue().equals(reqVO.getMode())) {
                    mailMessageClient.copyAttachments(message, original, reqVO.getAttachmentParts());
                }
                // 2.2 回复时补充线程标识
                if (StrUtil.equalsAny(reqVO.getMode(), OaMailComposeModeEnum.REPLY.getValue(), OaMailComposeModeEnum.REPLY_ALL.getValue())) {
                    String messageId = original.getMessageID();
                    if (messageId != null) {
                        message.setHeader("In-Reply-To", messageId);
                        message.setHeader("References", messageId);
                    }
                }
                message.saveChanges();
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }
        } catch (IOException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw exception(MAIL_SOURCE_READ_FAILED);
        }
        return message;
    }

    /**
     * 添加回复地址
     *
     * 排除本人邮箱，并按忽略大小写的邮箱地址去重。
     *
     * @param target 按邮箱地址索引的回复地址映射，新增地址写入此映射
     * @param values 待添加的邮箱地址列表
     * @param self 当前发件账号的邮箱地址
     * @throws MessagingException 邮箱地址解析失败
     */
    private void addReplyAddresses(Map<String, InternetAddress> target, List<String> values, String self)
            throws MessagingException {
        for (InternetAddress address : mailMessageClient.parseAddresses(values)) {
            if (!address.getAddress().equalsIgnoreCase(self)) {
                target.putIfAbsent(address.getAddress().toLowerCase(Locale.ROOT), address);
            }
        }
    }

    /**
     * 获得可放入编辑器的正文
     *
     * @param content 邮件正文
     * @return 移除样式和外部资源后的正文
     */
    private String getEditableContent(String content) {
        Document editable = Jsoup.parse(StrUtil.nullToEmpty(content));
        editable.select("style").remove();
        return Jsoup.clean(editable.body().html(), Safelist.relaxed().addTags("details", "summary"));
    }

}
