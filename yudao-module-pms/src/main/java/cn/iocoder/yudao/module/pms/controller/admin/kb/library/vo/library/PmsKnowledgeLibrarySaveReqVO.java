package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识库新增/修改 Request VO")
@Data
public class PmsKnowledgeLibrarySaveReqVO {

    @Schema(description = "知识库编号", example = "1024")
    private Long id;

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品知识库")
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 50, message = "知识库名称不能超过 50 个字符")
    private String name;

    @Schema(description = "知识库简介", example = "沉淀产品文档")
    @Size(max = 300, message = "知识库简介不能超过 300 个字符")
    private String description;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "知识库可见范围不能为空")
    private Boolean openStatus;

    @Schema(description = "知识库封面")
    @Size(max = 255, message = "知识库封面地址不能超过 255 个字符")
    private String coverUrl;

    @Schema(description = "创建时的初始管理员用户编号列表", example = "[1, 2]")
    private List<Long> adminUserIds;

    @Schema(description = "创建时的初始普通成员用户编号列表", example = "[3, 4]")
    private List<Long> memberUserIds;

    @Schema(description = "模板编号，仅创建时有效", example = "1")
    @Positive(message = "模板编号必须大于 0")
    private Long templateId;

}
