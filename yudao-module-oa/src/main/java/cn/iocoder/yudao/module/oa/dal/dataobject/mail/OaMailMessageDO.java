package cn.iocoder.yudao.module.oa.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业邮箱邮件索引 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_mail_message", autoResultMap = true)
@KeySequence("oa_mail_message_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaMailMessageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 邮箱账号编号
     *
     * 关联 {@link OaMailAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 文件夹编号
     *
     * 关联 {@link OaMailFolderDO#getId()}
     */
    private Long folderId;
    /**
     * 远端文件夹 UID 有效期
     */
    private Long uidValidity;
    /**
     * 远端邮件 UID
     */
    private Long uid;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 经安全过滤的正文缓存
     *
     * null 表示未读取，空字符串表示正文为空
     */
    private String content;
    /**
     * 回复地址
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> replyTos;
    /**
     * 附件目录缓存，附件文件仍从远端读取
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Attachment> attachments;
    /**
     * 发件人
     */
    private String sender;
    /**
     * 收件人
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> recipients;
    /**
     * 抄送人
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> ccs;
    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;
    /**
     * 是否已读
     */
    private Boolean readStatus;
    /**
     * 是否有附件
     */
    private Boolean hasAttach;
    /**
     * 邮件大小（字节）
     */
    private Integer size;

    /**
     * 远端附件目录信息
     */
    @Data
    public static class Attachment {

        /**
         * MIME 部件路径
         */
        private String part;
        /**
         * 附件名称
         */
        private String name;
        /**
         * 附件大小，单位字节
         */
        private Integer size;

    }

}
