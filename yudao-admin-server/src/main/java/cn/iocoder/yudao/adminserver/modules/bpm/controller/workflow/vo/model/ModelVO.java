package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// TODO @Li：新增和更新 ModelVO 最好分开哈。
// TODO @Li：swagger 的 example 属性，还有参数校验要加一下哈。
// TODO @Li：前缀要加 Bpm 。因为是单体工程，不拆分，很容易重复类名
/**
 * 新增模型 VO
 * @author yunlongn
 */
@Data
public class ModelVO {

    @ApiModelProperty(value = "模型Id")
    private String id;

    @ApiModelProperty(value = "模型名字", required = true)
    private String name;

    @ApiModelProperty(value = "模型描述")
    private String description;

    @ApiModelProperty(value = "版本号")
    private Integer revision;

    @ApiModelProperty(value = "key值")
    private String key;

    @ApiModelProperty(value = "bpmnXml")
    private String bpmnXml;
}
