package cn.iocoder.yudao.module.ai.controller.admin.ocr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台 - AI 文字识别 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 文字识别 Request VO")
@Data
public class AiOcrRecognizeReqVO {

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型不能为空")
    private Long modelId;

    @Schema(description = "图片文件地址，先上传到文件服务", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://www.iocoder.cn/invoice.png")
    @NotEmpty(message = "图片文件地址不能为空")
    @Size(max = 1024, message = "图片文件地址不能超过 1024 个字符")
    private String fileUrl;

}
