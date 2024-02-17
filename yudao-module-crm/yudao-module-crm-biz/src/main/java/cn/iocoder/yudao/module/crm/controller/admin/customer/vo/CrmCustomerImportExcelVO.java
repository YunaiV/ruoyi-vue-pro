package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
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

    // TODO @puhui999：industryId、level、source 字段，可以研究下怎么搞下拉框
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

    @ExcelProperty("QQ")
    private String qq;

    @ExcelProperty("微信")
    private String wechat;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("客户描述")
    private String description;

    @ExcelProperty("备注")
    private String remark;

    // TODO @puhui999：需要选择省市区，需要研究下，怎么搞合理点；
    @ExcelProperty("地区编号")
    private Integer areaId;

    @ExcelProperty("详细地址")
    private String detailAddress;

}
