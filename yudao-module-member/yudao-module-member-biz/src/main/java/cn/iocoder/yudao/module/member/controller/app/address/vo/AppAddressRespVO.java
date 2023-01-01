package cn.iocoder.yudao.module.member.controller.app.address.vo;

import lombok.*;
import java.time.LocalDateTime;
import io.swagger.annotations.*;

@ApiModel("用户 APP - 用户收件地址 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppAddressRespVO extends AppAddressBaseVO {

    @ApiModelProperty(value = "编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
