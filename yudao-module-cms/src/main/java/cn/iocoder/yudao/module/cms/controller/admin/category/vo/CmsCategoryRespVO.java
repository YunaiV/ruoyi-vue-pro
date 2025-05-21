package cn.iocoder.yudao.module.cms.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;

@Schema(description = "Admin - CMS Category Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCategoryRespVO extends CmsCategoryBaseVO {

    @Schema(description = "Category ID", required = true, example = "1")
    private Long id;

    @Schema(description = "Creation Time", required = true)
    private LocalDateTime createTime;
}
