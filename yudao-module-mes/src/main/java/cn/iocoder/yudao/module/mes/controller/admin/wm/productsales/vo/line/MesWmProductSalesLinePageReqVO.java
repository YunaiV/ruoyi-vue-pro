package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.line;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - MES 销售出库单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmProductSalesLinePageReqVO extends PageParam {

    @Schema(description = "出库单ID", example = "1")
    private Long salesId;

    @Schema(description = "客户编号", example = "1")
    private Long clientId;

    @Schema(description = "出库单编号列表（内部使用，客户过滤时自动填充）", hidden = true)
    private List<Long> salesIds;

}
