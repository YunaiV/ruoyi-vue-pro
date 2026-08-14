package cn.iocoder.yudao.module.hrm.controller.admin.portal.employee.vo.employee;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.hrm.enums.employee.experience.HrmEmployeeEducationEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeIdTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工端档案更新 Request VO")
@Data
public class HrmPortalEmployeeUpdateReqVO {

    @Schema(description = "员工姓名", example = "张三")
    @Size(max = 255, message = "员工姓名不能超过 255 个字符")
    private String name;

    @Schema(description = "手机号", example = "15601691300")
    @Mobile
    private String mobile;

    @Schema(description = "国家或地区", example = "中国")
    @Size(max = 64, message = "国家或地区不能超过 64 个字符")
    private String country;

    @Schema(description = "民族", example = "汉族")
    @Size(max = 64, message = "民族不能超过 64 个字符")
    private String nation;

    @Schema(description = "证件类型", example = "1")
    @InEnum(value = HrmEmployeeIdTypeEnum.class, message = "证件类型必须是 {value}")
    private Integer idType;

    @Schema(description = "证件号码", example = "310101199001011234")
    @Size(max = 255, message = "证件号码不能超过 255 个字符")
    private String idNumber;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "邮箱", example = "hrm@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱不能超过 255 个字符")
    private String email;

    @Schema(description = "籍贯", example = "浙江杭州")
    @Size(max = 128, message = "籍贯不能超过 128 个字符")
    private String nativePlace;

    @Schema(description = "出生日期")
    @PastOrPresent(message = "出生日期不能晚于当前时间")
    private LocalDateTime birthday;

    @Schema(description = "户籍地址", example = "杭州市西湖区")
    @Size(max = 255, message = "户籍地址不能超过 255 个字符")
    private String address;

    @Schema(description = "最高学历", example = "8")
    @InEnum(value = HrmEmployeeEducationEnum.class, message = "最高学历必须是 {value}")
    private Integer highestEducation;

    @AssertTrue(message = "身份证号码格式不正确")
    @JsonIgnore
    public boolean isIdNumberValid() {
        return ObjUtil.notEqual(HrmEmployeeIdTypeEnum.ID_CARD.getType(), idType)
                || StrUtil.isBlank(idNumber) || IdcardUtil.isValidCard(idNumber);
    }

}
