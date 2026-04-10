package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 外协入库单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmOutsourceReceiptLinePageReqVO extends PageParam {

    @Schema(description = "入库单编号", example = "1")
    private Long receiptId;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;

}
