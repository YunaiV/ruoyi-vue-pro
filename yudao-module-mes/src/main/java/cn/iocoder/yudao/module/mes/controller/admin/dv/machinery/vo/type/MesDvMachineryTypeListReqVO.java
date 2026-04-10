package cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - MES 设备类型列表 Request VO")
@Data
@Accessors(chain = true)
public class MesDvMachineryTypeListReqVO {

    @Schema(description = "类型名称", example = "数控机床")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
