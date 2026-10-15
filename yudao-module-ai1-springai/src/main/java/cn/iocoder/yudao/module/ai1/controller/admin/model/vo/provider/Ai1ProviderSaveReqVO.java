package cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai1.util.Ai1Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI1 供应商新增/修改 Request VO")
@Data
public class Ai1ProviderSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "DeepSeek")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 50, message = "名称长度不能超过 50 个字符")
    private String name;

    @Schema(description = "接口地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://api.deepseek.com/v1")
    @NotEmpty(message = "接口地址不能为空")
    @Size(max = 200, message = "接口地址长度不能超过 200 个字符")
    private String baseUrl;

    @Schema(description = "API 密钥；修改时为空表示保持原密钥不变", example = "sk-xxx")
    @Size(max = 200, message = "API 密钥长度不能超过 200 个字符")
    private String apiKey;

    @Schema(description = "请求 Header，JSON 对象", example = "{\"X-Session\":\"{session}\"}")
    private Map<String, String> headers;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注", example = "官方接口")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

    @AssertTrue(message = "请求 Header 名称不能为空或重复，值必须为字符串，JSON 长度不能超过 2000 个字符")
    @JsonIgnore
    public boolean isHeadersValid() {
        return Ai1Utils.isHeadersValid(headers);
    }

}
