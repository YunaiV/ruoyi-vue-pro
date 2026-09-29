package cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.provider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 供应商 Response VO")
@Data
public class Ai1ProviderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "DeepSeek")
    private String name;

    @Schema(description = "接口地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://api.deepseek.com/v1")
    private String baseUrl;

    @Schema(description = "API 密钥，脱敏展示", example = "sk-****abcd")
    private String apiKey;

    @Schema(description = "请求附属 Header，JSON 数组", example = "[{\"key\":\"X-Session\",\"value\":\"{session}\"}]")
    private String headers;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "官方接口")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
