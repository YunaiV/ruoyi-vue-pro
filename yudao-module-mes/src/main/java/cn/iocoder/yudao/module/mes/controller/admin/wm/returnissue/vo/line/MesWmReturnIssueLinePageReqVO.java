package cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 生产退料单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmReturnIssueLinePageReqVO extends PageParam {

    @Schema(description = "退料单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "退料单编号不能为空")
    private Long issueId;

}
