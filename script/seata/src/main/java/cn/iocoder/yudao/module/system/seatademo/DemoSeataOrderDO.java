package cn.iocoder.yudao.module.system.seatademo;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 分布式事务示例订单 DO
 *
 * 位于 yudao_seata_order 演示库，对应 seata_order 数据源
 *
 * @author 芋道源码
 */
@TableName("demo_seata_order")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoSeataOrderDO extends TenantBaseDO {

    /**
     * 订单编号
     *
     * 由调用方传入，作为幂等主键：重复的下单请求会因主键冲突失败，不会重复扣减余额
     */
    @TableId(type = IdType.INPUT)
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 订单金额，单位：分
     */
    private Integer amount;

}
