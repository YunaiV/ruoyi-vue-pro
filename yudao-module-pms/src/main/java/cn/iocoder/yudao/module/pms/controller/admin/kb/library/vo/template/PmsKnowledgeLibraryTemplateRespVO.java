package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 知识库模板 Response VO")
@Data
public class PmsKnowledgeLibraryTemplateRespVO {

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品研发")
    private String name;

    @Schema(description = "模板简介", example = "提供完善的产品流程文档")
    private String description;

    @Schema(description = "模板封面地址", example = "/static/pms/kb/template/product.png")
    private String coverUrl;

    @Schema(description = "模板状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

    @Schema(description = "模板文档列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsKnowledgeLibraryTemplateSaveReqVO.Document> documents;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
