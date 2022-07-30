package cn.iocoder.yudao.module.product.dal.dataobject.group;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品分组 DO
 *
 * @author 芋道源码
 */
@TableName("product_group")
@KeySequence("product_group_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductGroupDO extends BaseDO {

    /**
     * 商品分组编号，自增
     */
    @TableId
    private Long id;

    // TODO 芋艿：字段补全

}
