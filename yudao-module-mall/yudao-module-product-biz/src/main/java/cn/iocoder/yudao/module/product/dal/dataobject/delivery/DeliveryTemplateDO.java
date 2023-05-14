package cn.iocoder.yudao.module.product.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @Jason：配送放到 trade 里。然后属于 deliver 配送；配送分成两个方式：1）快递 express；2）自提 pickup；
// 这样的话，实体名字一个是 DeliveryExpressTemplateDO；长一点没关系哈；还有一个 DeliveryPickUpStoreDO 自提门店；
// 表名的话，还是加上 trade_delivery_ 前缀，主要归属在交易域

// TODO @Jason：额外补充，不是这个类哈。应该还有个快递；DeliveryExpress；需要设计下这个表

/**
 * 配送模板 SPU DO
 *
 * @author 芋道源码
 */
@TableName("delivery_template")
@KeySequence("delivery_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTemplateDO extends BaseDO {

    /**
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    // TODO @Jason 我看了下，crmeb 界面是假的，没有全国包邮、部分包邮、自定义；直接干掉这个字段号了；没啥用。
    /**
     * 配送费用类型 1:全区域包邮 2：非全区域包邮
     */
    private Integer chargeType;

    // TODO @Jason：1:按件 2:按重量 3:按体积 枚举，然后关联下
    /**
     * 配送计费方式 1:按件 2:按重量 3:按体积
     */
    private Integer chargeMode;

    /**
     * 排序
     */
    private Integer sort;

}
