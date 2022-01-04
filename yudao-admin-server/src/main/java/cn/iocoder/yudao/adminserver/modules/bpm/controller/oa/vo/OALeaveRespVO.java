package cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo;

import lombok.*;
import io.swagger.annotations.*;

@ApiModel("请假申请 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OALeaveRespVO extends OALeaveBaseVO {

    @ApiModelProperty(value = "请假表单主键", required = true)
    private Long id;

}
