package cn.iocoder.yudao.module.product.dal.dataobject.spu;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品spu DO
 *
 * @author 芋道源码
 */
@TableName("product_spu")
@KeySequence("product_spu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpuDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 卖点
     */
    private String sellPoint;
    /**
     * 描述
     */
    private String description;
    /**
     * 分类id
     */
    private Long categoryId;
    /**
     * 商品主图地址,* 数组，以逗号分隔,最多上传15张
     */
    // TODO franky：List<String>。可以参考别的模块，怎么处理这种类型的哈
    private String picUrls;
    /**
     * 排序字段
     */
    private Integer sort;
    /**
     * 点赞初始人数
     */
    private Integer likeCount;
    /**
     * 价格 单位使用：分
     */
    private Integer price;
    /**
     * 库存数量
     */
    private Integer quantity;
    /**
     * 上下架状态： 0 上架（开启） 1 下架（禁用）
     */
    private Boolean status;

}
