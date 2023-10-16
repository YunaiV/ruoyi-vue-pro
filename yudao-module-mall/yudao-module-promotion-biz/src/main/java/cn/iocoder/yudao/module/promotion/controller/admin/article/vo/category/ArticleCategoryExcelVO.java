package cn.iocoder.yudao.module.promotion.controller.admin.article.vo.category;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 文章分类 Excel VO
 *
 * @author HUIHUI
 */
@Data
public class ArticleCategoryExcelVO {

    @ExcelProperty("文章分类编号")
    private Long id;

    @ExcelProperty("文章分类名称")
    private String name;

    @ExcelProperty("图标地址")
    private String picUrl;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
