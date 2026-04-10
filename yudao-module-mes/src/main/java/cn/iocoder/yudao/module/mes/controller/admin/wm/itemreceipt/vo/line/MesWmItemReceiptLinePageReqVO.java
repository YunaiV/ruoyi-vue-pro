package cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - MES 采购入库单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmItemReceiptLinePageReqVO extends PageParam {

    @Schema(description = "入库单编号", example = "1")
    private Long receiptId;

    @Schema(description = "供应商编号", example = "1")
    private Long vendorId;

    @Schema(description = "入库单编号列表（内部使用，供应商过滤时自动填充）", hidden = true)
    private List<Long> receiptIds;

}
