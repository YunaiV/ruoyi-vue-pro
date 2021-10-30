package cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("请假申请更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaLeaveUpdateReqVO extends OaLeaveBaseVO {

    @ApiModelProperty(value = "请假表单主键", required = true)
    @NotNull(message = "请假表单主键不能为空")
    private Long id;


    private String taskId;

    private String comment;

    private Map<String,Object> variables;

}
