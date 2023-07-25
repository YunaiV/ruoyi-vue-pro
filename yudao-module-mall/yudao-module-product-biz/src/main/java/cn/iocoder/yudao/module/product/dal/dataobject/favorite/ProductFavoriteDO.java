package cn.iocoder.yudao.module.product.dal.dataobject.favorite;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品收藏 DO
 *
 * @author 芋道源码
 */
@TableName("product_favorite")
@KeySequence("product_favorite_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFavoriteDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 商品 SPU 编号
     *
     * 关联 {@link ProductSpuDO#getId()}
     */
    private Long spuId;

}
