package cn.iocoder.yudao.module.crm.dal.dataobject.contract;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 合同产品关联表 DO
 *
 * @author HUIHUI
 */
@TableName("crm_contract_product")
@KeySequence("crm_contract_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmContractProductDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 产品编号
     *
     * 关联 {@link CrmProductDO#getId()}
     */
    private Long productId;
    /**
     * 合同编号
     *
     * 关联 {@link CrmContractDO#getId()}
     */
    private Long contractId;
    /**
     * 产品单价
     */
    private Integer price;
    /**
     * 销售价格, 单位：分
     */
    private Integer salesPrice;
    /**
     * 数量
     */
    private Integer count;
    /**
     * 折扣
     */
    private Integer discountPercent;
    /**
     * 总计价格（折扣后价格）
     *
     * TODO @puhui999：可以写下计算公式哈；
     */
    private Integer totalPrice;

}
