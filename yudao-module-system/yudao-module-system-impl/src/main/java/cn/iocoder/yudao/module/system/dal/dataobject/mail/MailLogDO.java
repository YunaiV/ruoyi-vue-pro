package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;

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
public class MailLogDO extends BaseDO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    // TODO @wangjingyi：accountId
    /**
     * 邮箱账号编号
     */
    private String accountCode;

    // TODO @wangjingyi：如果是冗余字段，记得 @ 下；
    /**
     * 邮箱账号
     */
    private String from;

    /**
     * 模版主键
     */
    private String templateId;

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
    private Timestamp sendTime;

    /**
     * 发送状态
     */
    // TODO @wangjingyi：四个状态，参考短信模块
    private Boolean sendStatus;

    /**
     * 发送结果
     */
    private String sendResult;


}
