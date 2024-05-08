package cn.iocoder.yudao.module.bpm.dal.dataobject.definition;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * BPM 流程定义的拓信息
 * 主要解决 Flowable {@link org.flowable.engine.repository.ProcessDefinition} 不支持拓展字段，所以新建该表
 *
 * @author 芋道源码
 */
@TableName(value = "bpm_process_definition_info", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmProcessDefinitionInfoDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 流程定义的编号
     *
     * 关联 ProcessDefinition 的 id 属性
     */
    private String processDefinitionId;
    /**
     * 流程模型的编号
     *
     * 关联 Model 的 id 属性
     */
    private String modelId;

    /**
     * 图标
     */
    private String icon;
    /**
     * 描述
     */
    private String description;

    /**
     * 表单类型
     *
     * 关联 {@link BpmModelFormTypeEnum}
     */
    private Integer formType;
    /**
     * 动态表单编号
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     *
     * 关联 {@link BpmFormDO#getId()}
     */
    private Long formId;
    /**
     * 表单的配置
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     *
     * 冗余 {@link BpmFormDO#getConf()}
     */
    private String formConf;
    /**
     * 表单项的数组
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     *
     * 冗余 {@link BpmFormDO#getFields()} ()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
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

}
