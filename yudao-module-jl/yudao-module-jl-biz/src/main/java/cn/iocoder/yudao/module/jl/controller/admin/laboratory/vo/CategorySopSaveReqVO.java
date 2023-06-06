package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 实验名目的操作SOP全量保存 Request VO")
@Data
@ToString(callSuper = true)
public class CategorySopSaveReqVO {
    @Schema(description = "sop 数组", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<CategorySopBaseWithoutIDVO> categorySopList;

    @Schema(description = "实验名目 id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Long categoryId;

}
