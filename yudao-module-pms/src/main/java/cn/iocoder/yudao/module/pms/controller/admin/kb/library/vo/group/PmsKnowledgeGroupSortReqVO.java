package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识库分组排序 Request VO")
@Data
public class PmsKnowledgeGroupSortReqVO {

    @Schema(description = "知识库分组排序项", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "知识库分组排序项不能为空")
    private List<@Valid Item> items;

    @Schema(description = "管理后台 - PMS 知识库分组排序项 Request VO")
    @Data
    public static class Item {

        @Schema(description = "知识库分组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "知识库分组编号不能为空")
        private Long id;

        @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "显示顺序不能为空")
        @Min(value = 0, message = "显示顺序不能小于 0")
        private Integer sort;

    }

}
