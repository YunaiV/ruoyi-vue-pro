package cn.iocoder.yudao.module.ai.controller.admin.ppt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI PPT 切换单页图片版本 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI PPT 切换单页图片版本 Request VO")
@Data
public class AiPptSlideImageReqVO {

    @Schema(description = "PPT 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "PPT 编号不能为空")
    private Long id;

    @Schema(description = "页码，从 1 开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "页码不能为空")
    private Integer slideIndex;

    @Schema(description = "图片地址，必须是该页的历史版本之一", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://www.iocoder.cn/ppt/v1.png")
    @NotEmpty(message = "图片地址不能为空")
    private String imageUrl;

}
