package cn.iocoder.yudao.module.product.controller.admin.category.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;


/**
 * 商品分类 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class CategoryExcelVO {

    @ExcelProperty("分类编号")
    private Long id;

    @ExcelProperty("父分类编号")
    private Long parentId;

    @ExcelProperty("分类名称")
    private String name;

    @ExcelProperty("分类图标")
    private String icon;

    @ExcelProperty("分类图片")
    private String bannerUrl;

    @ExcelProperty("分类排序")
    private Integer sort;

    @ExcelProperty("分类描述")
    private String description;

    @ExcelProperty(value = "开启状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("创建时间")
    private Date createTime;

}
