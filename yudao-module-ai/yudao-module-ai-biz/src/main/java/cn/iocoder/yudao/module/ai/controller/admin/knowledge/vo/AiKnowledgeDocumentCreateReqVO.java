package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author xiaoxin
 */
@Schema(description = "管理后台 - AI 知识库【创建文档】 Request VO")
@Data
public class AiKnowledgeDocumentCreateReqVO {


    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1204")
    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeId;

    @Schema(description = "文档名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "三方登陆")
    @NotBlank(message = "文档名称不能为空")
    private String name;

    @Schema(description = "文档 url", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://doc.iocoder.cn")
    private String url;

}
