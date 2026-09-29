package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 知识文档分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1KnowledgeDocumentPageReqVO extends PageParam {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeBaseId;

    @Schema(description = "名称，模糊匹配", example = "三体")
    private String name;

    @Schema(description = "向量化状态", example = "1")
    private Integer status;

}
