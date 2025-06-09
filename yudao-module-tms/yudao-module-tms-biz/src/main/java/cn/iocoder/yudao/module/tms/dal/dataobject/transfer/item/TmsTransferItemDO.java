package cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;

/**
 * 调拨单明细 DO
 *
 * @author wdy
 */
@TableName("tms_transfer_item")
@KeySequence("tms_transfer_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTransferItemDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 关联调拨单ID
     */
    private Long transferId;
    /**
     * 乐观锁
     */
    @Version
    private Integer revision;
    /**
     * 产品id
     */
    private Long productId;
    /**
     * 数量
     */
    private Integer qty;
    /**
     * 箱数
     */
    private Integer boxQty;
    /**
     * 包装重量（kg）
     */
    private BigDecimal packageWeight;
    /**
     * 包装体积（m³）
     */
    private BigDecimal packageVolume;
    /**
     * 库存公司ID
     */
    private Long stockCompanyId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 实际发货数
     */
    private Integer outboundClosedQty;
    /**
     * 已入库数
     */
    private Integer inboundClosedQty;
    /**
     * 库存归属部门ID
     */
    private Long deptId;
}