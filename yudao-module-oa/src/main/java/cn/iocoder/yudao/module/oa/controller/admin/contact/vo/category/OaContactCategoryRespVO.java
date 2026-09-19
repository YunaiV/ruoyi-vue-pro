package cn.iocoder.yudao.module.oa.controller.admin.contact.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OA 联系人分类 Response VO")
@Data
public class OaContactCategoryRespVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "客户")
    private String name;

    @Schema(description = "显示排序", example = "10")
    private Integer sort;

    @Schema(description = "创建时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
