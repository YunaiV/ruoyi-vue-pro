package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;

@ApiModel("管理后台 - 微信菜单 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMenuRespVO extends MpMenuBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
