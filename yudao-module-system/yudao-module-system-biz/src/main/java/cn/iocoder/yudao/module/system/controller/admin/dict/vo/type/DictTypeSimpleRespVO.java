package cn.iocoder.yudao.module.system.controller.admin.dict.vo.type;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("管理后台 - 字典类型精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictTypeSimpleRespVO {

    @ApiModelProperty(value = "字典类型编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "字典类型名称", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "字典类型", required = true, example = "sys_common_sex")
    private String type;

}
