package cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo;

import lombok.*;
import io.swagger.annotations.*;

@ApiModel("请假申请创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OALeaveCreateReqVO extends OALeaveBaseVO {

    private String processKey;
}
