package cn.iocoder.yudao.module.oa.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - OA 任务完成排行 Response VO")
@Data
public class OaTaskRankingRespVO {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道")
    private String userName;

    @Schema(description = "完成任务数量", example = "10")
    private Long completedCount;

}
