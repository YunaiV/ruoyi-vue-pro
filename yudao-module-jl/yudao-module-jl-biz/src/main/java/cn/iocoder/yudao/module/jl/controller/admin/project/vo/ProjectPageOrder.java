package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 项目管理 Order 设置，用于分页使用
 */
@Data
public class ProjectPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String createTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String salesleadId;

    @Schema(allowableValues = {"desc", "asc"})
    private String name;

    @Schema(allowableValues = {"desc", "asc"})
    private String stage;

    @Schema(allowableValues = {"desc", "asc"})
    private String status;

    @Schema(allowableValues = {"desc", "asc"})
    private String type;

    @Schema(allowableValues = {"desc", "asc"})
    private String startDate;

    @Schema(allowableValues = {"desc", "asc"})
    private String endDate;

    @Schema(allowableValues = {"desc", "asc"})
    private String managerId;

    @Schema(allowableValues = {"desc", "asc"})
    private String participants;

    @Schema(allowableValues = {"desc", "asc"})
    private String salesId;

    @Schema(allowableValues = {"desc", "asc"})
    private String customerId;

}
