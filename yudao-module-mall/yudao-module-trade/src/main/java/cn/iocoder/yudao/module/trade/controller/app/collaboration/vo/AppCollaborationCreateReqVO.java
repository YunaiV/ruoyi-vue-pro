package cn.iocoder.yudao.module.trade.controller.app.collaboration.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户 App - 创建多方协作会话 Request VO")
@Data
public class AppCollaborationCreateReqVO {

    @Schema(description = "关联订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH_1001_1715000000000")
    @NotBlank(message = "订单号不能为空")
    private String orderId;

    @Schema(description = "参与者列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "参与者不能为空")
    @Valid
    private List<ParticipantReqVO> participants;

    @Data
    public static class ParticipantReqVO {

        @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "用户编号不能为空")
        private Long userId;

        @Schema(description = "显示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王采购")
        @NotBlank(message = "显示名称不能为空")
        private String name;

        @Schema(description = "角色：purchaser / finance / logistics / supplier",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "purchaser")
        @NotBlank(message = "角色不能为空")
        private String role;

    }

}
