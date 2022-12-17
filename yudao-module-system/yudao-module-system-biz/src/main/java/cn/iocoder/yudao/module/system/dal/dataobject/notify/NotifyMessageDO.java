package cn.iocoder.yudao.module.system.dal.dataobject.notify;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * 站内信 DO
 *
 * @author xrcoder
 */
@TableName("system_notify_message")
@KeySequence("system_notify_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyMessageDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 站内信模版编号
     *
     * 关联 {@link NotifyTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 站内信模版编码
     *
     * 关联 {@link NotifyTemplateDO#getCode()}
     */
    private String templateCode;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 字段、或者 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 发送时间
     */
    private Date sendTime;
    /**
     * 发送用户id
     */
    private Long sendUserId;
    /**
     * 发送用户名
     */
    private String sendUserName;
    /**
     * 是否已读
     */
    private Boolean readStatus;
    /**
     * 阅读时间
     */
    private Date readTime;

}
