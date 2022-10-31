package cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 区块链网络更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NetUpdateReqVO extends NetBaseVO {

    @ApiModelProperty(value = "网络编号", required = true)
    @NotNull(message = "网络编号不能为空")
    private Long id;

}
