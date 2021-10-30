package cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("请假申请更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaLeaveUpdateReqVO extends OaLeaveBaseVO {

    @ApiModelProperty(value = "请假表单主键", required = true)
    @NotNull(message = "请假表单主键不能为空")
    private Long id;

    // TODO @json：swagger 和 validator 的注解要加哈。

    private String taskId;

    private String comment;

    private Map<String,Object> variables;

    // TODO @芋艿：variables 的作用是啥。

}
