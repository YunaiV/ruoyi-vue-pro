package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @puhui999：可以考虑删除 excel 导出哈
/**
 * 拼团商品 Excel VO
 *
 * @author HUIHUI
 */
@Data
public class CombinationProductExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("拼团活动编号")
    private Long activityId;

    @ExcelProperty("商品 SPU 编号")
    private Long spuId;

    @ExcelProperty("商品 SKU 编号")
    private Long skuId;

    @ExcelProperty("拼团商品状态")
    private Integer activityStatus;

    @ExcelProperty("活动开始时间点")
    private LocalDateTime activityStartTime;

    @ExcelProperty("活动结束时间点")
    private LocalDateTime activityEndTime;

    @ExcelProperty("拼团价格，单位分")
    private Integer activePrice;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
