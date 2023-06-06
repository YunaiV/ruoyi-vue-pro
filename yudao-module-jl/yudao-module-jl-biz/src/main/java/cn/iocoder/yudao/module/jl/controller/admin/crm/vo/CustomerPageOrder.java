package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

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
 * 客户 Order 设置，用于分页使用
 */
@Data
public class CustomerPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String creator;

    @Schema(allowableValues = {"desc", "asc"})
    private String createTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String name;

    @Schema(allowableValues = {"desc", "asc"})
    private String source;

    @Schema(allowableValues = {"desc", "asc"})
    private String phone;

    @Schema(allowableValues = {"desc", "asc"})
    private String email;

    @Schema(allowableValues = {"desc", "asc"})
    private String mark;

    @Schema(allowableValues = {"desc", "asc"})
    private String wechat;

    @Schema(allowableValues = {"desc", "asc"})
    private String doctorProfessionalRank;

    @Schema(allowableValues = {"desc", "asc"})
    private String hospitalDepartment;

    @Schema(allowableValues = {"desc", "asc"})
    private String academicTitle;

    @Schema(allowableValues = {"desc", "asc"})
    private String academicCredential;

    @Schema(allowableValues = {"desc", "asc"})
    private String hospitalId;

    @Schema(allowableValues = {"desc", "asc"})
    private String universityId;

    @Schema(allowableValues = {"desc", "asc"})
    private String companyId;

    @Schema(allowableValues = {"desc", "asc"})
    private String province;

    @Schema(allowableValues = {"desc", "asc"})
    private String city;

    @Schema(allowableValues = {"desc", "asc"})
    private String area;

    @Schema(allowableValues = {"desc", "asc"})
    private String type;

    @Schema(allowableValues = {"desc", "asc"})
    private String dealCount;

    @Schema(allowableValues = {"desc", "asc"})
    private String dealTotalAmount;

    @Schema(allowableValues = {"desc", "asc"})
    private String arrears;

    @Schema(allowableValues = {"desc", "asc"})
    private String lastFollowupTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String salesId;

    @Schema(allowableValues = {"desc", "asc"})
    private String lastFollowupId;

    @Schema(allowableValues = {"desc", "asc"})
    private String lastSalesleadId;

}
