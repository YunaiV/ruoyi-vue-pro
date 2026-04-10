package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - MES 物料产品导入 Response VO")
@Data
@Builder
public class MesMdItemImportRespVO {

    @Schema(description = "创建成功的物料编码数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createCodes;

    @Schema(description = "更新成功的物料编码数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateCodes;

    @Schema(description = "导入失败的物料集合，key 为物料编码，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureCodes;

}
