package cn.iocoder.yudao.module.iot.controller.admin.product.vo.script;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductScriptTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品脚本测试 Request VO")
@Data
public class IotProductScriptTestReqVO {

    @Schema(description = "脚本ID，如果已保存脚本则传入", example = "1024")
    private Long id;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "脚本类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "脚本类型不能为空")
    @InEnum(value = IotProductScriptTypeEnum.class)
    private Integer scriptType;

    @Schema(description = "脚本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "脚本内容不能为空")
    private String scriptContent;

    @Schema(description = "脚本语言", requiredMode = Schema.RequiredMode.REQUIRED, example = "javascript")
    @NotEmpty(message = "脚本语言不能为空")
    private String scriptLanguage;

    @Schema(description = "测试输入数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "测试输入数据不能为空")
    private String testInput;

}