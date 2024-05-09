package cn.iocoder.yudao.module.ai.controller.admin.model.vo.apikey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - AI API 密钥新增/修改 Request VO")
@Data
public class AiApiKeySaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23538")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "文心一言")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "密钥", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABC")
    @NotEmpty(message = "密钥不能为空")
    private String apiKey;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "OpenAI")
    @NotEmpty(message = "平台不能为空")
    private String platform;

    @Schema(description = "自定义 API 地址", example = "https://aip.baidubce.com")
    private String url;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}