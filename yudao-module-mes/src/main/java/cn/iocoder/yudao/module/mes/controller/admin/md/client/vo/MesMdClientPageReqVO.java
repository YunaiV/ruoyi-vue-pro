package cn.iocoder.yudao.module.mes.controller.admin.md.client.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 客户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesMdClientPageReqVO extends PageParam {

    @Schema(description = "客户编码", example = "C00184")
    private String code;

    @Schema(description = "客户名称", example = "比亚迪")
    private String name;

    @Schema(description = "客户简称", example = "比亚迪")
    private String nickname;

    @Schema(description = "客户英文名称", example = "BYD")
    private String englishName;

    @Schema(description = "客户类型", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
