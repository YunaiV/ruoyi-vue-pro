package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.product.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


import java.time.LocalDateTime;

/**
 * 商品 Spu Excel 导出 VO TODO 暂定
 *
 * @author HUIHUI
 */
@Data
public class ProductSpuExcelVO {

    @ExcelProperty("商品编号")
    private Long id;

    @ExcelProperty("商品名称")
    private String name;

    @ExcelProperty("关键字")
    private String keyword;

    @ExcelProperty("商品简介")
    private String introduction;

    @ExcelProperty("商品详情")
    private String description;

    @ExcelProperty("条形码")
    private String barCode;

    @ExcelProperty("商品分类编号")
    private Long categoryId;

    @ExcelProperty("商品品牌编号")
    private Long brandId;

    @ExcelProperty("商品封面图")
    private String picUrl;

    @ExcelProperty("排序字段")
    private Integer sort;

    @ExcelProperty(value = "商品状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.PRODUCT_SPU_STATUS)
    private Integer status;

    @ExcelProperty("规格类型")
    private Boolean specType;

    @ExcelProperty("商品价格")
    private Integer price;

    @ExcelProperty("市场价")
    private Integer marketPrice;

    @ExcelProperty("成本价")
    private Integer costPrice;

    @ExcelProperty("库存")
    private Integer stock;

    @ExcelProperty("物流配置模板编号")
    private Long deliveryTemplateId;

    @ExcelProperty("赠送积分")
    private Integer giveIntegral;

    @ExcelProperty("分销类型")
    private Boolean subCommissionType;

    @ExcelProperty("商品销量")
    private Integer salesCount;

    @ExcelProperty("虚拟销量")
    private Integer virtualSalesCount;

    @ExcelProperty("商品点击量")
    private Integer browseCount;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
