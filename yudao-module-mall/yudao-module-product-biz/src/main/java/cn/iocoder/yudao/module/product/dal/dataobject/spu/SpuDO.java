package cn.iocoder.yudao.module.product.dal.dataobject.spu;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

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
public class SpuDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 上下架状态： true 上架，false 下架
     */
    private Boolean visible;
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
    private Integer cid;
    /**
     * 列表图
     */
    private String listPicUrl;
    /**
     * 商品主图地址, 数组，以逗号分隔, 最多上传15张
     */
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
     * 价格
     */
    private Integer price;
    /**
     * 库存数量
     */
    private Integer quantity;

}
