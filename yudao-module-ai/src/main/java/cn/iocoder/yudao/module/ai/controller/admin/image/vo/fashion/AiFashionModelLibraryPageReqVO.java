package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - AI 服装素材库分页查询 Request VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 服装素材库分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiFashionModelLibraryPageReqVO extends PageParam {

    @Schema(description = "关键词（搜索标题/描述）", example = "红色连衣裙")
    private String keyword;

    @Schema(description = "来源类型（fashion_show/brand/model_agency/street_style）", example = "fashion_show")
    private String sourceType;

    @Schema(description = "品类（dress/suit/shirt/pants/jacket/skirt/accessories）", example = "dress")
    private String category;

    @Schema(description = "品牌名称", example = "Gucci")
    private String brand;

    @Schema(description = "是否含模特", example = "true")
    private Boolean isModel;

    @Schema(description = "模特姿势（walking/standing/sitting/front/side/back）", example = "walking")
    private String modelPose;

    @Schema(description = "模特体型（slim/athletic/curvy/plus_size）", example = "slim")
    private String modelBodyType;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
