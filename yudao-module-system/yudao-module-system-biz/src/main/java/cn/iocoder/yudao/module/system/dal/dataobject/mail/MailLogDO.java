package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.enums.mail.MailLogUserTypeEnum;
import cn.iocoder.yudao.module.system.enums.mail.MailSendStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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

    /**
     * 用户编码
     */
    private Long userId;

    /**
     * 用户类型
     *
     * 冗余 {@link MailLogUserTypeEnum#getUserType}
     */
    private Integer userType;

    // TODO @wangjingyi：accountId
    /**
     * 邮箱账号编号
     */
    private Long accountId;

    // TODO @wangjingyi：如果是冗余字段，记得 @ 下；DONE
    /**
     * 邮箱账号
     *
     * 冗余 {@link MailAccountDO}
     */
    private String fromAddress;

    /**
     * 模版主键
     */
    private Long templateId;

    /**
     * 模版内容
     */
    private String templateContent;

    /**
     * 基于 {@link MailTemplateDO#getParams()} 输入后的参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String,Object> templateParams;

    /**
     * 发送时间
     */
    private Date sendTime;

    //=========接收相关字段=========
    /**
     * 发送状态
     *
     * 枚举 {@link MailSendStatusEnum}
     */
    private Integer sendStatus;

    /**
     * 发送结果
     */
    private String sendResult;

    /**
     *  消息ID
     */
    private String messageId;


}
