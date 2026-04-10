package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.detail;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理后台 - MES 销售退货相关
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 销售退货明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmReturnSalesDetailPageReqVO extends PageParam {

    @Schema(description = "退货单ID", example = "1")
    private Long returnId;

    @Schema(description = "行ID", example = "1")
    private Long lineId;

    @Schema(description = "物料ID", example = "1")
    private Long itemId;

}
