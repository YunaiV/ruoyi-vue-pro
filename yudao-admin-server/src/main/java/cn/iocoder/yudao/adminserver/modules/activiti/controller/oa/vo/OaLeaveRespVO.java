package cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("请假申请 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaLeaveRespVO extends OaLeaveBaseVO {

    @ApiModelProperty(value = "请假表单主键", required = true)
    private Long id;

}
