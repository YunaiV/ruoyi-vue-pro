package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - MES 物料产品分类列表 Request VO")
@Data
public class MesMdItemTypeListReqVO {

    @Schema(description = "分类名称", example = "原材料")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
