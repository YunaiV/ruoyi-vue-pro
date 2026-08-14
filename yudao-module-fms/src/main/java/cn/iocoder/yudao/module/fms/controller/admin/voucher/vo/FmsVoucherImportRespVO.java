package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - FMS 凭证导入结果 Response VO")
@Data
public class FmsVoucherImportRespVO {

    @Schema(description = "总分录数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalRowCount;

    @Schema(description = "成功分录数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer successRowCount;

    @Schema(description = "失败分录数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer failureRowCount;

    @Schema(description = "总凭证数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalVoucherCount;

    @Schema(description = "成功凭证数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer successVoucherCount;

    @Schema(description = "失败凭证数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer failureVoucherCount;

    @Schema(description = "错误数据文件地址")
    private String errorFileUrl;

    /**
     * 错误数据数组
     *
     * 仅用于生成错误数据文件，不直接返回前端
     */
    @JsonIgnore
    private List<FmsVoucherImportExcelVO> errorRows;

}
