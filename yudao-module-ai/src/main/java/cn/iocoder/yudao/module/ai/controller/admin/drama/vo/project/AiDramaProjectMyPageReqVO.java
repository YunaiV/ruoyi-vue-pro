package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧【我的】项目分页 Request VO")
@Data
public class AiDramaProjectMyPageReqVO extends PageParam {

    @Schema(description = "项目名称，模糊匹配", example = "地铁")
    private String name;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "成片状态", example = "40")
    private Integer composeStatus;

    @Schema(description = "是否公开", example = "true")
    private Boolean publicStatus;

}
