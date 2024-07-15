package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Ai Image 发布列表 req")
@Data
public class AiImagePublicListReqVO extends PageParam {

    @Schema(description = "提示词")
    private String prompt;

}