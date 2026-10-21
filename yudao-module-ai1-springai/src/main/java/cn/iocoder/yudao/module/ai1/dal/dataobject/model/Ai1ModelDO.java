package cn.iocoder.yudao.module.ai1.dal.dataobject.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 模型 DO
 *
 * @author 芋道源码
 */
@TableName("ai1_model")
@KeySequence("ai1_model_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1ModelDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 供应商编号
     *
     * 关联 {@link Ai1ProviderDO#getId()}
     */
    private Long providerId;
    /**
     * 展示名称
     */
    private String name;
    /**
     * 模型标识，例如说 gpt-4o、deepseek-chat
     */
    private String model;
    /**
     * 类型
     *
     * 枚举 {@link Ai1ModelTypeEnum}
     */
    private Integer type;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
