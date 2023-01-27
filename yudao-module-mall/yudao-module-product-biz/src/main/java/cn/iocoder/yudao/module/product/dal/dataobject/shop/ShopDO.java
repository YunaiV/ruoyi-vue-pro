package cn.iocoder.yudao.module.product.dal.dataobject.shop;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO 芋艿：待设计
/**
 * 店铺 DO
 *
 * @author 芋道源码
 */
@TableName("shop")
@KeySequence("shop_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDO extends BaseDO {

    private Long id;

}
