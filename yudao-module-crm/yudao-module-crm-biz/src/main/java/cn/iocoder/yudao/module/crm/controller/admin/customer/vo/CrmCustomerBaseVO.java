package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.framework.common.validation.Telephone;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 客户 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmCustomerBaseVO {

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "客户名称不能为空")
    private String name;

    @Schema(description = "所属行业", example = "1")
    private Integer industryId;

    @Schema(description = "客户等级", example = "2")
    @InEnum(CrmCustomerLevelEnum.class)
    private Integer level;

    @Schema(description = "客户来源", example = "3")
    private Integer source;

    @Schema(description = "手机", example = "18000000000")
    @Mobile
    private String mobile;

    @Schema(description = "电话", example = "18000000000")
    @Telephone
    private String telephone;

    @Schema(description = "网址", example = "https://www.baidu.com")
    private String website;

    @Schema(description = "QQ", example = "123456789")
    @Size(max = 20, message = "QQ长度不能超过 20 个字符")
    private String qq;

    @Schema(description = "wechat", example = "123456789")
    @Size(max = 255, message = "微信长度不能超过 255 个字符")
    private String wechat;

    @Schema(description = "email", example = "123456789@qq.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过 255 个字符")
    private String email;

    @Schema(description = "客户描述", example = "任意文字")
    @Size(max = 4096, message = "客户描述长度不能超过 4096 个字符")
    private String description;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "地区编号", example = "20158")
    private Integer areaId;

    @Schema(description = "详细地址", example = "北京市海淀区")
    private String detailAddress;

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

}
