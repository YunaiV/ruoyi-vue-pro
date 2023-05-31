package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 销售跟进 Order 设置，用于分页使用
 */
@Data
public class FollowupPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String createTime;

    @Schema(allowableValues = {"desc", "asc"})
    private String content;

    @Schema(allowableValues = {"desc", "asc"})
    private String customerId;

    @Schema(allowableValues = {"desc", "asc"})
    private String refId;

    @Schema(allowableValues = {"desc", "asc"})
    private String type;

}
