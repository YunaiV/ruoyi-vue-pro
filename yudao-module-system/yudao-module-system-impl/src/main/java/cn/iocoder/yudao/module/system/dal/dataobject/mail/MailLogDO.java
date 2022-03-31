package cn.iocoder.yudao.module.system.dal.dataobject.mail;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import java.sql.Timestamp;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 邮箱日志
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@TableName(value = "system_mail_log", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MailLogDO extends BaseDO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 邮箱账号编号
     */
    private String accountCode;

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
    private Boolean sendStatus;

    /**
     * 发送结果
     */
    private String sendResult;


}
