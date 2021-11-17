package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 新增模型 VO
 * @author yunlongn
 */
@Data
public class ModelCreateVO {

    @ApiModelProperty(value = "模型名字", required = true)
    private String name;

    @ApiModelProperty(value = "模型描述")
    private String description;

    @ApiModelProperty(value = "版本号")
    private Integer revision;

    @ApiModelProperty(value = "key值")
    private String key;
}
