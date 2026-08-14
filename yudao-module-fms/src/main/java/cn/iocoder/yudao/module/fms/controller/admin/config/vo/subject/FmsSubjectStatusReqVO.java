package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * FMS 科目状态更新 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 科目状态更新 Request VO")
@Data
public class FmsSubjectStatusReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "科目编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "科目编号数组不能为空")
    private List<Long> ids;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
