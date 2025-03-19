package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 供应商产品 DO
 *
 * @author 索迈管理员
 */
@TableName("erp_supplier_product")
@KeySequence("erp_supplier_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSupplierProductDO extends BaseDO {

    /**
     * 供应商产品编号
     */
    @TableId
    private Long id;
    /**
     * 供应商产品编码
     */
    private String code;
    /**
     * 供应商编号
     */
    private Long supplierId;
    /**
     * 产品编号
     */
    private Long productId;
    /**
     * 包装高度
     */
    private Double packageHeight;
    /**
     * 包装长度
     */
    private Double packageLength;
    /**
     * 包装重量
     */
    private Double packageWeight;
    /**
     * 包装宽度
     */
    private Double packageWidth;
    /**
     * 采购价格
     */
    private Double purchasePrice;
    /**
     * 采购货币代码
     */
    private Integer purchasePriceCurrencyCode;

}