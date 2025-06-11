package cn.iocoder.yudao.module.tms.api.first.mile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 头程单明细 DTO
 *
 * @author wdy
 */
@Data
@Accessors(chain = true)
public class TmsFistMileItemUpdateDTO {

    /**
     * 明细行ID
     */
    @NotNull(message = "头程单明细行ID不能为空")
    private Long id;

    /**
     * 出库 = false, 入库 = true
     */
    private Boolean InOutType;
    // ==================== 出库信息 ====================
    /**
     * 出库单ID
     */
    private Long outboundId;
    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;
    /**
     * 出库状态(暂时留着)
     */
    private Integer outboundStatus;
    /**
     * 出库数量
     */
    private Integer outboundQty;

    // ==================== 入库信息 ====================
    /**
     * 入库单ID
     */
    private Long inboundId;
    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;
    /**
     * 入库状态(暂时留着)
     */
    private Integer inboundStatus;
    /**
     * 入库数量
     */
    private Integer inboundQty;
} 