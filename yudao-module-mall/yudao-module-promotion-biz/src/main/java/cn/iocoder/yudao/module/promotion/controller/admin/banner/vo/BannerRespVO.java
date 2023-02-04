package cn.iocoder.yudao.module.promotion.controller.admin.banner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author xia
 */
@Schema(description = "管理后台 - Banner Response VO")
@Data
@ToString(callSuper = true)
public class BannerRespVO  extends BannerBaseVO {

    @Schema(description = "banner编号", required = true)
    @NotNull(message = "banner编号不能为空")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
