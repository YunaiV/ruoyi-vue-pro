package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 绘画公开的分页 Request VO")
@Data
public class AiImagePublicPageReqVO extends PageParam {

    @Schema(description = "提示词")
    private String prompt;

}