package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 支付应用信息 Excel VO
 *
 * @author 芋艿
 */
@Data
public class PayAppExcelVO {

    @ExcelProperty("应用编号")
    private Long id;

    @ExcelProperty("应用名")
    private String name;

    @ExcelProperty("开启状态")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("支付结果的回调地址")
    private String payNotifyUrl;

    @ExcelProperty("退款结果的回调地址")
    private String refundNotifyUrl;

    @ExcelProperty("商户编号")
    private Long merchantId;

    @ExcelProperty("创建时间")
    private Date createTime;

}
