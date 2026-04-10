package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - MES 销售退货相关
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 销售退货单 Response VO")
@Data
public class MesWmReturnSalesRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "RS202603010001")
    private String code;

    @Schema(description = "退货单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "销售退货")
    private String name;

    @Schema(description = "销售订单编号", example = "SO202603010001")
    private String salesOrderCode;

    @Schema(description = "客户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long clientId;

    @Schema(description = "客户编码", example = "C001")
    private String clientCode;

    @Schema(description = "客户名称", example = "XX客户")
    private String clientName;

    @Schema(description = "客户简称", example = "XX")
    private String clientNickname;

    @Schema(description = "退货日期")
    private LocalDateTime returnDate;

    @Schema(description = "退货原因", example = "质量问题")
    private String returnReason;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
