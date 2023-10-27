package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.framework.common.validation.Telephone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    // TODO wanwan：这个字段应该只有 RespVO 会有；创建和修改不传递；
    @Schema(description = "跟进状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "跟进状态不能为空")
    private Boolean followUpStatus;

    // TODO wanwan：这个字段应该只有 RespVO 会有；创建和修改不传递；
    @Schema(description = "锁定状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "锁定状态不能为空")
    private Boolean lockStatus;

    @Schema(description = "手机", example = "18000000000")
    @Mobile
    private String mobile;

    @Schema(description = "电话", example = "18000000000")
    @Telephone
    private String telephone;

    @Schema(description = "网址", example = "https://www.baidu.com")
    private String website;

    @Schema(description = "备注", example = "随便")
    private String remark;

    // TODO wanwan：这个字段应该只有 RespVO 会有；创建和修改不传递；因为它会在“移交”里面做哈
    @Schema(description = "负责人的用户编号", example = "25682")
    @NotNull(message = "负责人不能为空")
    private Long ownerUserId;

    @Schema(description = "地区编号", example = "20158")
    private Long areaId;

    @Schema(description = "详细地址", example = "北京市海淀区")
    private String detailAddress;

    // TODO @芋艿：longitude、latitude 这两个字段删除；
    @Schema(description = "地理位置经度", example = "116.40341")
    private String longitude;

    @Schema(description = "地理位置维度", example = "39.92409")
    private String latitude;

    // TODO wanwan：这个字段应该只有 RespVO 会有；创建和修改不传递；
    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactLastTime;

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

}
