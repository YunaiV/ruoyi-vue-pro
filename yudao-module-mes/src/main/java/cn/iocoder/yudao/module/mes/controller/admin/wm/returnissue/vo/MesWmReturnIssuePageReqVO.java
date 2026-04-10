package cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - MES 生产退料单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmReturnIssuePageReqVO extends PageParam {

    @Schema(description = "退料单编号", example = "RI20250226001")
    private String code;

    @Schema(description = "退料单名称", example = "生产退料")
    private String name;

    @Schema(description = "工作站 ID", example = "1")
    private Long workstationId;

    @Schema(description = "生产工单 ID", example = "1")
    private Long workOrderId;

    @Schema(description = "退料类型", example = "1")
    private Integer type;

}
