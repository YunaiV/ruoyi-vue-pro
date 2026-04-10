package cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 仓库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库编码", example = "WH001")
    private String code;

    @Schema(description = "仓库名称", example = "原料仓")
    private String name;

    @Schema(description = "是否冻结", example = "false")
    private Boolean frozen;

}
