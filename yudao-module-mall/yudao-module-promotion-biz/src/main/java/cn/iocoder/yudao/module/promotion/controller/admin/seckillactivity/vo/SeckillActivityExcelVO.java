package cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 秒杀活动 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class SeckillActivityExcelVO {

    @ExcelProperty("秒杀活动名称")
    private String name;

    @ExcelProperty(value = "活动状态", converter = DictConvert.class)
    @DictFormat("promotion_activity_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("活动开始时间")
    private Date startTime;

    @ExcelProperty("活动结束时间")
    private Date endTime;

    @ExcelProperty("付款订单数")
    private Integer orderCount;

    @ExcelProperty("付款人数")
    private Integer userCount;

    @ExcelProperty("订单实付金额，单位：分")
    private BigDecimal totalPrice;

    @ExcelProperty("创建时间")
    private Date createTime;

}
