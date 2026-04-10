package cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 供应商分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesMdVendorPageReqVO extends PageParam {

    @Schema(description = "供应商编码", example = "V00101")
    private String code;

    @Schema(description = "供应商名称", example = "海力德")
    private String name;

    @Schema(description = "供应商简称", example = "海力德")
    private String nickname;

    @Schema(description = "供应商英文名称", example = "HLD")
    private String englishName;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
