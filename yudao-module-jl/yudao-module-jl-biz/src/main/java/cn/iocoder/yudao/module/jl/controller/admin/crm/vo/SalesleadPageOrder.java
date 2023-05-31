package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 销售线索 Order 设置，用于分页使用
 */
@Data
public class SalesleadPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String createTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String source;

    @Schema(allowableValues = {"desc", "asc"})
    private String requirement;

    @Schema(allowableValues = {"desc", "asc"})
    private String budget;

    @Schema(allowableValues = {"desc", "asc"})
    private String quotation;

    @Schema(allowableValues = {"desc", "asc"})
    private String status;

    @Schema(allowableValues = {"desc", "asc"})
    private String customerId;

    @Schema(allowableValues = {"desc", "asc"})
    private String projectId;

    @Schema(allowableValues = {"desc", "asc"})
    private String businessType;

    @Schema(allowableValues = {"desc", "asc"})
    private String lostNote;

    @Schema(allowableValues = {"desc", "asc"})
    private String managerId;

}
