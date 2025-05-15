package cn.iocoder.yudao.module.oms.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * OMS订单备份表 DO
 *
 * @author 谷毛毛
 */
@TableName("oms_order_raw")
@KeySequence("oms_order_raw_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsOrderRawDO extends TenantBaseDO {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /***
     *订单id
     */
    private Long orderId;

    /***
     *订单原数据
     */
    private String data;
}
