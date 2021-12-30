package cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("动态表单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmFormPageReqVO extends PageParam {

    @ApiModelProperty(value = "表单名称")
    private String name;

}
