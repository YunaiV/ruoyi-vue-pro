package cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.iteration.PmsIterationStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - PMS 项目迭代分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsIterationPageReqVO extends PageParam {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "迭代名称", example = "第一期")
    private String name;

    @Schema(description = "迭代状态", example = "1")
    @InEnum(PmsIterationStatusEnum.class)
    private Integer status;

}
