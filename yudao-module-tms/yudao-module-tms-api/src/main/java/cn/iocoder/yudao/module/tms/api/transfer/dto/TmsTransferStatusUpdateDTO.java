package cn.iocoder.yudao.module.tms.api.transfer.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 调拨单状态更新 DTO
 */
@Data
@Accessors(chain = true)
public class TmsTransferStatusUpdateDTO {

    /**
     * 调拨单编号
     */
    private Long id;

    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;
    /**
     * 出库状态
     */
    private Integer outboundStatus;
    /**
     * 出库单ID
     */
    private Long outboundId;
    /**
     * 出库单编码
     */
    private String outboundCode;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;
    /**
     * 入库状态
     */
    private Integer inboundStatus;
    /**
     * 入库单ID
     */
    private Long inboundId;
    /**
     * 入库单编码
     */
    private String inboundCode;
} 