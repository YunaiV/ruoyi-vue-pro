package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 商品spu Excel VO
 *
 * @author 芋道源码
 */
@Data
public class SpuExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("创建时间")
    private Date createTime;

    @ExcelProperty("商品名称")
    private String name;

    @ExcelProperty("上下架状态： true 上架，false 下架")
    private Boolean visible;

    @ExcelProperty("卖点")
    private String sellPoint;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("分类id")
    private Integer cid;

    @ExcelProperty("列表图")
    private String listPicUrl;

    @ExcelProperty("商品主图地址, 数组，以逗号分隔, 最多上传15张")
    private String picUrls;

    @ExcelProperty("排序字段")
    private Integer sort;

    @ExcelProperty("点赞初始人数")
    private Integer likeCount;

    @ExcelProperty("价格")
    private Integer price;

    @ExcelProperty("库存数量")
    private Integer quantity;

}
