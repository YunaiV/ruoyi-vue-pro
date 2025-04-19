package cn.iocoder.yudao.module.oms.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * OMS店铺表
 */
@TableName("oms_shop")
@KeySequence("oms_shop_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsShopDO extends TenantBaseDO {
    /**
     * 店铺id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 店铺别名
     */
    private String name;

    /**
     * 外部来源名称
     */
    private String externalName;

    /**
     * 店铺编码
     */
    private String code;

    /**
     * 平台店铺编码
     */
    private String platformShopCode;


    /**
     * 平台名称
     */
    private String platformCode;

    /**
     * 店铺类型
     */
    private Integer type;
}