package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

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

    @Schema(description = "医院id", example = "13346")
    private Long hospitalId;

    @Schema(description = "学校机构id", example = "32115")
    private Long universityId;

    @Schema(description = "公司id", example = "14623")
    private Long companyId;

    @Schema(description = "省", example = "1")
    private String province;

    @Schema(description = "市", example = "2")
    private String city;

    @Schema(description = "区", example = "1")
    private String area;

    @Schema(description = "客户类型", example = "医院")
    private String type;

    @Schema(description = "成交次数", example = "1")
    private Integer dealCount;

    @Schema(description = "成交总额", example = "100")
    private Integer dealTotalAmount;

    @Schema(description = "欠款", example = "100")
    private Integer arrears;

    @Schema(description = "负责的销售id")
    private Long salesId;

    @Schema(description = "最近跟进记录", example = "最近跟进记录")
    private LocalDateTime lastFollowupTime;

    @Schema(description = "最近一次跟进 id")
    private Long lastFollowupId;

    @Schema(description = "最近一次销售线索 id")
    private Long lastSalesleadId;
}
