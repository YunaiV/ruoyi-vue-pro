package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 支付商户信息 Excel VO
 *
 * @author 芋艿
 */
@Data
public class PayMerchantExcelVO {

    @ExcelProperty("商户编号")
    private Long id;

    @ExcelProperty("商户号")
    private String no;

    @ExcelProperty("商户全称")
    private String name;

    @ExcelProperty("商户简称")
    private String shortName;

    @ExcelProperty(value = "开启状态",converter = DictConvert.class)
    @DictFormat("sys_common_status")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
