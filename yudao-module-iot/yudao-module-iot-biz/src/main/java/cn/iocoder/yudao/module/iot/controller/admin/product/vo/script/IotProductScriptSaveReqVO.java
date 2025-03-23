package cn.iocoder.yudao.module.iot.controller.admin.product.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品脚本信息新增/修改 Request VO")
@Data
public class IotProductScriptSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26565")
    private Long id;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28277")
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "产品唯一标识符", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品唯一标识符不能为空")
    private String productKey;

    @Schema(description = "脚本类型(property_parser=属性解析,event_parser=事件解析,command_encoder=命令编码)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "脚本类型(property_parser=属性解析,event_parser=事件解析,command_encoder=命令编码)不能为空")
    private String scriptType;

    @Schema(description = "脚本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "脚本内容不能为空")
    private String scriptContent;

    @Schema(description = "脚本语言", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "脚本语言不能为空")
    private String scriptLanguage;

    @Schema(description = "状态(0=禁用 1=启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态(0=禁用 1=启用)不能为空")
    private Integer status;

    @Schema(description = "备注说明", example = "你说的对")
    private String remark;

}