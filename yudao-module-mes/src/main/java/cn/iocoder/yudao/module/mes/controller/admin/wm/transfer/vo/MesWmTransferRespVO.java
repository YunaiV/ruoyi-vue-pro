package cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 转移单 Response VO")
@Data
public class MesWmTransferRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "转移单编号", example = "TR2026020001")
    private String code;

    @Schema(description = "转移单名称", example = "钢板转移单")
    private String name;

    @Schema(description = "转移单类型", example = "1")
    private Integer type;

    @Schema(description = "是否配送", example = "true")
    private Boolean deliveryFlag;

    @Schema(description = "收货人", example = "张三")
    private String recipientName;

    @Schema(description = "联系方式", example = "13800138000")
    private String recipientTelephone;

    @Schema(description = "目的地", example = "上海市浦东新区")
    private String destinationAddress;

    @Schema(description = "承运商", example = "顺丰速运")
    private String carrier;

    @Schema(description = "运输单号", example = "SF123456789")
    private String shippingNumber;

    @Schema(description = "是否已确认", example = "false")
    private Boolean confirmFlag;

    @Schema(description = "转移日期")
    private LocalDateTime transferDate;

    @Schema(description = "单据状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

}
