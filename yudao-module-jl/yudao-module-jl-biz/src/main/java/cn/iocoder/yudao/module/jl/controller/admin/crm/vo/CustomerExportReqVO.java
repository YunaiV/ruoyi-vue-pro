package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 客户 Excel 导出 Request VO，参数和 CustomerPageReqVO 是一致的")
@Data
public class CustomerExportReqVO {

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "客户来源")
    private String source;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "备注")
    private String mark;

    @Schema(description = "微信号")
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
    private LocalDateTime[] lastFollowupTime;

    @Schema(description = "当前负责的销售人员", example = "5204")
    private Long salesId;

    @Schema(description = "最后一次的跟进 id", example = "5860")
    private Long lastFollowupId;

    @Schema(description = "最后一次销售线索", example = "17776")
    private Long lastSalesleadId;

}
