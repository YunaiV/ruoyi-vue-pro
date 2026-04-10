package cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.location;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 库区分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmWarehouseLocationPageReqVO extends PageParam {

    @Schema(description = "库区编码", example = "L001")
    private String code;

    @Schema(description = "库区名称", example = "原料区")
    private String name;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

}
