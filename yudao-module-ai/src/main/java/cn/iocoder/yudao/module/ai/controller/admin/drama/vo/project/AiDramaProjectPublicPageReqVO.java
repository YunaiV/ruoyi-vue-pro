package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaDurationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧公开作品分页 Request VO")
@Data
public class AiDramaProjectPublicPageReqVO extends PageParam {

    @Schema(description = "关键字：作品名称或画风", example = "地铁")
    private String keyword;

    @Schema(description = "画幅：按成片实际使用的画幅筛选", example = "9:16")
    private String aspectRatio;

    @Schema(description = "时长", example = "1")
    @InEnum(value = AiDramaDurationTypeEnum.class, message = "时长必须是 {value}")
    private Integer durationType;

}
