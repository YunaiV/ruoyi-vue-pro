package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 招聘渠道删除 Request VO")
@Data
public class HrmRecruitChannelDeleteReqVO {

    @Schema(description = "待删除招聘渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "待删除招聘渠道编号不能为空")
    private Long id;

    @Schema(description = "承接招聘渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1025")
    @NotNull(message = "承接招聘渠道编号不能为空")
    private Long transferChannelId;

}
