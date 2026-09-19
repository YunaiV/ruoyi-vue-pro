package cn.iocoder.yudao.module.oa.service.mail;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.*;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailCapabilityEnum;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailFolderTypeEnum;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 企业邮箱远端操作：保持账号隔离，禁止下载外链和隐式全文件夹清除。
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class OaMailMessageClientImpl implements OaMailMessageClient {

    /**
     * 单文件夹一次完整同步的最大邮件数量
     */
    private static final int MAX_MESSAGES = 100000;
    /**
     * 每次向远端批量获取的邮件数量
     */
    private static final int MESSAGE_FETCH_BATCH_SIZE = 100;

    /**
     * IMAP SPECIAL-USE 属性与标准目录类型的映射
     */
    private static final Map<String, String> FOLDER_ATTRIBUTE_TYPES = new HashMap<>();
    /**
     * 服务商常用目录名称与标准目录类型的映射，名称统一为小写
     */
    private static final Map<String, String> FOLDER_NAME_TYPES = new HashMap<>();

    /**
     * MIME 多部件类型匹配符
     */
    private static final String MULTIPART_TYPE = "multipart/*";
    /**
     * 同一正文的多种格式
     */
    private static final String MULTIPART_ALTERNATIVE_TYPE = "multipart/alternative";

    static {
        // 初始化 FOLDER_ATTRIBUTE_TYPES
        FOLDER_ATTRIBUTE_TYPES.put("\\sent", OaMailFolderTypeEnum.SENT.getType());
        FOLDER_ATTRIBUTE_TYPES.put("\\drafts", OaMailFolderTypeEnum.DRAFTS.getType());
        FOLDER_ATTRIBUTE_TYPES.put("\\trash", OaMailFolderTypeEnum.TRASH.getType());
        // 初始化 FOLDER_NAME_TYPES
        for (String name : Arrays.asList("sent", "sent messages", "sent items", "已发送", "已发送邮件")) {
            FOLDER_NAME_TYPES.put(name, OaMailFolderTypeEnum.SENT.getType());
        }
        for (String name : Arrays.asList("drafts", "draft", "草稿箱", "草稿")) {
            FOLDER_NAME_TYPES.put(name, OaMailFolderTypeEnum.DRAFTS.getType());
        }
        for (String name : Arrays.asList("trash", "deleted messages", "deleted items", "已删除", "废纸篓")) {
            FOLDER_NAME_TYPES.put(name, OaMailFolderTypeEnum.TRASH.getType());
        }
    }

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaMailProviderService mailProviderService;

    // ==================== 连接入口 ====================

    @Override
    public Store openStore(OaMailAccountDO account) throws MessagingException {
        OaMailProviderDO provider = getProvider(account);
        return openImapStore(provider.getImap(), account.getUsername(), account.getPassword());
    }

    // ==================== 文件夹识别 ====================

    @Override
    public Map<String, String> getFolders(Store store) throws MessagingException {
        // 1. 查询可访问的远端目录
        Folder[] folders = store.getDefaultFolder().list("*");

        // 2. 优先按 IMAP SPECIAL-USE 属性识别标准目录
        Map<String, String> result = new LinkedHashMap<>();
        result.put(OaMailFolderTypeEnum.INBOX.getType(), OaMailFolderTypeEnum.INBOX.getType());
        for (Folder folder : folders) {
            if (!holdsMessages(folder)) {
                continue;
            }
            if (!(folder instanceof IMAPFolder)) {
                continue;
            }
            for (String attribute : ((IMAPFolder) folder).getAttributes()) {
                String type = FOLDER_ATTRIBUTE_TYPES.get(attribute.toLowerCase(Locale.ROOT));
                if (type != null) {
                    result.put(type, folder.getFullName());
                }
            }
        }
        for (Folder folder : folders) {
            if (!holdsMessages(folder)) {
                continue;
            }
            String type = getFolderType(folder.getName());
            if (type != null) {
                result.putIfAbsent(type, folder.getFullName());
            }
        }
        // 保留可存邮件的自建目录；完整路径作为临时键，避免同名子目录覆盖。
        for (Folder folder : folders) {
            if (!holdsMessages(folder) || result.containsValue(folder.getFullName())) {
                continue;
            }
            result.put(OaMailFolderTypeEnum.getCustomKey(folder.getFullName()), folder.getFullName());
        }
        return result;
    }

    @Override
    public String getFolderType(String name) {
        return FOLDER_NAME_TYPES.get(name.toLowerCase(Locale.ROOT));
    }

    // ==================== 邮件索引与正文读取 ====================

    @Override
    public List<OaMailMessageDO> getMessageList(Folder folder) throws MessagingException {
        int count = folder.getMessageCount();
        if (count > MAX_MESSAGES) {
            throw exception(MAIL_SYNC_COUNT_EXCEEDED, MAX_MESSAGES);
        }
        UIDFolder uidFolder = (UIDFolder) folder;
        List<OaMailMessageDO> messages = new ArrayList<>();
        for (int begin = 1; begin <= count; begin += MESSAGE_FETCH_BATCH_SIZE) {
            Message[] batch = folder.getMessages(begin, Math.min(count, begin + MESSAGE_FETCH_BATCH_SIZE - 1));
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            profile.add(FetchProfile.Item.FLAGS);
            profile.add(FetchProfile.Item.CONTENT_INFO);
            profile.add(UIDFolder.FetchProfileItem.UID);
            folder.fetch(batch, profile);
            for (Message message : batch) {
                if (message.isExpunged() || message.isSet(Flags.Flag.DELETED)) {
                    continue;
                }
                Date date;
                try {
                    date = message.getReceivedDate() == null ? message.getSentDate() : message.getReceivedDate();
                } catch (MessagingException e) {
                    // 网易部分历史邮件仅返回 UID 和 FLAGS，邮件头及正文均为空；保留索引，不中断其他邮件同步
                    // 连接断开、超时等异常仍向上抛出，避免将不完整同步当成完整快照
                    if (!"Failed to load IMAP envelope".equals(e.getMessage())) {
                        throw e;
                    }
                    messages.add(new OaMailMessageDO().setUid(uidFolder.getUID(message)).setUidValidity(uidFolder.getUIDValidity())
                            .setSubject("（服务端暂未提供邮件内容）").setReadStatus(message.isSet(Flags.Flag.SEEN))
                            .setHasAttach(false));
                    log.warn("[getMessageList][服务端未提供邮件内容，目录({}) UID({})]", folder.getFullName(), uidFolder.getUID(message));
                    continue;
                }
                messages.add(new OaMailMessageDO().setUid(uidFolder.getUID(message)).setUidValidity(uidFolder.getUIDValidity())
                        .setSubject(StrUtil.nullToEmpty(message.getSubject()))
                        .setSender(addresses(message.getFrom()))
                        .setRecipients(addressList(message.getRecipients(Message.RecipientType.TO)))
                        .setCcs(addressList(message.getRecipients(Message.RecipientType.CC)))
                        .setReceiveTime(date == null ? null : DateUtils.of(date))
                        .setReadStatus(message.isSet(Flags.Flag.SEEN)).setSize(message.getSize()).setHasAttach(hasAttachment(message, 0)));
            }
        }
        if (folder.getMessageCount() != count) {
            throw exception(MAIL_SYNC_CHANGED);
        }
        return messages;
    }

    @Override
    public Message getMessage(Folder folder, OaMailMessageDO index) throws MessagingException {
        UIDFolder uidFolder = (UIDFolder) folder;
        if (uidFolder.getUIDValidity() != index.getUidValidity()) {
            throw exception(MAIL_UID_EXPIRED);
        }
        Message message = uidFolder.getMessageByUID(index.getUid());
        if (message == null || message.isExpunged() || message.isSet(Flags.Flag.DELETED)) {
            throw exception(MAIL_MESSAGE_NOT_EXISTS);
        }
        return message;
    }

    @Override
    public OaMailMessageDO getDetail(Folder folder, OaMailMessageDO index) throws MessagingException, IOException {
        // 1. 校验远端 UID 并读取邮件信封
        Message message = getMessage(folder, index);
        OaMailMessageDO result = BeanUtils.toBean(index, OaMailMessageDO.class)
                .setReplyTos(addressList(message.getReplyTo()))
                .setReadStatus(message.isSet(Flags.Flag.SEEN));
        // 2. 解析安全正文和附件信息
        MimeMessage copy = copyMessage(message);
        List<OaMailMessageDO.Attachment> attachments = new ArrayList<>();
        Map<String, String> inlineImages = new HashMap<>();
        Document document = Jsoup.parse(readContent(copy, "", 0, attachments, inlineImages));
        for (Element image : document.select("img[src]")) {
            String src = image.attr("src");
            if (StrUtil.startWithIgnoreCase(src, "cid:")) {
                String data = inlineImages.get(src.substring(4));
                if (data != null) {
                    image.attr("src", data);
                } else {
                    image.removeAttr("src");
                }
            }
        }
        result.setContent(sanitizeHtml(document.body().html())).setAttachments(attachments);
        return result;
    }

    @Override
    public String sanitizeHtml(String html) {
        Document document = Jsoup.parse(StrUtil.nullToEmpty(html));
        // 内嵌图片只允许常用位图，拒绝 data HTML、SVG 等可执行内容
        for (Element image : document.select("img[src]")) {
            String src = image.attr("src");
            if (StrUtil.startWithIgnoreCase(src, "data:")
                    && !src.matches("(?i)data:image/(png|jpeg|gif|webp);base64,[a-z0-9+/=\\s]+")) {
                image.removeAttr("src");
            }
        }
        Safelist safelist = Safelist.relaxed().addTags("style", "details", "summary").addAttributes(":all", "style")
                .addProtocols("img", "src", "cid", "data")
                .addEnforcedAttribute("a", "target", "_blank").addEnforcedAttribute("a", "rel", "noopener noreferrer");
        return Jsoup.clean(document.body().html(), safelist);
    }

    /**
     * 解析邮件正文和附件元数据
     *
     * 处理全部 MIME 子项，附件与正文分开展示。
     *
     * @param part MIME 部件
     * @param path 当前 MIME 部件路径
     * @param depth 当前递归层级
     * @param attachments 附件元数据列表，解析出的附件追加到此列表
     * @param inlineImages Content-ID 与内嵌位图的对应关系
     * @return 安全 HTML 正文，无可读正文时返回空字符串
     * @throws MessagingException 读取邮件结构失败
     * @throws IOException 读取邮件内容失败
     */
    private String readContent(Part part, String path, int depth, List<OaMailMessageDO.Attachment> attachments,
                               Map<String, String> inlineImages)
            throws MessagingException, IOException {
        String[] contentIds = part.getHeader("Content-ID");
        if (ArrayUtil.isNotEmpty(contentIds) && (part.isMimeType("image/png") || part.isMimeType("image/jpeg")
                || part.isMimeType("image/gif") || part.isMimeType("image/webp"))) {
            String contentId = StrUtil.strip(contentIds[0].trim(), "<", ">");
            String mimeType = new javax.mail.internet.ContentType(part.getContentType()).getBaseType();
            try (InputStream input = part.getInputStream()) {
                inlineImages.put(contentId, "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(IoUtil.readBytes(input)));
            }
        }
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            attachments.add(new OaMailMessageDO.Attachment().setPart(path).setName(MimeUtility.decodeText(
                    StrUtil.blankToDefault(part.getFileName(), "附件"))).setSize(part.getSize()));
            return "";
        }
        if (part.isMimeType(ContentType.TEXT_HTML.getValue())) {
            return sanitizeHtml((String) part.getContent());
        }
        if (part.isMimeType(ContentType.TEXT_PLAIN.getValue())) {
            return "<pre style='white-space:pre-wrap'>" + Entities.escape((String) part.getContent()) + "</pre>";
        }
        if (!part.isMimeType(MULTIPART_TYPE)) {
            return "";
        }
        Multipart multipart = (Multipart) part.getContent();
        StringBuilder content = new StringBuilder();
        boolean alternative = part.isMimeType(MULTIPART_ALTERNATIVE_TYPE);
        for (int i = 0; i < multipart.getCount(); i++) {
            String child = readContent(multipart.getBodyPart(i), path.isEmpty() ? "" + i : path + "." + i, depth + 1, attachments, inlineImages);
            if (alternative && !child.isEmpty()) {
                content.setLength(0);
            }
            content.append(child);
        }
        return content.toString();
    }

    /**
     * 判断邮件是否包含附件
     *
     * 读取邮件结构，不下载附件内容。
     *
     * @param part MIME 部件
     * @param depth 当前递归层级
     * @return 是否包含附件
     * @throws MessagingException 读取邮件结构失败
     */
    private boolean hasAttachment(Part part, int depth) throws MessagingException {
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            return true;
        }
        if (!part.isMimeType(MULTIPART_TYPE)) {
            return false;
        }
        try {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                if (hasAttachment(multipart.getBodyPart(i), depth + 1)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            throw new MessagingException("无法读取附件结构");
        }
    }

    // ==================== 邮件复制与写入 ====================

    @Override
    public MimeMessage copyMessage(Message message) throws MessagingException, IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        message.writeTo(bytes);
        return new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(bytes.toByteArray()));
    }

    @Override
    public void deleteMessage(Store store, Folder folder, Message message, String trash, boolean permanent) throws MessagingException {
        IMAPStore imapStore = (IMAPStore) store;
        IMAPFolder imapFolder = (IMAPFolder) folder;
        if (permanent) {
            if (!imapStore.hasCapability(OaMailCapabilityEnum.UID_PLUS.getValue())) {
                throw exception(MAIL_PERMANENT_DELETE_UNSUPPORTED);
            }
            message.setFlag(Flags.Flag.DELETED, true);
            imapFolder.expunge(new Message[]{message});
        } else {
            if (trash == null) {
                throw exception(MAIL_TRASH_NOT_EXISTS);
            }
            moveMessage(store, folder, message, trash);
        }
    }

    @Override
    public void moveMessage(Store store, Folder folder, Message message, String target) throws MessagingException {
        if (!((IMAPStore) store).hasCapability(OaMailCapabilityEnum.MOVE.getValue())) {
            throw exception(MAIL_MOVE_UNSUPPORTED);
        }
        ((IMAPFolder) folder).moveMessages(new Message[]{message}, store.getFolder(target));
    }

    /**
     * 组装草稿或待发送邮件，清洗正文并将上传文件作为独立 MIME 附件
     *
     * @param account 发件邮箱账号
     * @param reqVO 正文、收件地址及附件
     * @param send 是否发送，发送时要求至少一个收件人
     * @return 已组装的 MIME 邮件
     * @throws MessagingException 邮件地址或附件读取失败
     */
    @Override
    public MimeMessage createMessage(OaMailAccountDO account, OaMailMessageSaveReqVO reqVO, boolean send) throws MessagingException {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        message.setFrom(new InternetAddress(account.getMail(), true));
        message.setRecipients(Message.RecipientType.TO, parseAddresses(reqVO.getRecipients()));
        message.setRecipients(Message.RecipientType.CC, parseAddresses(reqVO.getCcs()));
        if (send && ArrayUtil.isEmpty(message.getAllRecipients())) {
            throw exception(MAIL_RECIPIENT_REQUIRED);
        }
        if (reqVO.getSubject() != null && (reqVO.getSubject().contains("\r") || reqVO.getSubject().contains("\n"))) {
            throw exception(MAIL_SUBJECT_INVALID);
        }
        message.setSubject(StrUtil.nullToEmpty(reqVO.getSubject()), StandardCharsets.UTF_8.name());
        message.setContent(sanitizeHtml(reqVO.getContent()), ContentType.TEXT_HTML.getValue() + "; charset=" + StandardCharsets.UTF_8.name());
        // 有上传附件时使用 multipart/mixed，第一个部件为正文，其余部件为附件。
        if (CollUtil.isNotEmpty(reqVO.getFiles())) {
            MimeMultipart multipart = new MimeMultipart("mixed");
            MimeBodyPart body = new MimeBodyPart();
            body.setContent(sanitizeHtml(reqVO.getContent()), "text/html; charset=UTF-8");
            multipart.addBodyPart(body);
            for (MultipartFile file : reqVO.getFiles()) {
                MimeBodyPart attachment = new MimeBodyPart();
                try {
                    attachment.setDataHandler(new DataHandler(new ByteArrayDataSource(file.getBytes(),
                            StrUtil.blankToDefault(file.getContentType(), "application/octet-stream"))));
                } catch (IOException e) {
                    throw new MessagingException("无法读取上传附件", e);
                }
                attachment.setFileName(FileUtil.getName(StrUtil.blankToDefault(file.getOriginalFilename(), "附件"))
                        .replace('\r', '_').replace('\n', '_'));
                attachment.setDisposition(Part.ATTACHMENT);
                multipart.addBodyPart(attachment);
            }
            message.setContent(multipart);
        }
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    @Override
    public InternetAddress[] parseAddresses(List<String> values) throws AddressException {
        if (CollUtil.isEmpty(values)) {
            return new InternetAddress[0];
        }
        List<InternetAddress> addresses = new ArrayList<>();
        for (String value : values) {
            if (StrUtil.isBlank(value) || StrUtil.containsAny(value, "\r", "\n")) {
                throw exception(MAIL_ADDRESS_INVALID);
            }
            InternetAddress address = new InternetAddress(value, true);
            address.validate();
            addresses.add(address);
        }
        return addresses.toArray(new InternetAddress[0]);
    }

    @Override
    public void copyAttachments(MimeMessage target, MimeMessage source, List<String> attachmentParts) throws MessagingException, IOException {
        MimeMultipart multipart = new MimeMultipart("mixed");
        MimeBodyPart body = new MimeBodyPart();
        body.setContent(target.getContent(), target.getContentType());
        multipart.addBodyPart(body);
        addAttachments(source, multipart, "", attachmentParts);
        if (multipart.getCount() > 1) {
            target.setContent(multipart);
        }
        target.saveChanges();
    }

    /**
     * 递归添加邮件附件
     *
     * 从邮件副本提取全部附件。
     *
     * @param part 原邮件的 MIME 部件
     * @param target 目标 MIME 多部件内容
     * @param path 当前 MIME 路径
     * @param attachmentParts 保留的附件路径，null 时保留全部
     * @throws MessagingException 处理附件结构失败
     * @throws IOException 读取附件内容失败
     */
    private void addAttachments(Part part, MimeMultipart target, String path, List<String> attachmentParts) throws MessagingException, IOException {
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            if (attachmentParts != null && !attachmentParts.contains(path)) {
                return;
            }
            MimeBodyPart copy = new MimeBodyPart();
            copy.setDataHandler(part.getDataHandler());
            copy.setFileName(part.getFileName());
            copy.setDisposition(Part.ATTACHMENT);
            target.addBodyPart(copy);
        } else if (part.isMimeType(MULTIPART_TYPE)) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                addAttachments(multipart.getBodyPart(i), target, path.isEmpty() ? String.valueOf(i) : path + "." + i, attachmentParts);
            }
        }
    }

    @Override
    public Pair<String, byte[]> getAttachment(Message message, String path) throws MessagingException, IOException {
        // 1. 根据元数据中的 MIME 路径定位附件，不读取外部 URL
        Part part = message;
        if (path == null || (!path.isEmpty() && !path.matches("[0-9]+(\\.[0-9]+)*"))) {
            throw exception(MAIL_ATTACHMENT_NOT_EXISTS);
        }
        if (!path.isEmpty()) {
            for (String index : path.split("\\.")) {
                if (!part.isMimeType(MULTIPART_TYPE)) {
                    throw exception(MAIL_ATTACHMENT_NOT_EXISTS);
                }
                Multipart multipart = (Multipart) part.getContent();
                int partIndex;
                try {
                    partIndex = Integer.parseInt(index);
                } catch (NumberFormatException e) {
                    throw exception(MAIL_ATTACHMENT_NOT_EXISTS);
                }
                if (partIndex >= multipart.getCount()) {
                    throw exception(MAIL_ATTACHMENT_NOT_EXISTS);
                }
                part = multipart.getBodyPart(partIndex);
            }
        }
        // 2. 只返回附件部件，拒绝正文路径
        if (!Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && part.getFileName() == null) {
            throw exception(MAIL_ATTACHMENT_NOT_EXISTS);
        }
        try (InputStream input = part.getInputStream()) {
            return Pair.of(MimeUtility.decodeText(StrUtil.blankToDefault(part.getFileName(), "附件")), IoUtil.readBytes(input));
        }
    }

    @Override
    public void sendMessage(OaMailAccountDO account, MimeMessage message) throws MessagingException {
        OaMailProviderDO.ConnectionConfig config = getProvider(account).getSmtp();
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.ssl.enable", config.getSslEnable().toString());
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        properties.setProperty("mail.smtp.starttls.enable", config.getStarttlsEnable().toString());
        properties.setProperty("mail.smtp.starttls.required", config.getStarttlsEnable().toString());
        properties.setProperty("mail.smtp.connectiontimeout", "10000");
        properties.setProperty("mail.smtp.timeout", "30000");
        properties.setProperty("mail.smtp.writetimeout", "30000");
        try (Transport transport = Session.getInstance(properties).getTransport("smtp")) {
            transport.connect(config.getHost(), config.getPort(), account.getUsername(), account.getPassword());
            transport.sendMessage(message, message.getAllRecipients());
        }
    }

    /**
     * 获得可用的邮箱服务配置
     *
     * 校验邮箱账号和服务配置均为启用状态
     *
     * @param account 邮箱账号
     * @return 邮箱服务配置
     */
    private OaMailProviderDO getProvider(OaMailAccountDO account) {
        if (CommonStatusEnum.isDisable(account.getStatus())) {
            throw exception(MAIL_ACCOUNT_DISABLED);
        }
        OaMailProviderDO provider = mailProviderService.validateMailProviderExists(account.getProviderId());
        if (CommonStatusEnum.isDisable(provider.getStatus())) {
            throw exception(MAIL_PROVIDER_DISABLED);
        }
        return provider;
    }

    /**
     * 将邮箱地址转换为文本
     *
     * @param addresses 邮箱地址数组，允许为空
     * @return 包含显示名称的地址文本，空数组返回空字符串
     */
    private static String addresses(Address[] addresses) {
        return addresses == null ? "" : InternetAddress.toUnicodeString(addresses);
    }

    // ==================== 协议连接管理 ====================

    @Override
    public Store openImapStore(OaMailProviderDO.ConnectionConfig config, String username, String password)
            throws MessagingException {
        Properties properties = new Properties();
        properties.setProperty("mail.imap.ssl.enable", config.getSslEnable().toString());
        properties.setProperty("mail.imap.ssl.checkserveridentity", "true");
        properties.setProperty("mail.imap.starttls.enable", config.getStarttlsEnable().toString());
        properties.setProperty("mail.imap.starttls.required", config.getStarttlsEnable().toString());
        properties.setProperty("mail.imap.connectiontimeout", "10000");
        properties.setProperty("mail.imap.timeout", "10000");
        properties.setProperty("mail.imap.writetimeout", "10000");
        properties.setProperty("mail.imap.peek", "true");
        Store store = Session.getInstance(properties).getStore("imap");
        try {
            store.connect(config.getHost(), config.getPort(), username, password);
            // 支持 ID 扩展的服务端需要客户端身份信息，例如网易在打开收件箱前进行校验
            if (store instanceof IMAPStore && ((IMAPStore) store).hasCapability(OaMailCapabilityEnum.ID.getValue())) {
                ((IMAPStore) store).id(Collections.singletonMap("name", "Yudao OA Mail"));
            }
            return store;
        } catch (MessagingException e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            try {
                store.close();
            } catch (MessagingException ignored) {
                // 连接尚未建立，不覆盖认证失败原因
            }
            throw e;
        }
    }

    @Override
    public boolean testConnection(OaMailProviderDO.ConnectionConfig config,
                                  String username, String password, boolean imap) {
        try {
            if (imap) {
                try (Store store = openImapStore(config, username, password)) {
                    // 同时验证收件箱可读，避免认证成功但实际读取被服务端拒绝
                    try (Folder folder = store.getFolder(OaMailFolderTypeEnum.INBOX.getType())) {
                        folder.open(Folder.READ_ONLY);
                    }
                }
            } else {
                String protocol = "smtp";
                Properties properties = new Properties();
                String prefix = "mail." + protocol + ".";
                properties.setProperty(prefix + "ssl.enable", config.getSslEnable().toString());
                properties.setProperty(prefix + "ssl.checkserveridentity", "true");
                properties.setProperty(prefix + "starttls.enable", config.getStarttlsEnable().toString());
                properties.setProperty(prefix + "starttls.required", config.getStarttlsEnable().toString());
                properties.setProperty(prefix + "connectiontimeout", "10000");
                properties.setProperty(prefix + "timeout", "10000");
                properties.setProperty(prefix + "writetimeout", "10000");
                properties.setProperty(prefix + "auth", "true");
                Session session = Session.getInstance(properties);
                try (Transport transport = session.getTransport(protocol)) {
                    transport.connect(config.getHost(), config.getPort(), username, password);
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("[mail][远端操作失败，异常类型({})]", e.getClass().getSimpleName());
            // 不向前端返回可能包含账号、连接参数或凭据的底层异常
            return false;
        }
    }


    /**
     * 判断目录是否可存邮件
     *
     * @param folder 远端目录
     * @return 是否可存邮件
     * @throws MessagingException 读取目录类型失败
     */
    private boolean holdsMessages(Folder folder) throws MessagingException {
        return (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
    }

    /**
     * 将远端地址数组转换为完整地址列表
     *
     * @param addresses 远端邮箱地址
     * @return 地址列表
     */
    private static List<String> addressList(Address[] addresses) {
        if (ArrayUtil.isEmpty(addresses)) {
            return Collections.emptyList();
        }
        return convertList(Arrays.asList(addresses), address -> address instanceof InternetAddress
                ? ((InternetAddress) address).toUnicodeString() : address.toString());
    }

}
