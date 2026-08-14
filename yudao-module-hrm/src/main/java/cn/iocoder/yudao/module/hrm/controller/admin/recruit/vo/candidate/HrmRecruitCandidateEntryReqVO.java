package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 招聘候选人转员工 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmRecruitCandidateEntryReqVO extends HrmEmployeeSaveReqVO {

    @AssertTrue(message = "候选人编号不能为空")
    @JsonIgnore
    public boolean isCandidateIdValid() {
        return getCandidateId() != null;
    }

}
