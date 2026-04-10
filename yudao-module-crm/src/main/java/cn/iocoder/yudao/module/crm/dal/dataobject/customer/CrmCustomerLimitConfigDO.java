package cn.iocoder.yudao.module.crm.dal.dataobject.customer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.List;

/**
 * 客户限制配置 DO
 *
 * @author Wanwan
 */
@TableName(value = "crm_customer_limit_config", autoResultMap = true)
@KeySequence("crm_customer_limit_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerLimitConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 规则类型
     * <p>
     * 枚举 {@link CrmCustomerLimitConfigTypeEnum}
     */
    private Integer type;
    /**
     * 规则适用人群
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> userIds;
    /**
     * 规则适用部门
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> deptIds;
    /**
     * 数量上限
     */
    private Integer maxCount;
    /**
     * 成交客户是否占有拥有客户数
     *
     * 当且仅当 {@link #type} 为 1 时，进行使用
     */
    private Boolean dealCountEnabled;

}
