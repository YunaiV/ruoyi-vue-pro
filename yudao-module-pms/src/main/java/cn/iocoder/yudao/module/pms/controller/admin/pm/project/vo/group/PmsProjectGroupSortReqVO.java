package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 项目分组排序 Request VO")
@Data
public class PmsProjectGroupSortReqVO {

    @Schema(description = "项目分组排序项", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "项目分组排序项不能为空")
    private List<Item> items;

    @Schema(description = "管理后台 - PMS 项目分组排序项")
    @Data
    public static class Item {

        @Schema(description = "项目分组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "项目分组编号不能为空")
        private Long id;

        @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "显示顺序不能为空")
        @Min(value = 0, message = "显示顺序不能小于 0")
        private Integer sort;

    }

}
