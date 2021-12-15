package cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("待办任务申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TodoTaskPageReqVO extends PageParam {

    private String assignee;
}
