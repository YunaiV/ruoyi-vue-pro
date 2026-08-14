package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - FMS 科目导入结果 Response VO")
@Data
public class FmsSubjectImportRespVO {

    @Schema(description = "总数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalCount;

    @Schema(description = "成功科目编码数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> successSubjectCodes;

    @Schema(description = "失败原因 Map", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureReasons;

}
