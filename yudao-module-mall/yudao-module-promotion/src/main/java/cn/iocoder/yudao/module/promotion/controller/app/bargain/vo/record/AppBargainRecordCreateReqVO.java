package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - 砍价记录的创建 Request VO")
@Data
public class AppBargainRecordCreateReqVO {

    @Schema(description = "砍价活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "砍价活动编号不能为空")
    private Long activityId;

}
