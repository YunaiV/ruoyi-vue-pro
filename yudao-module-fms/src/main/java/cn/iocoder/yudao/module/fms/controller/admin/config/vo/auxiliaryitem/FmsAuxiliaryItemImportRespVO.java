package cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * FMS 辅助核算项目导入结果 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 辅助核算项目导入结果 Response VO")
@Data
public class FmsAuxiliaryItemImportRespVO {

    @Schema(description = "总数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalCount;

    @Schema(description = "成功编码数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> successItemCodes;

    @Schema(description = "失败原因 Map", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureReasons;

}
