package cn.iocoder.yudao.module.trade.controller.app.collaboration.vo;

import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabSessionBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 多方协作会话 Response VO")
@Data
public class AppCollaborationRespVO {

    @Schema(description = "协作会话完整信息")
    private CollabSessionBO session;

}
