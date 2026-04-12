package cn.iocoder.yudao.module.im.controller.admin.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 消息拉取 Response VO
 */
@Schema(description = "管理后台 - 消息拉取 Response VO")
@Data
public class ImMessagePullRespVO<T> {

    @Schema(description = "消息列表")
    private List<T> list;

    @Schema(description = "下次拉取的最小 id", example = "100")
    private String nextMinId;

}
