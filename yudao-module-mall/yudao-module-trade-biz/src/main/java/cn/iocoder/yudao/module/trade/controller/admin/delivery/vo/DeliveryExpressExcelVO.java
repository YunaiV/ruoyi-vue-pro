package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递公司 Excel VO
 *
 * @author jason
 */
@Data
public class DeliveryExpressExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("快递公司编号")
    private String code;

    @ExcelProperty("快递公司名称")
    private String name;

    @ExcelProperty("快递公司logo")
    private String logo;

    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty("状态（0正常 1停用）")
    private Byte status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
