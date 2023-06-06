package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 销售线索中的客户方案 Order 设置，用于分页使用
 */
@Data
public class SalesleadCustomerPlanPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String createTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String salesleadId;

    @Schema(allowableValues = {"desc", "asc"})
    private String fileUrl;

    @Schema(allowableValues = {"desc", "asc"})
    private String fileName;

}
