package cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order;

import cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.detail.WmsCheckOrderDetailSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - WMS 盘库单保存 Request VO")
@Data
public class WmsCheckOrderSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "盘库单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PK202605110001")
    @NotBlank(message = "盘库单号不能为空")
    @Size(max = 64, message = "盘库单号长度不能超过 64 个字符")
    private String no;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单据日期不能为空")
    private LocalDateTime orderTime;

    @Schema(description = "备注", example = "备注")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @Schema(description = "盘库明细")
    @Valid
    private List<WmsCheckOrderDetailSaveReqVO> details;

}
