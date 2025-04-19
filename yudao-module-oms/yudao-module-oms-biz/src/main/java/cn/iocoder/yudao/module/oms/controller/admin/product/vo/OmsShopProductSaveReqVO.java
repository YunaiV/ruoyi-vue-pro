package cn.iocoder.yudao.module.oms.controller.admin.product.vo;

import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemSaveReqVO;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - OMS 店铺产品新增/修改 Request VO")
@Data
public class OmsShopProductSaveReqVO {

    @Schema(description = "店铺产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18131")
    private Long id;

    @Schema(description = "店铺编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18131")
    private Long shopId;

    @Schema(description = "平台产品ID编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18131")
    private String platformProductUid;

    @Schema(description = "价格")
    @ExcelProperty("价格")
    private BigDecimal price;

    @Schema(description = "币种")
    @ExcelProperty("币种")
    private String currencyCode;

    @Schema(description = "店铺别名")
    private String name;

    @Schema(description = "平台SKU", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "平台SKU不能为空")
    private String code;

    @Schema(description = "链接", example = "https://www.iocoder.cn")
    private String url;

    @Schema(description = "店铺产品项目", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<OmsShopProductItemSaveReqVO> items;


    @Schema(description = "所属部门ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long deptId;


}