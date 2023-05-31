package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 友商 Order 设置，用于分页使用
 */
@Data
public class CompetitorPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String createTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String name;

    @Schema(allowableValues = {"desc", "asc"})
    private String contactName;

    @Schema(allowableValues = {"desc", "asc"})
    private String phone;

    @Schema(allowableValues = {"desc", "asc"})
    private String type;

    @Schema(allowableValues = {"desc", "asc"})
    private String advantage;

    @Schema(allowableValues = {"desc", "asc"})
    private String disadvantage;

    @Schema(allowableValues = {"desc", "asc"})
    private String mark;

}
