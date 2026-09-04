package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识库模板新增/修改 Request VO")
@Data
public class PmsKnowledgeLibraryTemplateSaveReqVO {

    @Schema(description = "模板编号", example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品研发")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过 100 个字符")
    private String name;

    @Schema(description = "模板简介", example = "提供完善的产品流程文档")
    @Size(max = 500, message = "模板简介不能超过 500 个字符")
    private String description;

    @Schema(description = "模板封面地址", example = "/static/pms/kb/template/product.png")
    @Size(max = 255, message = "模板封面地址不能超过 255 个字符")
    private String coverUrl;

    @Schema(description = "模板状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "模板状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "显示顺序不能为空")
    @PositiveOrZero(message = "显示顺序不能小于 0")
    private Integer sort;

    @Schema(description = "模板文档列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "模板文档不能为空")
    @Valid
    private List<Document> documents;

    @Schema(description = "模板文档")
    @Data
    public static class Document {

        @Schema(description = "文档标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品需求文档")
        @NotBlank(message = "文档标题不能为空")
        @Size(max = 255, message = "文档标题不能超过 255 个字符")
        private String title;

        @Schema(description = "文档内容", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "文档内容不能为空")
        private String content;

    }

}
