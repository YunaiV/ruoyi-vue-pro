package cn.iocoder.yudao.module.tms.api.transfer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨出库请求 DTO
 * @author jisencai
 */
@Data
public class TmsOutboundReqDTO {

    /**
     * 出库单编号
     */
    @NotNull(message = "出库单编号不能为空")
    private Long id;

    /**
     * 出库单编码
     */
    private String code;

    /**
     * 上游单据类型
     */
    private Integer upstreamType;

    /**
     * 上游单据编号
     */
    @NotNull(message = "上游单据编号不能为空")
    private Long upstreamId;

    /**
     * 上游单据编码
     */
    private String upstreamCode;
    /**
     * 出库时间
     */
    @NotNull(message = "出库时间不能为空")
    private LocalDateTime outboundTime;

    /**
     * 出库状态
     */
    @NotNull(message = "出库状态不能为空")
    private Integer outboundStatus;
    /**
     * 出库单明细列表
     */
    @Valid
    @NotEmpty(message = "出库单明细列表不能为空")
    @Size(min = 1, message = "出库单明细列表长度不能小于 1")
    private List<TmsOutboundItemReqDTO> items;
} 