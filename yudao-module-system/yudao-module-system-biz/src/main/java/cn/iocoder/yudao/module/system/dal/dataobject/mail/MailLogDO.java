package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.enums.mail.MailSendStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 邮箱日志 DO
 * 记录每一次邮件的发送
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@TableName(value = "system_mail_log", autoResultMap = true)
@KeySequence("system_mail_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailLogDO extends BaseDO implements Serializable {

    /**
     * 日志编号，自增
     */
    private Long id;

    /**
     * 用户编码
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;
    /**
     * 接收邮箱地址
     */
    private String toMail;

    /**
     * 邮箱账号编号
     *
     * 关联 {@link MailAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 发送邮箱地址
     *
     * 冗余 {@link MailAccountDO#getMail()}
     */
    private String fromMail;

    // ========= 模板相关字段 =========
    /**
     * 模版编号
     *
     * 关联 {@link MailTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 模版编码
     *
     * 冗余 {@link MailTemplateDO#getCode()}
     */
    private String templateCode;
    /**
     * 模版发送人名称
     *
     * 冗余 {@link MailTemplateDO#getNickname()}
     */
    private String templateNickname;
    /**
     * 模版标题
     */
    private String templateTitle;
    /**
     * 模版内容
     *
     * 基于 {@link MailTemplateDO#getContent()} 格式化后的内容
     */
    private String templateContent;
    /**
     * 模版参数
     *
     * 基于 {@link MailTemplateDO#getParams()} 输入后的参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> templateParams;

    // ========= 发送相关字段 =========
    /**
     * 发送状态
     *
     * 枚举 {@link MailSendStatusEnum}
     */
    private Integer sendStatus;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * 发送返回的消息 ID
     */
    private String sendMessageId;
    /**
     * 发送异常
     */
    private String sendException;

}
