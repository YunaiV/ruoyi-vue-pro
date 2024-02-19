package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.framework.common.validation.Telephone;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerLevelEnum;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmCustomerIndustryParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmCustomerLevelParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmCustomerSourceParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.SysAreaParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_INDUSTRY;

@Schema(description = "管理后台 - CRM 线索创建/更新 Request VO")
@Data
public class CrmClueSaveReqVO {

    @Schema(description = "编号", example = "10969")
    private Long id;

    @Schema(description = "线索名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "线索xxx")
    @DiffLogField(name = "线索名称")
    @NotEmpty(message = "线索名称不能为空")
    private String name;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DiffLogField(name = "最后跟进时间")
    private LocalDateTime contactLastTime;

    @Schema(description = "下次联系时间", example = "2023-10-18 01:00:00")
    @DiffLogField(name = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

    @Schema(description = "负责人编号", example = "2048")
    @NotNull(message = "负责人编号不能为空")
    private Long ownerUserId;

    @Schema(description = "手机号", example = "18000000000")
    @DiffLogField(name = "手机号")
    @Mobile
    private String mobile;

    @Schema(description = "电话", example = "18000000000")
    @DiffLogField(name = "电话")
    @Telephone
    private String telephone;

    @Schema(description = "QQ", example = "123456789")
    @DiffLogField(name = "QQ")
    @Size(max = 20, message = "QQ长度不能超过 20 个字符")
    private String qq;

    @Schema(description = "微信", example = "123456789")
    @DiffLogField(name = "微信")
    @Size(max = 255, message = "微信长度不能超过 255 个字符")
    private String wechat;

    @Schema(description = "邮箱", example = "123456789@qq.com")
    @DiffLogField(name = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过 255 个字符")
    private String email;

    @Schema(description = "地区编号", example = "20158")
    @DiffLogField(name = "地区编号", function = SysAreaParseFunction.NAME)
    private Integer areaId;

    @Schema(description = "详细地址", example = "北京市海淀区")
    @DiffLogField(name = "详细地址")
    private String detailAddress;

    @Schema(description = "所属行业", example = "1")
    @DiffLogField(name = "所属行业", function = CrmCustomerIndustryParseFunction.NAME)
    @DictFormat(CRM_CUSTOMER_INDUSTRY)
    private Integer industryId;

    @Schema(description = "客户等级", example = "2")
    @DiffLogField(name = "客户等级", function = CrmCustomerLevelParseFunction.NAME)
    @InEnum(CrmCustomerLevelEnum.class)
    private Integer level;

    @Schema(description = "客户来源", example = "3")
    @DiffLogField(name = "客户来源", function = CrmCustomerSourceParseFunction.NAME)
    private Integer source;

    @Schema(description = "客户描述", example = "任意文字")
    @DiffLogField(name = "客户描述")
    @Size(max = 4096, message = "客户描述长度不能超过 4096 个字符")
    private String description;

    @Schema(description = "备注", example = "随便")
    @DiffLogField(name = "备注")
    private String remark;

}
