package cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FMS 辅助核算项目分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 辅助核算项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class FmsAuxiliaryItemPageReqVO extends PageParam {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "辅助核算类别编号不能为空")
    private Long auxiliaryTypeId;

    @Schema(description = "关键词，匹配编码或名称", example = "测试")
    private String search;

}
