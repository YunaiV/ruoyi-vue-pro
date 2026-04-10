package cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 到货通知单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmArrivalNoticeLinePageReqVO extends PageParam {

    @Schema(description = "到货通知单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "到货通知单编号不能为空")
    private Long noticeId;

}
