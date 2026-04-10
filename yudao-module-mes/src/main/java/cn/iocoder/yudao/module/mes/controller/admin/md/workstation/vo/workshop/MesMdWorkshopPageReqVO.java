package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 车间分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesMdWorkshopPageReqVO extends PageParam {

    @Schema(description = "车间编码", example = "WS001")
    private String code;

    @Schema(description = "车间名称", example = "一号车间")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
