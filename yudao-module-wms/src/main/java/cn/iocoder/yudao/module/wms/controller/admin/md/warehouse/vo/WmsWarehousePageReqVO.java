package cn.iocoder.yudao.module.wms.controller.admin.md.warehouse.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - WMS 仓库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库编号", example = "WH001")
    private String code;

    @Schema(description = "仓库名称", example = "原料仓")
    private String name;

}
