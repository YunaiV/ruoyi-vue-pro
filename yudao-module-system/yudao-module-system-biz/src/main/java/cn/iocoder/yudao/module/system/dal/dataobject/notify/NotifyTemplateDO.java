package cn.iocoder.yudao.module.system.dal.dataobject.notify;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 站内信模版 DO
 *
 * @author xrcoder
 */
@TableName("system_notify_template")
@KeySequence("system_notify_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyTemplateDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 模版编码
     */
    private String code;
    /**
     * 模版标题
     */
    private String title;
    /**
     * 模版内容
     */
    private String content;
    /**
     * 参数数组
     */
    private String params;
    /**
     * 状态：1-启用 0-禁用
     *
     * 枚举 {@link TODO common_status 对应的类}
     */
    private String status;
    /**
     * 备注
     */
    private String remarks;

}
