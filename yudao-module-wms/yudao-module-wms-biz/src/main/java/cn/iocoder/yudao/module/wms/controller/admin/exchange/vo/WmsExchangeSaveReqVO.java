package cn.iocoder.yudao.module.wms.controller.admin.exchange.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemSaveReqVO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author jisencai
 * @table-fields : code,remark,id,audit_status,type,warehouse_id
 */
@Schema(description = "管理后台 - 换货单新增/修改 Request VO")
@Data
public class WmsExchangeSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20006")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "单据号")
    private String code;

    @Schema(description = "WMS换货单类型 ; WmsExchangeType : 1-良品转次品 , 2-次品转良品", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "WMS换货单类型不能为空", groups = { ValidationGroup.create.class })
    @InEnum(WmsExchangeType.class)
    private Integer type;

    @Schema(description = "调出仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27440")
    @NotNull(message = "调出仓库ID不能为空", groups = { ValidationGroup.create.class })
    private Long warehouseId;

    @Schema(description = "WMS换货单审批状态 ; WmsExchangeAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer auditStatus;

    @Schema(description = "特别说明", example = "你猜")
    private String remark;

    @Schema(description = "详情清单", example = "")
    private List<WmsExchangeItemSaveReqVO> itemList;
}
