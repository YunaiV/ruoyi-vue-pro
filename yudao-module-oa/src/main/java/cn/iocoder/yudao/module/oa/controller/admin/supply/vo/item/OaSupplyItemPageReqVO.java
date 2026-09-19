package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 办公用品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaSupplyItemPageReqVO extends PageParam {

    @Schema(description = "物品名称", example = "A4 打印纸")
    private String name;

    @Schema(description = "物品编码", example = "BGYP0001")
    private String no;

    @Schema(description = "类别", example = "1")
    private Integer category;

    @Schema(description = "管理类型", example = "1")
    private Integer manageType;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
