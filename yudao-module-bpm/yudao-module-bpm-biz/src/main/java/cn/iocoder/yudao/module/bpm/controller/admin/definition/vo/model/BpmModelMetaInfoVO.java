package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmAutoApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * BPM 流程 MetaInfo Response DTO
 * 主要用于 { Model#setMetaInfo(String)} 的存储
 *
 * 最终，它的字段和
 * {@link cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO}
 * 是一致的
 *
 * @author 芋道源码
 */
@Data
public class BpmModelMetaInfoVO {

    @Schema(description = "流程图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/yudao.jpg")
    @NotEmpty(message = "流程图标不能为空")
    @URL(message = "流程图标格式不正确")
    private String icon;

    @Schema(description = "流程描述", example = "我是描述")
    private String description;

    @Schema(description = "流程类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(BpmModelTypeEnum.class)
    @NotNull(message = "流程类型不能为空")
    private Integer type;

    @Schema(description = "表单类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(BpmModelFormTypeEnum.class)
    @NotNull(message = "表单类型不能为空")
    private Integer formType;
    @Schema(description = "表单编号", example = "1024")
    private Long formId; // formType 为 NORMAL 使用，必须非空
    @Schema(description = "自定义表单的提交路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/create")
    private String formCustomCreatePath; // 表单类型为 CUSTOM 时，必须非空
    @Schema(description = "自定义表单的查看路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/view")
    private String formCustomViewPath; // 表单类型为 CUSTOM 时，必须非空

    @Schema(description = "是否可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否可见不能为空")
    private Boolean visible;

    @Schema(description = "可发起用户编号数组", example = "[1,2,3]")
    private List<Long> startUserIds;

    @Schema(description = "可管理用户编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2,4,6]")
    @NotEmpty(message = "可管理用户编号数组不能为空")
    private List<Long> managerUserIds;

    @Schema(description = "排序", example = "1")
    private Long sort; // 创建时，后端自动生成

    @Schema(description = "允许撤销审批中的申请", example = "true")
    private Boolean allowCancelRunningProcess;

    @Schema(description = "流程 ID 规则", example = "{}")
    private ProcessIdRule processIdRule;

    @Schema(description = "自动去重类型", example = "1")
    @InEnum(BpmAutoApproveTypeEnum.class)
    private Integer autoApprovalType;

    @Schema(description = "标题设置", example = "{}")
    private TitleSetting titleSetting;

    @Schema(description = "摘要设置", example = "{}")
    private SummarySetting summarySetting;

    @Schema(description = "流程 ID 规则")
    @Data
    @Valid
    public static class ProcessIdRule {

        @Schema(description = "是否启用", example = "false")
        @NotNull(message = "是否启用不能为空")
        private Boolean enable;

        @Schema(description = "前缀", example = "XX")
        private String prefix;

        @Schema(description = "中缀", example = "20250120")
        private String infix; // 精确到日、精确到时、精确到分、精确到秒

        @Schema(description = "后缀", example = "YY")
        private String postfix;

        @Schema(description = "序列长度", example = "5")
        @NotNull(message = "序列长度不能为空")
        private Integer length;

    }

    @Schema(description = "标题设置")
    @Data
    @Valid
    public static class TitleSetting {

        @Schema(description = "是否自定义", example = "false")
        @NotNull(message = "是否自定义不能为空")
        private Boolean enable;

        @Schema(description = "标题", example = "流程标题")
        private String title;

    }

    @Schema(description = "摘要设置")
    @Data
    @Valid
    public static class SummarySetting {

        @Schema(description = "是否自定义", example = "false")
        @NotNull(message = "是否自定义不能为空")
        private Boolean enable;

        @Schema(description = "摘要字段数组", example = "[]")
        private List<String> summary;

    }

}
