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

    @ExcelProperty("快递公司编码")
    private String code;

    @ExcelProperty("快递公司名称")
    private String name;

    @ExcelProperty("快递公司 logo")
    private String logo;

    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty("状态")
    // TODO @jason：可以使用     @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Byte status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
