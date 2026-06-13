package cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 发货通知单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmSalesNoticePageReqVO extends PageParam {

    @Schema(description = "通知单编码", example = "SN202603010001")
    private String code;

    @Schema(description = "通知单名称", example = "测试发货通知")
    private String name;

    @Schema(description = "销售订单编号", example = "SO202603010001")
    private String salesOrderCode;

    @Schema(description = "客户编号", example = "1")
    private Long clientId;

    @Schema(description = "状态", example = "3")
    private Integer status;

}
