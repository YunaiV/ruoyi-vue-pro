package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Schema(description = "管理后台 - AI 知识库文档批量创建 Request VO")
@Data
public class AiKnowledgeDocumentCreateListReqVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1204")
    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeId;

    @Schema(description = "分段的最大 Token 数", requiredMode = Schema.RequiredMode.REQUIRED, example = "800")
    @NotNull(message = "分段的最大 Token 数不能为空")
    private Integer segmentMaxTokens;

    @Schema(description = "文档列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "文档列表不能为空")
    private List<Document> list;

    @Schema(description = "文档")
    @Data
    public static class Document {

        @Schema(description = "文档名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "三方登陆")
        @NotBlank(message = "文档名称不能为空")
        private String name;

        @Schema(description = "文档 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://doc.iocoder.cn")
        @URL(message = "文档 URL 格式不正确")
        private String url;

    }

}