package cn.iocoder.yudao.module.tms.api.transfer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调拨出库明细请求 DTO
 */
@Data
public class TmsOutboundItemReqDTO {

    /**
     * 调拨出库明细编号
     */
    @NotNull(message = "调拨出库明细编号不能为空")
    private Long id;

    /**
     * 上游单据明细编号
     */
    @NotNull(message = "上游单据明细编号不能为空")
    private Long upstreamId;

    /**
     * 实际出库数量
     */
    @NotNull(message = "实际出库数量不能为空")
    private Double actualQty;
} 