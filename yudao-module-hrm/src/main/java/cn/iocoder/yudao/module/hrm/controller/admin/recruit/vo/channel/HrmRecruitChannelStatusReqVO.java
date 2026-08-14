package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 招聘渠道状态修改 Request VO")
@Data
public class HrmRecruitChannelStatusReqVO {

    @Schema(description = "招聘渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "招聘渠道编号不能为空")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

}
