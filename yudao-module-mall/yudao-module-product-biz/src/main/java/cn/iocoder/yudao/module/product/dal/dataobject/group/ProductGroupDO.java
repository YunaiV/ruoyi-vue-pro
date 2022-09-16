package cn.iocoder.yudao.module.product.dal.dataobject.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.enums.group.ProductGroupStyleEnum;
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
    /**
     * 分组名称
     */
    private String name;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 商品数量
     */
    private Integer count;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 风格，用于 APP 首页展示商品的样式
     *
     * 枚举 {@link ProductGroupStyleEnum}
     */
    private Integer style;
    /**
     * 是否默认
     *
     * true - 系统默认，不允许删除
     * false - 自定义，允许删除
     */
    private Boolean defaulted;

}
