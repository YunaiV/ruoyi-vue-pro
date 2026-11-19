package cn.iocoder.yudao.module.ai.controller.admin.video.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 视频公开作品分页 Request VO")
@Data
public class AiVideoPublicPageReqVO extends PageParam {

    @Schema(description = "创作类型", example = "1")
    @InEnum(AiVideoTypeEnum.class)
    private Integer type;

    @Schema(description = "提示词，模糊匹配", example = "橘猫")
    private String prompt;

}
