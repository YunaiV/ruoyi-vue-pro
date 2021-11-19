package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author yunlong.li
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel("流程定义分页 Request VO")
public class ProcessDefinitionPageReqVo extends PageParam {
    @ApiModelProperty("流程名字")
    private String name;
}
