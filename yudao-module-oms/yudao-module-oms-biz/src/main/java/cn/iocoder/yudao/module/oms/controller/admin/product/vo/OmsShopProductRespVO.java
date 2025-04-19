package cn.iocoder.yudao.module.oms.controller.admin.product.vo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - OMS 店铺产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OmsShopProductRespVO extends BaseDO {

    @Schema(description = "店铺产品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18131")
    @ExcelProperty("店铺产品id")
    private Long id;

    @Schema(description = "店铺id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18131")
    @ExcelProperty("店铺id")
    private Long shopId;

    @Schema(description = "产品所属店铺", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private OmsShopRespVO shop;

    @Schema(description = "平台sku", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("店铺产品编号【平台sku】")
    private String code;

    @Schema(description = "外部资源id", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部资源id")
    private String sourceId;

    @Schema(description = "店铺产品名称")
    @ExcelProperty("店铺产品名称")
    private String name;

    @Schema(description = "价格")
    @ExcelProperty("价格")
    private BigDecimal price;

    @Schema(description = "币种")
    @ExcelProperty("币种")
    private String currencyCode;

    @Schema(description = "产品链接")
    @ExcelProperty("产品链接")
    private String url;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer sellableQty;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "店铺产品关联项", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<OmsShopProductItemRespVO> items;
}
