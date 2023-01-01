package cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 短信渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsChannelRespVO extends SmsChannelBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "渠道编码", required = true, example = "YUN_PIAN", notes = "参见 SmsChannelEnum 枚举类")
    private String code;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
