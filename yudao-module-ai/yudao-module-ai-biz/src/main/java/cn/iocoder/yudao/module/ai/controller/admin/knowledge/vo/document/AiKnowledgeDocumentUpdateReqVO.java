package cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Schema(description = "管理后台 - AI 更新 知识库-文档 Request VO")
@Data
public class AiKnowledgeDocumentUpdateReqVO {


    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15583")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "是否启用", example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "名称", example = "Java 开发手册")
    private String name;

}
