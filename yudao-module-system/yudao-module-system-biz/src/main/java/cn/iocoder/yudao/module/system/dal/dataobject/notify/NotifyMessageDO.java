package cn.iocoder.yudao.module.system.dal.dataobject.notify;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

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
     */
    private Long templateId;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
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
     * 是否已读 0-未读  1-已读
     *
     * 枚举 {@link TODO system_notify_message_read_status 对应的类}
     */
    private Boolean readStatus;
    /**
     * 阅读时间
     */
    private Date readTime;

}
