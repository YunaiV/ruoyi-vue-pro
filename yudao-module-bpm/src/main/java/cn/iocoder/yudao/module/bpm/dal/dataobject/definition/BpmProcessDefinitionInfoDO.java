package cn.iocoder.yudao.module.bpm.dal.dataobject.definition;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmAutoApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;

import java.util.List;

/**
 * BPM 流程定义的拓信息
 * 主要解决 Flowable {@link org.flowable.engine.repository.ProcessDefinition} 不支持拓展字段，所以新建该表
 *
 * @author 芋道源码
 */
@TableName(value = "bpm_process_definition_info", autoResultMap = true)
@KeySequence("bpm_process_definition_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 关联 {@link ProcessDefinition#getId()} 属性
     */
    private String processDefinitionId;
    /**
     * 流程模型的编号
     *
     * 关联 {@link Model#getId()} 属性
     */
    private String modelId;
    /**
     * 流程模型的类型
     *
     * 枚举 {@link BpmModelTypeEnum}
     */
    private Integer modelType;

    /**
     * 流程分类的编码
     *
     * 关联 {@link BpmCategoryDO#getCode()}
     *
     * 为什么要存储？原因是，{@link ProcessDefinition#getCategory()} 无法设置
     */
    private String category;
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
     * 枚举 {@link BpmModelFormTypeEnum}
     */
    private Integer formType;
    /**
     * 动态表单编号
     *
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     *
     * 关联 {@link BpmFormDO#getId()}
     */
    private Long formId;
    /**
     * 表单的配置
     *
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     *
     * 冗余 {@link BpmFormDO#getConf()}
     */
    private String formConf;
    /**
     * 表单项的数组
     *
     * 在表单类型为 {@link BpmModelFormTypeEnum#NORMAL} 时
     *
     * 冗余 {@link BpmFormDO#getFields()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> formFields;
    /**
     * 自定义表单的提交路径，使用 Vue 的路由地址
     *
     * 在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时
     */
    private String formCustomCreatePath;
    /**
     * 自定义表单的查看路径，使用 Vue 的路由地址
     *
     * 在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时
     */
    private String formCustomViewPath;

    /**
     * SIMPLE 设计器模型数据 json 格式
     *
     * 目的：当使用仿钉钉设计器时。流程模型发布的时候，需要保存流程模型设计器的快照数据。
     */
    private String simpleModel;
    /**
     * 是否可见
     *
     * 目的：如果 false 不可见，则不展示在“发起流程”的列表里
     */
    private Boolean visible;
    /**
     * 排序值
     */
    private Long sort;

    /**
     * 可发起用户编号数组
     *
     * 关联 {@link AdminUserRespDTO#getId()} 字段的数组
     *
     * 如果为空，则表示“全部可以发起”！
     *
     * 它和 {@link #visible} 的区别在于：
     * 1. {@link #visible} 只是决定是否可见。即使不可见，还是可以发起
     * 2. startUserIds 决定某个用户是否可以发起。如果该用户不可发起，则他也是不可见的
     */
    @TableField(typeHandler = LongListTypeHandler.class) // 为了可以使用 find_in_set 进行过滤
    private List<Long> startUserIds;

    /**
     * 可发起部门编号数组
     *
     * 关联 {@link AdminUserRespDTO#getDeptId()} 字段的数组
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> startDeptIds;

    /**
     * 可管理用户编号数组
     *
     * 关联 {@link AdminUserRespDTO#getId()} 字段的数组
     */
    @TableField(typeHandler = LongListTypeHandler.class) // 为了可以使用 find_in_set 进行过滤
    private List<Long> managerUserIds;

    /**
     * 是否允许撤销审批中的申请
     */
    private Boolean allowCancelRunningProcess;

    /**
     * 是否允许审批人撤回任务
     */
    private Boolean allowWithdrawTask;

    /**
     * 流程 ID 规则
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.ProcessIdRule processIdRule;

    /**
     * 自动去重类型
     *
     * 枚举 {@link BpmAutoApproveTypeEnum}
     */
    private Integer autoApprovalType;

    /**
     * 标题设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.TitleSetting titleSetting;
    /**
     * 摘要设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.SummarySetting summarySetting;

    /**
     * 流程前置通知设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.HttpRequestSetting processBeforeTriggerSetting;
    /**
     * 流程后置通知设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.HttpRequestSetting processAfterTriggerSetting;

    /**
     * 任务前置通知设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.HttpRequestSetting taskBeforeTriggerSetting;

    /**
     * 任务后置通知设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.HttpRequestSetting taskAfterTriggerSetting;

    /**
     * 自定义打印模板设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BpmModelMetaInfoVO.PrintTemplateSetting printTemplateSetting;

}
