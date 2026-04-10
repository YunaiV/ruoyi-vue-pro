package cn.iocoder.yudao.module.mp.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 公众号粉丝更新 Request VO")
@Data
public class MpUserUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "昵称", example = "芋道")
    private String nickname;

    @Schema(description = "备注", example = "你是一个芋头嘛")
    private String remark;

    @Schema(description = "标签编号数组", example = "1,2,3")
    private List<Long> tagIds;

}
