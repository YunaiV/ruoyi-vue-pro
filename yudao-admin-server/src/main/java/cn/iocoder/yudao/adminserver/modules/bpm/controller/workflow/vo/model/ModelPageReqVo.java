package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author yunlong.li
 */
@ApiModel("模型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ModelPageReqVo extends PageParam {
    @ApiModelProperty("模型名字")
    private String name;
}
