package cn.iocoder.yudao.module.crm.dal.dataobject.contract;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * CRM 合同产品关联表 DO
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
     * 合同编号
     *
     * 关联 {@link CrmContractDO#getId()}
     */
    private Long contractId;
    /**
     * 产品编号
     *
     * 关联 {@link CrmProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品单价，单位：元
     */
    private BigDecimal productPrice;
    /**
     * 合同价格, 单位：元
     */
    private BigDecimal contractPrice;
    /**
     * 数量
     */
    private BigDecimal count;
    /**
     * 总计价格，单位：元
     *
     * totalPrice = businessPrice * count
     */
    private BigDecimal totalPrice;

}
