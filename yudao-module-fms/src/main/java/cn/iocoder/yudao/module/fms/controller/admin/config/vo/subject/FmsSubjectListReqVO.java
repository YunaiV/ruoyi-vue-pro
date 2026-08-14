package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * FMS 科目列表 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 科目列表 Request VO")
@Data
public class FmsSubjectListReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "科目类型", example = "1")
    @InEnum(FmsSubjectTypeEnum.class)
    private Integer type;

}
