package cn.iocoder.yudao.module.product.dal.dataobject.property;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @franky：每个表名，都加个 Product 前缀哈。
/**
 * 规格名称 DO
 *
 * @author 芋道源码
 */
@TableName("product_property")
@KeySequence("product_property_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPropertyDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 规格名称
     */
    private String name;
    /**
     * 状态： 0 开启 ，1 禁用
     *
     * TODO @franky：加个 枚举 {@link CommonStatusEnum} ，这样就能更好的知道
     */
    private Integer status;

}
