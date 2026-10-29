package cn.iocoder.yudao.module.iot.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

/**
 * 管理后台 - IoT 产品数据解析脚本调试 Request VO
 * <p>
 * 执行当前编辑内容：不保存、不发消息、不改属性
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 产品数据解析脚本调试 Request VO")
@Data
public class IotProductScriptDebugReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "产品编号不能为空")
    private Long id;

    @Schema(description = "数据解析脚本（JS ES5）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "脚本内容不能为空")
    private String scriptContent;

    @Schema(description = "方向：decode 上行解码，encode 下行编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "decode")
    @NotEmpty(message = "调试方向不能为空")
    @Pattern(regexp = "decode|encode", message = "调试方向只能是 decode 或 encode")
    private String direction;

    @Schema(description = "上行原始字节的 HEX（decode 时必填，允许空格）", example = "01 03 00 00")
    private String inputHex;

    @Schema(description = "下行指令（encode 时必填）：{method, requestId?, params?}")
    private Map<String, Object> message;

}
