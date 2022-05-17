package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 商品sku Excel VO
 *
 * @author 芋道源码
 */
@Data
public class SkuExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("创建时间")
    private Date createTime;

    @ExcelProperty("spu编号")
    private Integer spuId;

    @ExcelProperty("状态： 1-正常 2-禁用")
    private Integer skuStatus;

    @ExcelProperty("规格值数组， 以逗号隔开")
    private String attrs;

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

}
