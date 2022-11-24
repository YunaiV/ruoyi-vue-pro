package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel;

import lombok.*;

import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 支付渠道 Excel VO
 *
 * @author 芋艿
 */
@Data
public class PayChannelExcelVO {

    @ExcelProperty("商户编号")
    private Long id;

    @ExcelProperty("渠道编码")
    private String code;

    @ExcelProperty("开启状态")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("渠道费率，单位：百分比")
    private Double feeRate;

    @ExcelProperty("商户编号")
    private Long merchantId;

    @ExcelProperty("应用编号")
    private Long appId;

    /**
     * todo @芋艿 mapStruct 存在转换问题
     * java: Can't map property "PayClientConfig payChannelDO.config" to "String payChannelExcelVO.config".
     * Consider to declare/implement a mapping method: "String map(PayClientConfig value)".
     */
    /// @ExcelProperty("支付渠道配置")
    /// private String config;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
