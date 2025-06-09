package cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;

/**
 * 头程单明细 DO
 *
 * @author wdy
 */
@TableName("tms_first_mile_item")
@KeySequence("tms_first_mile_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsFirstMileItemDO extends TenantBaseDO {

    /**
     * 明细ID
     */
    @TableId
    private Long id;
    /**
     * 头程主表ID
     */
    private Long firstMileId;
    /**
     * 乐观锁
     */
    @Version
    private Integer revision;
    /**
     * 申请项ID
     */
    private Long requestItemId;
    /**
     * 产品ID
     */
    private Long productId;
    /**
     * FBA条码
     */
    private String fbaBarCode;
    /**
     * 件数
     */
    private Integer qty;
    /**
     * 箱数
     */
    private Integer boxQty;
    /**
     * 库存公司
     */
    private Long companyId;
    /**
     * 库存归属部门ID
     */
    private Long deptId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 实际发货数(回填)
     */
    private Integer outboundClosedQty;
    /**
     * 计划发货数
     */
    private Integer outboundPlanQty;
    /**
     * 已入库数量(回填)
     */
    private Integer inboundClosedQty;
    /**
     * 发出仓ID
     */
    private Long fromWarehouseId;
    /**
     * 包装长（cm）
     */
    private BigDecimal packageLength;
    /**
     * 包装宽（cm）
     */
    private BigDecimal packageWidth;
    /**
     * 包装高（cm）
     */
    private BigDecimal packageHeight;
    /**
     * 毛重（kg）
     */
    private BigDecimal packageWeight;
    /**
     * 体积（m³）
     */
    private BigDecimal volume;
    /**
     * 销售公司ID
     */
    private Long salesCompanyId;
}