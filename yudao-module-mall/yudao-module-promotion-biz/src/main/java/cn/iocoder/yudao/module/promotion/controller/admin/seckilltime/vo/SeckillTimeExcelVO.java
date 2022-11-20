package cn.iocoder.yudao.module.promotion.controller.admin.seckilltime.vo;

import lombok.*;

import java.time.LocalTime;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 秒杀时段 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class SeckillTimeExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("秒杀时段名称")
    private String name;

    @ExcelProperty("开始时间点")
    private LocalTime startTime;

    @ExcelProperty("结束时间点")
    private LocalTime endTime;

    @ExcelProperty("商品数量")
    private Integer productCount;

    @ExcelProperty("创建时间")
    private Date createTime;

}
