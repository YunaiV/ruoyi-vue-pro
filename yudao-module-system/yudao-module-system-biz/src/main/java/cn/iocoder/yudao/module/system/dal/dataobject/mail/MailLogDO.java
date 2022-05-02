package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.enums.mail.MailSendStatusEnum;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 邮箱日志
 * 记录每一次邮件的发送
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@TableName(value = "system_mail_log", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailLogDO extends BaseDO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    // TODO @wangjingyi：accountId
    /**
     * 邮箱账号编号
     */
    private Long accountId;

    // TODO @wangjingyi：如果是冗余字段，记得 @ 下；
    /**
     * 邮箱账号
     */
    private String from;

    /**
     * 模版主键
     */
    private Long templateId;

    /**
     * 模版编号
     */
    private String templateCode;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 收件人
     */
    private String to;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 发送状态
     *
     * 枚举 {@link MailSendStatusEnum}
     */
    private Integer sendStatus;

    /**
     * 发送结果
     *
     */
    private String sendResult;


}
