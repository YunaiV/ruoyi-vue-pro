package cn.iocoder.yudao.module.tms.dal.dataobject.transfer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 调拨单 DO
 *
 * @author wdy
 */
@TableName("tms_transfer")
@KeySequence("tms_transfer_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTransferDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 乐观锁
     */
    @Version
    private Integer revision;
    /**
     * 调拨单编码
     */
    private String code;
    /**
     * 发出仓库ID
     */
    private Long fromWarehouseId;
    /**
     * 目的仓库ID
     */
    private Long toWarehouseId;
    /**
     * 审核人ID
     */
    private Long auditorId;
    /**
     * 审核状态
     */
    private Integer auditStatus;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 审核意见
     */
    private String auditAdvice;
    /**
     * 出库状态(wms的状态字典)
     */
    private Integer outboundStatus;
    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;
    /**
     * 入库状态(wms的状态字典)
     */
    private Integer inboundStatus;
    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 跟踪号
     */
    private String traceNo;
    /**
     * 总货值
     */
    private BigDecimal totalValue;
    /**
     * 总净重
     */
    private BigDecimal netWeight;
    /**
     * 总毛重
     */
    private BigDecimal totalWeight;
    /**
     * 总体积
     */
    private BigDecimal totalVolume;
    /**
     * 总件数
     */
    private Integer totalQty;
    /**
     * 出库单ID
     */
    private Long outboundId;
    /**
     * 出库单编码
     */
    private String outboundCode;
    /**
     * 入库单ID
     */
    private Long inboundId;
    /**
     * 入库单编码
     */
    private String inboundCode;

}