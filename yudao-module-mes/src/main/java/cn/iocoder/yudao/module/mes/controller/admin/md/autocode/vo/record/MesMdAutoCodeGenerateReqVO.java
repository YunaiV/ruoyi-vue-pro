package cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - MES 编码生成 Request VO")
@Data
public class MesMdAutoCodeGenerateReqVO {

    @Schema(description = "规则编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ITEM_CODE")
    @NotEmpty(message = "规则编码不能为空")
    private String ruleCode;

    @Schema(description = "输入字符", example = "A")
    private String inputChar;

}
