package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 社保方案 Response VO")
@Data
public class HrmInsuranceSchemeRespVO {

    @Schema(description = "社保方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "深圳标准社保方案")
    private String name;

    @Schema(description = "参保地区编号", example = "440300")
    private Integer areaId;

    @Schema(description = "参保地区", example = "广东省 深圳市")
    private String areaName;

    @Schema(description = "户籍类型", example = "深户")
    private String householdType;

    @Schema(description = "方案类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "全部社保项目")
    private List<HrmInsuranceSchemeProjectRespVO> projectList;

    @Schema(description = "社保项目")
    private List<HrmInsuranceSchemeProjectRespVO> socialSecurityProjectList;

    @Schema(description = "公积金项目")
    private List<HrmInsuranceSchemeProjectRespVO> providentFundProjectList;

    @Schema(description = "个人社保金额", example = "800.00")
    private BigDecimal personalInsuranceAmount;

    @Schema(description = "公司社保金额", example = "1600.00")
    private BigDecimal corporateInsuranceAmount;

    @Schema(description = "个人公积金金额", example = "700.00")
    private BigDecimal personalProvidentFundAmount;

    @Schema(description = "公司公积金金额", example = "700.00")
    private BigDecimal corporateProvidentFundAmount;

    @Schema(description = "使用人数", example = "10")
    private Long useCount;

    @Schema(description = "历史月记录数", example = "120")
    private Long monthRecordCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
