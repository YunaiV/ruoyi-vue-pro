package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 品牌 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class BrandExcelVO {

    @ExcelProperty("品牌编号")
    private Long id;

    @ExcelProperty("分类编号")
    private Long categoryId;

    @ExcelProperty("品牌名称")
    private String name;

    @ExcelProperty("品牌图片")
    private String bannerUrl;

    @ExcelProperty("品牌排序")
    private Integer sort;

    @ExcelProperty("品牌描述")
    private String description;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("创建时间")
    private Date createTime;

}
