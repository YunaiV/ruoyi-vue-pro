package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group;

import lombok.*;

import java.time.LocalDateTime;

import io.swagger.annotations.*;

@ApiModel("管理后台 - 用户组 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmUserGroupRespVO extends BpmUserGroupBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
