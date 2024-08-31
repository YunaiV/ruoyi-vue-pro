package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 知识库-文档 Response VO")
@Data
public class AiKnowledgeDocumentRespVO extends PageParam {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long id;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
    private Long knowledgeId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 开发手册")
    private String name;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 是一门面向对象的语言.....")
    private String content;

    @Schema(description = "文档 url", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://doc.iocoder.cn")
    private String url;

    @Schema(description = "token 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer tokens;

    @Schema(description = "字符数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1008")
    private Integer wordCount;

    @Schema(description = "切片状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sliceStatus;

    @Schema(description = "文档状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}
