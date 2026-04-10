package cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 生产流转卡分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProCardPageReqVO extends PageParam {

    @Schema(description = "流转卡编码", example = "CARD-001")
    private String code;

    @Schema(description = "生产工单编号", example = "100")
    private Long workOrderId;

    @Schema(description = "产品物料编号", example = "200")
    private Long itemId;

    @Schema(description = "批次号", example = "BATCH-001")
    private String batchCode;


}
