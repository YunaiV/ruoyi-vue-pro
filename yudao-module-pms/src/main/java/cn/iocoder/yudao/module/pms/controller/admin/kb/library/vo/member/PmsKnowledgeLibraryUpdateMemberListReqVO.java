package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.member;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeLibraryMemberLevelEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 更新知识库成员列表 Request VO")
@Data
public class PmsKnowledgeLibraryUpdateMemberListReqVO {

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "知识库编号不能为空")
    private Long libraryId;

    @Schema(description = "成员列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "成员列表不能为空")
    private List<Member> members;

    @Schema(description = "管理后台 - PMS 知识库成员更新项")
    @Data
    public static class Member {

        @Schema(description = "用户编号", example = "1")
        private Long userId;

        @Schema(description = "部门编号", example = "100")
        private Long deptId;

        @Schema(description = "成员等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        @NotNull(message = "成员等级不能为空")
        @InEnum(PmsKnowledgeLibraryMemberLevelEnum.class)
        private Integer level;

        @AssertTrue(message = "知识库成员必须且只能选择用户或部门")
        @JsonIgnore
        public boolean isUserOrDeptValid() {
            return (userId != null && deptId == null) || (userId == null && deptId != null);
        }

    }

}
