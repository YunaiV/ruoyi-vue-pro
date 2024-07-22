package cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - AI 聊天角色分页 Request VO")
@Data
public class AiChatRolePageReqVO extends PageParam {

    @Schema(description = "角色名称", example = "李四")
    private String name;

    @Schema(description = "角色类别", example = "创作")
    private String category;

    @Schema(description = "是否公开", example = "1")
    private Boolean publicStatus;

}