package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * BPM 流程 MetaInfo Response DTO
 * 主要用于 { Model#setMetaInfo(String)} 的存储
 *
 * 最终，它的字段和 {@link cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO} 是一致的
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
    private Long formId;  // formType 为 NORMAL 使用，必须非空
    @Schema(description = "自定义表单的提交路径，使用 Vue 的路由地址",
            example = "/bpm/oa/leave/create")
    private String formCustomCreatePath;  // 表单类型为 CUSTOM 时，必须非空
    @Schema(description = "自定义表单的查看路径，使用 Vue 的路由地址",
            example = "/bpm/oa/leave/view")
    private String formCustomViewPath;  // 表单类型为 CUSTOM 时，必须非空

    @Schema(description = "是否可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否可见不能为空")
    private Boolean visible;

    @Schema(description = "可发起用户编号数组", example = "[1,2,3]")
    private List<Long> startUserIds;

    @Schema(description = "可管理用户编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2,4,6]")
    @NotEmpty(message = "可管理用户编号数组不能为空")
    private List<Long> managerUserIds;

}
