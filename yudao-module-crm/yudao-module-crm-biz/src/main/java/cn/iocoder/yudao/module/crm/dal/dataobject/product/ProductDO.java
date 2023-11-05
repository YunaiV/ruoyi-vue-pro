package cn.iocoder.yudao.module.crm.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 产品 DO
 *
 * @author ZanGe丶
 */
@TableName("crm_product")
@KeySequence("crm_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDO extends BaseDO {

    /**
     * 主键 id
     */
    @TableId
    private Long id;
    /**
     * 产品名称
     */
    private String name;
    /**
     * 产品编码
     */
    private String no;
    /**
     * 单位
     */
    private String unit;
    /**
     * 价格
     */
    private Long price;
    /**
     * 状态
     *
     * 枚举 {@link TODO crm_product_status 对应的类}
     * // TODO @zange：这个写个枚举类，然后 {@link关联下
     */
    private Integer status;
    /**
     * 产品分类 ID
     * // TODO @zange：这个要写下关联 CategoryDO 的 id 字段；参考下别的模块哈
     */
    private Long categoryId;
    /**
     * 产品描述
     */
    private String description;
    /**
     * 负责人的用户编号
     */
    private Long ownerUserId;

}
