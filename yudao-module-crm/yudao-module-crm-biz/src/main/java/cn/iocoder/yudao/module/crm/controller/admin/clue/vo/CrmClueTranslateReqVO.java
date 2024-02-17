package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Schema(description = "管理后台 - 线索转化为客户 Request VO")
@Data
public class CrmClueTranslateReqVO {

    @Schema(description = "线索编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1024, 1025]")
    @NotEmpty(message = "线索编号不能为空")
    private Set<Long> ids;

}
