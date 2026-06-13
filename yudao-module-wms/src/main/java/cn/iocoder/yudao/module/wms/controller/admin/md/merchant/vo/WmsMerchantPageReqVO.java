package cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.md.WmsMerchantTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - WMS 往来企业分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsMerchantPageReqVO extends PageParam {

    @Schema(description = "往来企业编号", example = "MER001")
    private String code;

    @Schema(description = "往来企业名称", example = "华为")
    private String name;

    @Schema(description = "往来企业类型", example = "2")
    @InEnum(value = WmsMerchantTypeEnum.class, message = "往来企业类型必须是 {value}")
    private Integer type;

    @Schema(description = "往来企业类型数组", example = "2,3")
    @InEnum(value = WmsMerchantTypeEnum.class, message = "往来企业类型必须是 {value}")
    private List<Integer> types;

}
