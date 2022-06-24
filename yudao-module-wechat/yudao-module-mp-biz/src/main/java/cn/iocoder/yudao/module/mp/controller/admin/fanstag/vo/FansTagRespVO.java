package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;

@ApiModel("管理后台 - 粉丝标签 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FansTagRespVO extends FansTagBaseVO {

    @ApiModelProperty(value = "标签id，由微信分配.")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
