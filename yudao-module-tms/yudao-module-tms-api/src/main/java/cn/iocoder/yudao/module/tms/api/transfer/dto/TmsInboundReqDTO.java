package cn.iocoder.yudao.module.tms.api.transfer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨入库单 DTO
 */
@Data
@Validated
public class TmsInboundReqDTO {

    /**
     * 主键
     */
    @NotNull(message = "调拨入库单编号审核回调主键ID不能为空")
    private Long id;

    /**
     * 单据号
     */
    @NotBlank(message = "调拨入库单编号审核回调不能为空")
    private String code;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 入库状态
     */
    private Integer inboundStatus;

    /**
     * 库存财务公司ID
     */
    private Long companyId;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

    /**
     * 来源单据ID
     */
    @NotNull(message = "调拨入库单编号审核回调来源单据ID不能为空")
    private Long upstreamId;

    /**
     * 来源单据编码
     */
    private String upstreamCode;

    /**
     * 来源单据类型
     */
    private Integer upstreamType;

    /**
     * 上架状态
     */
    private Integer shelveStatus;

    /**
     * 入库单明细列表
     */
    @Size(min = 1, message = "调拨入库单编号审核回调,明细列表至少有一个")
    @NotEmpty(message = "调拨入库单编号审核回调,明细列表不能为空")
    private List<@Valid TmsInboundItemReqDTO> itemList;
} 