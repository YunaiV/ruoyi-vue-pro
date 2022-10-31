package cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 区块链节点更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EndpointUpdateReqVO extends EndpointBaseVO {

    @ApiModelProperty(value = "节点编号", required = true)
    @NotNull(message = "节点编号不能为空")
    private Long id;

}
