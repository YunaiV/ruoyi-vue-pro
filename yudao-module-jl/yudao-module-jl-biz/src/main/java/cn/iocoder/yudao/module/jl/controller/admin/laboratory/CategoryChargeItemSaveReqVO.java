package cn.iocoder.yudao.module.jl.controller.admin.laboratory;

import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.CategoryChargeitemBaseWithoutIDVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class CategoryChargeItemSaveReqVO {
    @Schema(description = "实验名目收费项数组", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<CategoryChargeitemBaseWithoutIDVO> categoryChargeitemList;

    @Schema(description = "实验名目 id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Long categoryId;
}
