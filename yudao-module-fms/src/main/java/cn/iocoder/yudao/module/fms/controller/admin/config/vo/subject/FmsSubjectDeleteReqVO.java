package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * FMS 科目删除 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 科目删除 Request VO")
@Data
public class FmsSubjectDeleteReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "科目编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "科目编号数组不能为空")
    private List<Long> ids;

}
