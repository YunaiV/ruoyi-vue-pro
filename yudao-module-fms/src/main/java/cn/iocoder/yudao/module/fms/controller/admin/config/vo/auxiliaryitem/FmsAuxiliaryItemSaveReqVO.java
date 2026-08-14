package cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * FMS 辅助核算项目创建或修改 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 辅助核算项目创建或修改 Request VO")
@Data
public class FmsAuxiliaryItemSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "辅助核算类别编号不能为空")
    private Long auxiliaryTypeId;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "KH001")
    @NotBlank(message = "编码不能为空")
    @Size(max = 64, message = "编码长度不能超过 64 个字符")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海测试数字科技有限公司")
    @NotBlank(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "备注", example = "重点客户")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    @Schema(description = "规格", example = "标准版")
    @Size(max = 255, message = "规格长度不能超过 255 个字符")
    private String specification;

    @Schema(description = "单位", example = "台")
    @Size(max = 255, message = "单位长度不能超过 255 个字符")
    private String unit;

}
