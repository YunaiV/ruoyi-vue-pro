package cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 积分设置 Excel VO
 *
 * @author QingX
 */
@Data
public class PointConfigExcelVO {

    @ExcelProperty("自增主键")
    private Integer id;

    @ExcelProperty(value = "1 开启积分抵扣 0 关闭积分抵扣", converter = DictConvert.class)
    @DictFormat("infra_boolean_string") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer tradeDeductEnable;

    @ExcelProperty("积分抵扣，抵扣最低为分 以0.01表示 1积分抵扣0.01元(单位：元)")
    private BigDecimal tradeDeductUnitPrice;

    @ExcelProperty("积分抵扣最大值")
    private Long tradeDeductMaxPrice;

    @ExcelProperty("1元赠送多少分")
    private Long tradeGivePoint;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("变更时间")
    private LocalDateTime updateTime;

}
