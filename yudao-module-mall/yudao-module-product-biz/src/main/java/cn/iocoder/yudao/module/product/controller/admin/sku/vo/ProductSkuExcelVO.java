package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 商品sku Excel VO
 *
 * @author 芋道源码
 */
@Data
public class ProductSkuExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("spu编号")
    private Long spuId;

    // TODO @franky：这个单元格，可能会有点展示的问题
    @ExcelProperty("规格值数组-json格式， [{propertId: , valueId: }, {propertId: , valueId: }]")
    private List<Property> properties;

    @ExcelProperty("销售价格，单位：分")
    private Integer price;

    @ExcelProperty("原价， 单位： 分")
    private Integer originalPrice;

    @ExcelProperty("成本价，单位： 分")
    private Integer costPrice;

    @ExcelProperty("条形码")
    private String barCode;

    @ExcelProperty("图片地址")
    private String picUrl;

    @ExcelProperty("状态： 0-正常 1-禁用")
    private Integer status;

    @ExcelProperty("创建时间")
    private Date createTime;

    @Data
    public static class Property {
        private Integer propertyId;
        private Integer valueId;
    }
}
