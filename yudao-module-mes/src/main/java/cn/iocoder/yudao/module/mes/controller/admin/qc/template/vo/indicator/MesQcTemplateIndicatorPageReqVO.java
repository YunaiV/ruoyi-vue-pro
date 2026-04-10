package cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.indicator;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 质检方案-检测指标项分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcTemplateIndicatorPageReqVO extends PageParam {

    @Schema(description = "质检方案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检方案ID不能为空")
    private Long templateId;

}
