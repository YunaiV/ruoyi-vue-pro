package cn.iocoder.yudao.module.mes.controller.admin.qc.pendinginspect.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 待检任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcPendingInspectPageReqVO extends PageParam {

    @Schema(description = "来源单据编号", example = "AN2025001")
    private String sourceDocCode;

    @Schema(description = "检验类型", example = "1")
    private Integer qcType;

    @Schema(description = "物料编号", example = "1024")
    private Long itemId;

}
