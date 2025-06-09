package cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;

/**
 * 头程申请表明细 DO
 *
 * @author wdy
 */
@TableName("tms_first_mile_request_item")
@KeySequence("tms_first_mile_request_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsFirstMileRequestItemDO extends TenantBaseDO {

    /**
     * 明细编号
     */
    @TableId
    private Long id;
    /**
     * 所属申请单ID
     */
    private Long requestId;
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
     * FBA条码
     */
    private String fbaBarCode;
    /**
     * 申请数量
     */
    private Integer qty;
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
     * 订购状态
     */
    private Integer orderStatus;
    /**
     * 关闭状态
     */
    private Integer offStatus;
    /**
     * 已订购数
     */
    private Integer orderClosedQty;

    /**
     * 销售公司ID
     */
    private Long salesCompanyId;

    /**
     * 备注
     */
    private String remark;
}