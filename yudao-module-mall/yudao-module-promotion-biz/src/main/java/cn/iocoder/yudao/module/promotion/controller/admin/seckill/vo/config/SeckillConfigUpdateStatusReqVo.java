package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 修改时段配置状态 Request VO")
@Data
public class SeckillConfigUpdateStatusReqVo {

    @Schema(description = "时段配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "时段配置编号不能为空")
    private Long id;

    @Schema(description = "状态，见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    private Integer status;

}
