package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
 * @author fengdan
 */
@ApiModel("管理后台 - 公众号账号更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAccountUpdateReqVO extends MpAccountBaseVO {

    @ApiModelProperty(value = "编号", required = true)
    @NotNull(message = "编号不能为空")
    private Long id;

}
