package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "管理后台 - PMS 工作项导入 Response VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PmsWorkItemImportRespVO {

    @Schema(description = "导入成功数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer successCount;

    @Schema(description = "导入失败行及原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<Integer, String> failureReasons;

}
