package cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.permission;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeContentLevelEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 更新知识内容协作权限 Request VO")
@Data
public class PmsKnowledgeContentPermissionUpdateReqVO {

    @Schema(description = "协作权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "协作权限编号不能为空")
    private Long id;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否公开不能为空")
    private Boolean openStatus;

    @Schema(description = "公开协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "公开协作等级不能为空")
    @InEnum(PmsKnowledgeContentLevelEnum.class)
    private Integer openLevel;

    @Schema(description = "协作者列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "协作者列表不能为空")
    private List<Member> members;

    @Schema(description = "管理后台 - PMS 知识内容协作者保存项")
    @Data
    public static class Member {

        @Schema(description = "用户编号", example = "1")
        private Long userId;

        @Schema(description = "部门编号", example = "100")
        private Long deptId;

        @Schema(description = "协作等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        @NotNull(message = "协作等级不能为空")
        @InEnum(PmsKnowledgeContentLevelEnum.class)
        private Integer level;

        @AssertTrue(message = "协作者必须且只能选择用户或部门")
        @JsonIgnore
        public boolean isUserOrDeptValid() {
            return (userId != null && deptId == null) || (userId == null && deptId != null);
        }

    }

}
