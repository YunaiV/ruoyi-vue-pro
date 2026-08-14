package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 招聘候选人新增/修改 Request VO")
@Data
public class HrmRecruitCandidateSaveReqVO {

    @Schema(description = "候选人编号", example = "1024")
    private Long id;

    @Schema(description = "候选人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "候选人姓名不能为空")
    @Size(max = 255, message = "候选人姓名长度不能超过 255 个字符")
    @DiffLogField(name = "候选人姓名")
    private String name;

    @Schema(description = "手机号码", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691399")
    @NotBlank(message = "手机号码不能为空")
    @Size(max = 18, message = "手机号码长度不能超过 18 个字符")
    @Pattern(regexp = "^(\\+?0?\\d{2,4}-?)?\\d{6,11}$", message = "手机号码格式不正确")
    @DiffLogField(name = "手机号码")
    private String mobile;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "性别不能为空")
    @DiffLogField(name = "性别")
    private Integer sex;

    @Schema(description = "年龄", example = "28")
    @Min(value = 0, message = "年龄不能小于 0")
    @Max(value = 99, message = "年龄不能大于 99")
    @DiffLogField(name = "年龄")
    private Integer age;

    @Schema(description = "邮箱", example = "candidate@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过 255 个字符")
    @DiffLogField(name = "邮箱")
    private String email;

    @Schema(description = "应聘职位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "应聘职位不能为空")
    @DiffLogField(name = "应聘职位")
    private Long postId;

    @Schema(description = "工作年限", example = "5")
    @Min(value = 0, message = "工作年限不能小于 0")
    @Max(value = 60, message = "工作年限不能大于 60")
    @DiffLogField(name = "工作年限")
    private Integer workTime;

    @Schema(description = "学历", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "学历不能为空")
    @DiffLogField(name = "学历")
    private Integer education;

    @Schema(description = "毕业院校", example = "浙江大学")
    @Size(max = 255, message = "毕业院校长度不能超过 255 个字符")
    @DiffLogField(name = "毕业院校")
    private String graduateSchool;

    @Schema(description = "最近工作单位", example = "某科技公司")
    @Size(max = 255, message = "最近工作单位长度不能超过 255 个字符")
    @DiffLogField(name = "最近工作单位")
    private String latestWorkPlace;

    @Schema(description = "招聘渠道编号", example = "1")
    @DiffLogField(name = "招聘渠道")
    private Long channelId;

    @Schema(description = "备注", example = "沟通意向强")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "简历附件地址数组", example = "[\"https://example.com/resume.pdf\"]")
    @Size(max = 5, message = "简历附件不能超过 5 个")
    @DiffLogField(name = "简历附件")
    private List<@Size(max = 512, message = "简历附件地址长度不能超过 512 个字符") String> resumeUrls;

}
