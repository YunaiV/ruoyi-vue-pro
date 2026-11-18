package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 音乐广场分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 音乐广场分页 Request VO")
@Data
public class AiMusicPublicPageReqVO extends PageParam {

    @Schema(description = "音乐名称，模糊匹配", example = "夏夜晚风")
    private String title;

}
