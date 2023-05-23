package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递运费模板 Excel VO
 *
 * @author jason
 */
@Data
public class DeliveryExpressTemplateExcelVO {

    @ExcelProperty("编号，自增")
    private Long id;

    @ExcelProperty("模板名称")
    private String name;

    @ExcelProperty("配送计费方式 1:按件 2:按重量 3:按体积")
    private Integer chargeMode;

    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
