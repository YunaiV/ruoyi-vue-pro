package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.infra.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO 芋艿：导出最后做，等基本确认的差不多之后；
/**
 * CRM 客户 Excel VO
 *
 * @author Wanwan
 */
@Data
public class CrmCustomerExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("客户名称")
    private String name;

    @ExcelProperty(value = "跟进状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    private Boolean followUpStatus;

    @ExcelProperty(value = "锁定状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    private Boolean lockStatus;

    @ExcelProperty(value = "成交状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    private Boolean dealStatus;

    @ExcelProperty(value = "所属行业", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_INDUSTRY)
    private Integer industryId;

    @ExcelProperty(value = "客户等级", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_LEVEL)
    private Integer level;

    @ExcelProperty(value = "客户来源", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_SOURCE)
    private Integer source;

    @ExcelProperty("手机")
    private String mobile;

    @ExcelProperty("电话")
    private String telephone;

    @ExcelProperty("网址")
    private String website;

    @ExcelProperty("QQ")
    private String qq;

    @ExcelProperty("wechat")
    private String wechat;

    @ExcelProperty("email")
    private String email;

    @ExcelProperty("客户描述")
    private String description;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;

    @ExcelProperty("地区编号")
    private Integer areaId;

    @ExcelProperty("详细地址")
    private String detailAddress;

    @ExcelProperty("最后跟进时间")
    private LocalDateTime contactLastTime;

    @ExcelProperty("下次联系时间")
    private LocalDateTime contactNextTime;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
