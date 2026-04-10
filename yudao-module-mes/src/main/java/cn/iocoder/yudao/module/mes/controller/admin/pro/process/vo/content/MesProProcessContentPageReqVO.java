package cn.iocoder.yudao.module.mes.controller.admin.pro.process.vo.content;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 生产工序内容分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProProcessContentPageReqVO extends PageParam {

    @Schema(description = "工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "工序编号不能为空")
    private Long processId;

}
