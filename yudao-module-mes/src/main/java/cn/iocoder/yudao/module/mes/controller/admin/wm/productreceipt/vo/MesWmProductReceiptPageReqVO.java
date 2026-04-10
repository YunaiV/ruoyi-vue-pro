package cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 产品收货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmProductReceiptPageReqVO extends PageParam {

    @Schema(description = "收货单编码", example = "PR202603010001")
    private String code;

    @Schema(description = "收货单名称", example = "产品收货单-工单001")
    private String name;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "产品物料编号", example = "1")
    private Long itemId;

}
