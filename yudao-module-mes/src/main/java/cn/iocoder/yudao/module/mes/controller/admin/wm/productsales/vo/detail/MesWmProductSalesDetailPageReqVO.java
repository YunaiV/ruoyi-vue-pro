package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.detail;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 销售出库明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmProductSalesDetailPageReqVO extends PageParam {

    @Schema(description = "出库单ID", example = "1")
    private Long salesId;

    @Schema(description = "出库单行ID", example = "1")
    private Long lineId;

}
