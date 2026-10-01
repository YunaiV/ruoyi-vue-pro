package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "管理后台 - AI1 知识库新增/修改 Request VO")
@Data
public class Ai1KnowledgeBaseSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品手册")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "描述", example = "产品使用说明")
    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    @Schema(description = "向量化 Provider 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "向量化 Provider 不能为空")
    private Long embeddingProviderId;

    @Schema(description = "向量化模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "向量化模型不能为空")
    private Long embeddingModelId;

    @Schema(description = "分片大小，单位：字符", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    @NotNull(message = "分片大小不能为空")
    @Min(value = 100, message = "分片大小不能小于 100")
    private Integer chunkSize;

    @Schema(description = "分片重叠，单位：字符", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "分片重叠不能为空")
    @Min(value = 0, message = "分片重叠不能小于 0")
    private Integer chunkOverlap;

    @Schema(description = "检索数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "检索数量不能为空")
    @Min(value = 1, message = "检索数量必须大于 0")
    private Integer topK;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @AssertTrue(message = "分片重叠必须小于分片大小")
    @JsonIgnore
    public boolean isChunkOverlapValid() {
        return chunkSize == null || chunkOverlap == null || chunkOverlap < chunkSize;
    }

}
