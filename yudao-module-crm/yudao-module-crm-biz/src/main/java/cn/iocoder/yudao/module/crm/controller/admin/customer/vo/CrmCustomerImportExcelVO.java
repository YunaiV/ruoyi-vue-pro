package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.*;

/**
 * 客户 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class CrmCustomerImportExcelVO {

    @ExcelProperty("客户名称")
    private String name;

    @ExcelProperty(value = "所属行业", converter = DictConvert.class)
    @DictFormat(CRM_CUSTOMER_INDUSTRY)
    private Integer industryId;

    @ExcelProperty(value = "客户等级", converter = DictConvert.class)
    @DictFormat(CRM_CUSTOMER_LEVEL)
    private Integer level;

    @ExcelProperty(value = "客户来源", converter = DictConvert.class)
    @DictFormat(CRM_CUSTOMER_SOURCE)
    private Integer source;

    @ExcelProperty("手机")
    private String mobile;

    @ExcelProperty("电话")
    private String telephone;

    @ExcelProperty("网址")
    private String website;

    @Size(max = 20, message = "QQ长度不能超过 20 个字符")
    @ExcelProperty("QQ")
    private String qq;

    @Size(max = 255, message = "微信长度不能超过 255 个字符")
    @ExcelProperty("微信")
    private String wechat;

    @Size(max = 255, message = "邮箱长度不能超过 255 个字符")
    @ExcelProperty("邮箱")
    private String email;

    @Size(max = 4096, message = "客户描述长度不能超过 4096 个字符")
    @ExcelProperty("客户描述")
    private String description;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("地区编号")
    private Integer areaId;

    @ExcelProperty("详细地址")
    private String detailAddress;

}
