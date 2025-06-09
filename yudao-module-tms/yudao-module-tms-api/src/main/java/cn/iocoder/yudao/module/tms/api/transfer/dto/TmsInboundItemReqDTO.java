package cn.iocoder.yudao.module.tms.api.transfer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * 调拨入库单明细 DTO
 */
@Data
@Validated
public class TmsInboundItemReqDTO {

    /**
     * 主键
     */
    @NotNull(message = "调拨入库单编号审核回调明细主键不能为空")
    private Long id;

    /**
     * 入库单明细ID
     */
    private Long inboundId;

    /**
     * 实际入库量
     */
    @NotNull(message = "调拨入库单编号审核回调明细实际入库量不能为空")
    private Integer actualQty;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源明细行ID
     */
    @NotNull(message = "调拨入库单编号审核回调明细上游ID不能为空")
    private Long upstreamId;
} 