package cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.area;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 库位分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmWarehouseAreaPageReqVO extends PageParam {

    @Schema(description = "库位编码", example = "A001")
    private String code;

    @Schema(description = "库位名称", example = "默认库位")
    private String name;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "位置 X", example = "1")
    private Integer positionX;

    @Schema(description = "位置 Y", example = "1")
    private Integer positionY;

    @Schema(description = "位置 Z", example = "1")
    private Integer positionZ;

}
