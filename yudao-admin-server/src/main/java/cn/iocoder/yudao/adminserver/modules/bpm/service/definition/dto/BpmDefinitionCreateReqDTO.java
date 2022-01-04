package cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 流程定义创建 Request DTO
 */
@Data
public class BpmDefinitionCreateReqDTO {

    /**
     * 流程标识
     */
    @NotEmpty(message = "流程标识不能为空")
    private String key;
    /**
     * 流程名称
     */
    @NotEmpty(message = "流程名称不能为空")
    private String name;
    /**
     * 流程描述
     */
    private String description;
    /**
     * 流程分类
     * 参见 bpm_model_category 数据字典
     */
    @NotEmpty(message = "流程分类不能为空")
    private String category;
    /**
     * BPMN XML
     */
    @NotEmpty(message = "BPMN XML 不能为空")
    private String bpmnXml;
    /**
     * 动态表单编号，允许空
     */
    private Long formId;

}
