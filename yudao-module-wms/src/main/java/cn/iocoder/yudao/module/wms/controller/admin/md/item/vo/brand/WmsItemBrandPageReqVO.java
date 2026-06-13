package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.brand;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - WMS 商品品牌分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsItemBrandPageReqVO extends PageParam {

    @Schema(description = "品牌编号", example = "B00000001")
    private String code;

    @Schema(description = "品牌名称", example = "华为")
    private String name;

}
