package cn.iocoder.yudao.module.ai.controller.admin.ppt.vo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptModeEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptSourceEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Objects;

/**
 * AI PPT 生成 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI PPT 生成 Request VO")
@Data
public class AiPptGenerateReqVO {

    @Schema(description = "素材来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "素材来源不能为空")
    @InEnum(AiPptSourceEnum.class)
    private Integer source;

    @Schema(description = "大纲内容（粘贴大纲）", example = "# 新能源汽车市场分析")
    @Size(max = 25000, message = "大纲内容不能超过 25000 个字符")
    private String content;

    @Schema(description = "材料文件地址（上传材料）", example = "https://www.iocoder.cn/report.pdf")
    @Size(max = 1024, message = "材料文件地址不能超过 1024 个字符")
    private String fileUrl;

    @Schema(description = "风格与设计要求", example = "极简商务风")
    @Size(max = 1024, message = "风格与设计要求不能超过 1024 个字符")
    private String prompt;

    @Schema(description = "格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "格式不能为空")
    @InEnum(AiPptModeEnum.class)
    private Integer mode;

    @Schema(description = "语言", requiredMode = Schema.RequiredMode.REQUIRED, example = "zh-CN")
    @NotEmpty(message = "语言不能为空")
    @Pattern(regexp = "zh-CN|en", message = "语言只支持 zh-CN、en")
    private String language;

    @Schema(description = "页数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "页数不能为空")
    @Min(value = 3, message = "页数不能少于 3 页")
    @Max(value = 30, message = "页数不能超过 30 页")
    private Integer pageCount;

    @Schema(description = "绘图模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    @NotNull(message = "绘图模型不能为空")
    private Long imageModelId;

    @AssertTrue(message = "请输入大纲内容")
    @JsonIgnore
    public boolean isContentValid() {
        return !Objects.equals(source, AiPptSourceEnum.PASTE.getSource()) || StrUtil.isNotBlank(content);
    }

    @AssertTrue(message = "请上传材料文件")
    @JsonIgnore
    public boolean isFileUrlValid() {
        return !Objects.equals(source, AiPptSourceEnum.UPLOAD.getSource()) || StrUtil.isNotBlank(fileUrl);
    }

}
