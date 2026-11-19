package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI 短剧 3D 导演工程 Response VO")
@Data
public class AiDramaDirectorRespVO {

    @Schema(description = "修订号：未保存过时为 0", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long revision;

    @Schema(description = "导演工程文档：场景对象、相机、时间轴等；未保存过时为空")
    private Map<String, Object> document;

}
