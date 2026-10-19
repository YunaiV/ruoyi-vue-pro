package cn.iocoder.yudao.module.oa.service.mail;

import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 企业邮箱协议 Client
 *
 * @author 芋道源码
 */
public interface OaMailMessageClient {

    /**
     * 建立邮箱连接
     *
     * 连接前校验账号及邮箱服务配置的启用状态。
     *
     * @param account 邮箱账号
     * @return 已建立的邮箱连接，由调用方负责关闭
     * @throws MessagingException 连接或认证失败
     */
    Store openStore(OaMailAccountDO account) throws MessagingException;

    /**
     * 获得远端邮件文件夹
     *
     * 优先按 SPECIAL-USE 属性识别标准目录，未声明属性时匹配常见名称，不创建远端文件夹。
     *
     * @param store 已建立的邮箱连接
     * @return 文件夹类型与远端完整名称的映射，自建目录以 CUSTOM:完整名称作为键
     * @throws MessagingException 读取文件夹失败
     */
    Map<String, String> getFolders(Store store) throws MessagingException;

    /**
     * 根据文件夹名称获得标准类型
     *
     * @param name 远端文件夹名称
     * @return 标准文件夹类型，无法识别时返回 null
     */
    String getFolderType(String name);

    /**
     * 获得文件夹的邮件索引列表
     *
     * 分批抓取完整信封快照，超过数量上限时拒绝同步，避免误删未同步的旧索引。
     *
     * @param folder 已打开的远端文件夹
     * @return 邮件索引列表，不包含正文缓存
     * @throws MessagingException 读取邮件索引失败
     */
    List<OaMailMessageDO> getMessageList(Folder folder) throws MessagingException;

    /**
     * 获得远端邮件
     *
     * 校验文件夹 UID 有效期，避免使用过期索引读取或修改其他邮件。
     *
     * @param folder 已打开的远端文件夹
     * @param index 本地邮件索引
     * @return 远端邮件
     * @throws MessagingException 读取远端邮件失败
     */
    Message getMessage(Folder folder, OaMailMessageDO index) throws MessagingException;

    /**
     * 获得远端邮件详情
     *
     * 不自动修改邮件已读状态。
     *
     * @param folder 已打开的远端文件夹
     * @param index 本地邮件索引
     * @return 包含安全正文和附件元数据的邮件详情
     * @throws MessagingException 读取邮件信息失败
     * @throws IOException 读取邮件内容失败
     */
    OaMailMessageDO getDetail(Folder folder, OaMailMessageDO index) throws MessagingException, IOException;

    /**
     * 过滤邮件 HTML 正文
     *
     * 使用 HTML 白名单过滤，前端仍须通过 sandbox 和 CSP 限制脚本和外部资源。
     *
     * @param html 原始 HTML 正文，允许为空
     * @return 过滤后的 HTML 正文
     */
    String sanitizeHtml(String html);

    /**
     * 复制邮件 MIME 内容
     *
     * @param message 原始邮件
     * @return 内存中的邮件副本
     * @throws MessagingException 读取邮件信息失败
     * @throws IOException 读取邮件内容失败
     */
    MimeMessage copyMessage(Message message) throws MessagingException, IOException;

    /**
     * 删除远端邮件
     *
     * 普通删除将邮件移入已删除文件夹；彻底删除只清除当前 UID，不执行全文件夹清除。
     *
     * @param store 已建立的邮箱连接
     * @param folder 邮件所在的已打开文件夹
     * @param message 待删除的邮件
     * @param trash 已删除文件夹的完整名称，彻底删除时允许为空
     * @param permanent 是否彻底删除
     * @throws MessagingException 移动或删除邮件失败
     */
    void deleteMessage(Store store, Folder folder, Message message, String trash, boolean permanent) throws MessagingException;

    /**
     * 将单封邮件移动到指定文件夹
     *
     * @param store 邮箱连接
     * @param folder 已打开的源文件夹
     * @param message 邮件
     * @param target 目标文件夹完整名称
     * @throws MessagingException 移动失败
     */
    void moveMessage(Store store, Folder folder, Message message, String target) throws MessagingException;

    /**
     * 构建待保存或发送的邮件
     *
     * 草稿允许暂缺收件人，发送时校验收件人及邮件头。
     *
     * @param account 发件邮箱账号
     * @param reqVO 邮件内容
     * @param send 是否用于发送，false 时用于保存草稿
     * @return MIME 邮件
     * @throws MessagingException 构建邮件失败
     */
    MimeMessage createMessage(OaMailAccountDO account, OaMailMessageSaveReqVO reqVO, boolean send) throws MessagingException;

    /**
     * 解析邮箱地址
     *
     * 校验地址格式，禁止邮件头注入。
     *
     * @param values 邮箱地址列表，每项为一个完整地址
     * @return 邮箱地址数组，空列表返回空数组
     * @throws AddressException 邮箱地址格式不正确
     */
    InternetAddress[] parseAddresses(List<String> values) throws AddressException;

    /**
     * 复制原邮件附件
     *
     * 用于转发邮件或编辑草稿，保留原邮件附件。
     *
     * @param target 目标邮件
     * @param source 原邮件
     * @param attachmentParts 保留的附件路径，null 时保留全部
     * @throws MessagingException 处理邮件结构失败
     * @throws IOException 读取邮件内容失败
     */
    void copyAttachments(MimeMessage target, MimeMessage source, List<String> attachmentParts) throws MessagingException, IOException;

    /**
     * 读取邮件中的指定附件，不允许将正文作为附件下载
     *
     * @param message 远端邮件
     * @param path 附件 MIME 路径
     * @return 附件名称和内容
     * @throws MessagingException 邮件结构读取失败
     * @throws IOException 附件内容读取失败
     */
    Pair<String, byte[]> getAttachment(Message message, String path) throws MessagingException, IOException;

    /**
     * 通过 SMTP 发送邮件
     *
     * 仅投递一次，不自动重试；服务器接收成功不代表最终送达。
     *
     * @param account 发件邮箱账号
     * @param message 待发送的邮件
     * @throws MessagingException 连接失败或投递结果未确认
     */
    void sendMessage(OaMailAccountDO account, MimeMessage message) throws MessagingException;

    /**
     * 建立 IMAP 连接
     *
     * @param config IMAP 连接配置
     * @param username 登录用户名
     * @param password 密码或授权码
     * @return 已建立的邮箱连接，由调用方负责关闭
     * @throws MessagingException 连接或认证失败
     */
    Store openImapStore(OaMailProviderDO.ConnectionConfig config, String username, String password)
            throws MessagingException;

    /**
     * 测试邮箱连接
     *
     * 仅测试协议认证，不发送邮件。
     *
     * @param config 邮箱连接配置
     * @param username 登录用户名
     * @param password 密码或授权码
     * @param imap 是否测试 IMAP，false 时测试 SMTP
     * @return 是否连接成功
     */
    boolean testConnection(OaMailProviderDO.ConnectionConfig config, String username, String password,
                           boolean imap);

}
