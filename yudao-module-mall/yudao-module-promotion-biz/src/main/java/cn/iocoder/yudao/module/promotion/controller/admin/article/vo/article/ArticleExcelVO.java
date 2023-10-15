package cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 文章管理 Excel VO
 *
 * @author HUIHUI
 */
@Data
public class ArticleExcelVO {

    @ExcelProperty("文章编号")
    private Long id;

    @ExcelProperty("文章分类编号")
    private Long categoryId;

    @ExcelProperty("关联商品编号")
    private Long spuId;

    @ExcelProperty("文章标题")
    private String title;

    @ExcelProperty("文章作者")
    private String author;

    @ExcelProperty("文章封面图片地址")
    private String picUrl;

    @ExcelProperty("文章简介")
    private String introduction;

    @ExcelProperty("浏览次数")
    private String browseCount;

    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty("是否热门(小程序)")
    private Boolean recommendHot;

    @ExcelProperty("是否轮播图(小程序)")
    private Boolean recommendBanner;

    @ExcelProperty("文章内容")
    private String content;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
