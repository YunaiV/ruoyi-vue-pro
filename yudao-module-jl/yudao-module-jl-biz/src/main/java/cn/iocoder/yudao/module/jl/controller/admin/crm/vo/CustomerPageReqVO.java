package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 客户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomerPageReqVO extends PageParam {

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

    @Schema(description = "type", example = "1")
    private String type;

    @Schema(description = "负责的销售id")
    private Long salesId;

}
