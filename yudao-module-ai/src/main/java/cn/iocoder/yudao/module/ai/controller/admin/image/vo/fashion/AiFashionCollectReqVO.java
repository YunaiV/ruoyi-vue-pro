package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 管理后台 - 触发素材采集 Request VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - 触发素材采集 Request VO")
@Data
public class AiFashionCollectReqVO {

    @Schema(description = "采集源 ID（不填则采集全部 active 源）", example = "vogue_runway")
    private String sourceId;

    @Schema(description = "来源类型过滤（不填则不过滤）", example = "fashion_show")
    private String sourceType;

    @Schema(description = "每个来源最多采集条数，默认 20", example = "20")
    @Positive
    private Integer limitPerSource = 20;

    @Schema(description = "是否异步执行（true=立即返回任务ID，false=同步等待）", example = "true")
    private Boolean async = Boolean.TRUE;

}
