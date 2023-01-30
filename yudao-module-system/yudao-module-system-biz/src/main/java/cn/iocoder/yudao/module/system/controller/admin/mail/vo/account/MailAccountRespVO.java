package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel("管理后台 - 邮箱账号 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailAccountRespVO extends MailAccountBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
