package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 客户 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CustomerBaseVO {

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "姓名不能为空")
    private String name;

    @Schema(description = "客户来源", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "客户来源不能为空")
    private String source;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "手机号不能为空")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "备注")
    private String mark;

    @Schema(description = "微信号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "微信号不能为空")
    private String wechat;

    @Schema(description = "医生职业级别")
    private String doctorProfessionalRank;

    @Schema(description = "医院科室")
    private String hospitalDepartment;

    @Schema(description = "学校职称")
    private String academicTitle;

    @Schema(description = "学历")
    private String academicCredential;

    @Schema(description = "医院", example = "13346")
    private Long hospitalId;

    @Schema(description = "学校机构", example = "32115")
    private Long universityId;

    @Schema(description = "公司", example = "14623")
    private Long companyId;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String area;

    @Schema(description = "客户类型", example = "2")
    private String type;

    @Schema(description = "成交次数", example = "2365")
    private Integer dealCount;

    @Schema(description = "成交总额")
    private Long dealTotalAmount;

    @Schema(description = "欠款总额")
    private Long arrears;

    @Schema(description = "最后一次跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime lastFollowupTime;

    @Schema(description = "当前负责的销售人员", example = "5204")
    private Long salesId;

    @Schema(description = "最后一次的跟进 id", example = "5860")
    private Long lastFollowupId;

    @Schema(description = "最后一次销售线索", example = "17776")
    private Long lastSalesleadId;

    private Institution company;

}
