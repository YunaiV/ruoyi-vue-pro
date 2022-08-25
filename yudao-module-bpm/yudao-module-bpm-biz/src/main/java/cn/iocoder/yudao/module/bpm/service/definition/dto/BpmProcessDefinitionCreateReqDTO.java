package cn.iocoder.yudao.module.bpm.service.definition.dto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * 流程定义创建 Request DTO
 */
@Data
public class BpmProcessDefinitionCreateReqDTO {

    // ========== 模型相关 ==========

    /**
     * 流程模型的编号
     */
    @NotEmpty(message = "流程模型编号不能为空")
    private String modelId;
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
    private byte[] bpmnBytes;

    // ========== 表单相关 ==========

    /**
     * 表单类型
     */
    @NotNull(message = "表单类型不能为空")
    private Integer formType;
    /**
     * 动态表单编号
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     */
    private Long formId;
    /**
     * 表单的配置
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     */
    private String formConf;
    /**
     * 表单项的数组
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     */
    private List<String> formFields;
    /**
     * 自定义表单的提交路径，使用 Vue 的路由地址
     * 在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时
     */
    private String formCustomCreatePath;
    /**
     * 自定义表单的查看路径，使用 Vue 的路由地址
     * 在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时
     */
    private String formCustomViewPath;

    @AssertTrue(message = "流程表单信息不全")
    public boolean isNormalFormTypeValid() {
        // 如果非业务表单，则直接通过
        if (!Objects.equals(formType, BpmModelFormTypeEnum.NORMAL.getType())) {
            return true;
        }
        return formId != null && StrUtil.isNotEmpty(formConf) && CollUtil.isNotEmpty(formFields);
    }

    @AssertTrue(message = "业务表单信息不全")
    public boolean isNormalCustomTypeValid() {
        // 如果非业务表单，则直接通过
        if (!Objects.equals(formType, BpmModelFormTypeEnum.CUSTOM.getType())) {
            return true;
        }
        return StrUtil.isNotEmpty(formCustomCreatePath) && StrUtil.isNotEmpty(formCustomViewPath);
    }

}
