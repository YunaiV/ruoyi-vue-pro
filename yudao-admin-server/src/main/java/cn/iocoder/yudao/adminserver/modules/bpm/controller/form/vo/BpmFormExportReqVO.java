package cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "动态表单 Excel 导出 Request VO", description = "参数和 BpmFormPageReqVO 是一致的")
@Data
public class BpmFormExportReqVO {

    @ApiModelProperty(value = "表单名称")
    private String name;

}
