package cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("请假申请审批人员 Response VO")
@Data
@Builder
@EqualsAndHashCode
@ToString
public class OALeaveApplyMembersVO {

    @ApiModelProperty(value = "部门的hr")
    private String hr;

    @ApiModelProperty(value = "部门的项目经理")
    private String pm;

    @ApiModelProperty(value = "部门的部门经理")
    private String bm;
}
