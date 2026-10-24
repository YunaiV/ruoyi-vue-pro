package cn.iocoder.yudao.module.infra.shardingdemo;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 分库分表示例订单 DO
 *
 * 逻辑表 demo_sharding_order，按 userId 分布到 2 个库 × 每库 2 张表，规则见 sharding/sharding.yaml
 *
 * @author 芋道源码
 */
@TableName("demo_sharding_order")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoShardingOrderDO extends TenantBaseDO {

    /**
     * 编号
     *
     * 分库分表后无法使用数据库自增，由 MyBatis Plus 的雪花算法生成全局唯一编号
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 用户编号，即分片键
     *
     * 新增时必须有值，创建后不允许修改
     */
    private Long userId;
    /**
     * 订单名称
     */
    private String name;

}
