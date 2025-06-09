package cn.iocoder.yudao.module.tms.controller.admin.transfer.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 调拨单新增/修改 Request VO")
@Data
public class TmsTransferSaveReqVO {

    @Schema(description = "调拨单编号")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时，调拨单id不能为空")
    @Null(groups = {Validation.OnCreate.class}, message = "创建时，调拨单id需为空")
    private Long id;

    @Schema(description = "调拨单编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调拨单编码不能为空")
    @NotNull(message = "调拨单编码不能为空")
    private String code;

    @Schema(description = "发出仓库ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发出仓库ID不能为空")
    private Long fromWarehouseId;

    @Schema(description = "目的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目的仓库ID不能为空")
    private Long toWarehouseId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "跟踪号")
    private String traceNo;

    @Schema(description = "调拨单明细")
    @Size(min = 1, message = "调拨单明细列表长度不能为空")
    private List<TmsTransferItemSaveReqVO> items;
}