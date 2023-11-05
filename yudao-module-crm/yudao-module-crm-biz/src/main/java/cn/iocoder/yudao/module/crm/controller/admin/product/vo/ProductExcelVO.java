package cn.iocoder.yudao.module.crm.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 产品 Excel VO
 *
 * @author ZanGe丶
 */
@Data
public class ProductExcelVO {

    @ExcelProperty("主键id")
    private Long id;

    @ExcelProperty("产品名称")
    private String name;

    @ExcelProperty("产品编码")
    private String no;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("价格")
    private Long price;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("crm_product_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("产品分类ID")
    private Long categoryId;

    @ExcelProperty("产品描述")
    private String description;

    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
