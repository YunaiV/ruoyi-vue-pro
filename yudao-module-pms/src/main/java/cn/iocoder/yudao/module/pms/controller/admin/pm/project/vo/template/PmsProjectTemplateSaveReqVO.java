package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 项目模板新增/修改 Request VO")
@Data
public class PmsProjectTemplateSaveReqVO {

    @Schema(description = "模板编号", example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "敏捷研发模板")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过 100 个字符")
    private String name;

    @Schema(description = "模板描述", example = "适用于标准敏捷研发项目")
    @Size(max = 500, message = "模板描述不能超过 500 个字符")
    private String description;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "项目类型不能为空")
    @InEnum(PmsProjectTypeEnum.class)
    private Integer projectType;

    @Schema(description = "模板状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "模板状态不能为空")
    private Integer status;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "启用的工作项类型列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2, 3, 4]")
    @NotEmpty(message = "工作项类型不能为空")
    private List<Integer> itemTypes;

    @Schema(description = "工作项状态模板列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "工作项状态不能为空")
    private List<StatusTemplate> statuses;

    @Schema(description = "看板列模板列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "看板列不能为空")
    private List<BoardTemplate> boards;

    @Schema(description = "工作项状态模板")
    @Data
    public static class StatusTemplate {

        @Schema(description = "模板内稳定编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "task_todo")
        @NotBlank(message = "状态编码不能为空")
        @Size(max = 50, message = "状态编码不能超过 50 个字符")
        private String code;

        @Schema(description = "状态名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "待处理")
        @NotBlank(message = "状态名称不能为空")
        @Size(max = 50, message = "状态名称不能超过 50 个字符")
        private String name;

        @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        @NotNull(message = "状态工作项类型不能为空")
        @InEnum(PmsWorkItemTypeEnum.class)
        private Integer workItemType;

        @Schema(description = "语义状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "语义状态不能为空")
        @InEnum(PmsWorkItemStatusTypeEnum.class)
        private Integer statusType;

        @Schema(description = "是否为初始状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "是否为初始状态不能为空")
        private Boolean defaultStatus;

        @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "状态显示顺序不能为空")
        private Integer sort;

        @Schema(description = "所属看板列编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "todo")
        @NotBlank(message = "状态所属看板列不能为空")
        private String boardCode;
    }

    @Schema(description = "看板列模板")
    @Data
    public static class BoardTemplate {

        @Schema(description = "模板内稳定编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "todo")
        @NotBlank(message = "看板列编码不能为空")
        @Size(max = 50, message = "看板列编码不能超过 50 个字符")
        private String code;

        @Schema(description = "看板列名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "待处理")
        @NotBlank(message = "看板列名称不能为空")
        @Size(max = 50, message = "看板列名称不能超过 50 个字符")
        private String name;

        @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        @NotNull(message = "看板列工作项类型不能为空")
        @InEnum(PmsWorkItemTypeEnum.class)
        private Integer workItemType;

        @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "看板列显示顺序不能为空")
        private Integer sort;

        @Schema(description = "关联的状态编码列表", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[\"task_todo\"]")
        @NotEmpty(message = "看板列关联状态不能为空")
        private List<String> statusCodes;
    }

}
