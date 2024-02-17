package cn.iocoder.yudao.module.erp.dal.dataobject.stock;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 产品库存明细 DO
 *
 * @author 芋道源码
 */
@TableName("erp_stock_record")
@KeySequence("erp_stock_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 产品编号
     *
     * 关联 {@link ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 仓库编号
     *
     * 关联 {@link ErpWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 出入库数量
     *
     * 正数，表示入库；负数，表示出库
     */
    private BigDecimal count;
    /**
     * 总库存量
     *
     * 出入库之后，目前的库存量
     */
    private BigDecimal totalCount;
    /**
     * 业务类型
     *
     * 枚举 {@link ErpStockRecordBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 业务编号
     *
     * 例如说：{@link ErpStockInDO#getId()}
     */
    private Long bizId;
    /**
     * 业务项编号
     *
     * 例如说：{@link ErpStockInItemDO#getId()}
     */
    private Long bizItemId;
    /**
     * 业务单号
     *
     * 例如说：{@link ErpStockInDO#getNo()}
     */
    private String bizNo;

}