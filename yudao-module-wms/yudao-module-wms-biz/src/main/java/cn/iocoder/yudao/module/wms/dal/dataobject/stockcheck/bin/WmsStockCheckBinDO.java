package cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 库位盘点 DO
 * @author 李方捷
 * @table-fields : actual_qty,bin_id,expected_qty,stockCheck_id,product_id,remark,id,status
 */
@TableName("wms_stock_check_bin")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stockCheck_bin_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockCheckBinDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 盘点结果单ID
     */
    private Long stockCheckId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 预期库存，仓位可用库存
     */
    private Integer expectedQty;

    /**
     * 仓位ID
     */
    private Long binId;

    /**
     * 实际库存，实盘数量
     */
    private Integer actualQty;

    /**
     * 备注
     */
    private String remark;

    /**
     * 盘点状态
     */
    private Integer status;
}
