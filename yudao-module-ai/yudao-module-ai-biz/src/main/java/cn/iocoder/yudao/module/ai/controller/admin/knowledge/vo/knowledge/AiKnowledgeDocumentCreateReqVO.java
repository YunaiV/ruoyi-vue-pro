package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;


@Schema(description = "管理后台 - AI 知识库文档的创建 Request VO")
@Data
public class AiKnowledgeDocumentCreateReqVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1204")
    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeId;

    @Schema(description = "文档名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "三方登陆")
    @NotBlank(message = "文档名称不能为空")
    private String name;

    @Schema(description = "文档 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://doc.iocoder.cn")
    @URL(message = "文档 URL 格式不正确")
    private String url;

    @Schema(description = "每个段落的目标 token 数", requiredMode = Schema.RequiredMode.REQUIRED, example = "800")
    @NotNull(message = "每个段落的目标 token 数不能为空")
    private Integer defaultSegmentTokens;

    @Schema(description = "每个段落的最小字符数", requiredMode = Schema.RequiredMode.REQUIRED, example = "350")
    @NotNull(message = "每个段落的最小字符数不能为空")
    private Integer minSegmentWordCount;

    @Schema(description = "丢弃阈值：低于此阈值的段落会被丢弃", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "丢弃阈值不能为空")
    private Integer minChunkLengthToEmbed;

    @Schema(description = "最大段落数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    @NotNull(message = "最大段落数不能为空")
    private Integer maxNumSegments;

    @Schema(description = "分块是否保留分隔符", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "分块是否保留分隔符不能为空")
    private Boolean keepSeparator;

}
