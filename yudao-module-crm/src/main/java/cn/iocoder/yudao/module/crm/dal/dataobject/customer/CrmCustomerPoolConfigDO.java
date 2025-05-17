package cn.iocoder.yudao.module.crm.dal.dataobject.customer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 客户公海配置 DO
 *
 * @author Wanwan
 */
@TableName(value = "crm_customer_pool_config")
@KeySequence("crm_customer_pool_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerPoolConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 是否启用客户公海
     */
    private Boolean enabled;
    /**
     * 未跟进放入公海天数
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer contactExpireDays;
    /**
     * 未成交放入公海天数
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer dealExpireDays;
    /**
     * 是否开启提前提醒
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Boolean notifyEnabled;
    /**
     * 提前提醒天数
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer notifyDays;

}
