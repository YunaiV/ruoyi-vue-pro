package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 项目的实验名目 Order 设置，用于分页使用
 */
@Data
public class ProjectCategoryPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String createTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String quoteId;

    @Schema(allowableValues = {"desc", "asc"})
    private String scheduleId;

    @Schema(allowableValues = {"desc", "asc"})
    private String type;

    @Schema(allowableValues = {"desc", "asc"})
    private String categoryType;

    @Schema(allowableValues = {"desc", "asc"})
    private String categoryId;

    @Schema(allowableValues = {"desc", "asc"})
    private String operatorId;

    @Schema(allowableValues = {"desc", "asc"})
    private String demand;

    @Schema(allowableValues = {"desc", "asc"})
    private String interference;

    @Schema(allowableValues = {"desc", "asc"})
    private String dependIds;

    @Schema(allowableValues = {"desc", "asc"})
    private String name;

    @Schema(allowableValues = {"desc", "asc"})
    private String mark;

}
