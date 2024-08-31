package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 知识库-文档 分页 Request VO")
@Data
public class AiKnowledgeDocumentPageReqVO extends PageParam {

    @Schema(description = "文档名称", example = "Java 开发手册")
    private String name;
}
