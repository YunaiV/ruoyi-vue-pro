package cn.iocoder.yudao.module.jl.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 用户信息 Order 设置，用于分页使用
 */
@Data
public class UserPageOrder {

    @Schema(allowableValues = {"desc", "asc"})
    private String id;

    @Schema(allowableValues = {"desc", "asc"})
    private String username;

    @Schema(allowableValues = {"desc", "asc"})
    private String nickname;

    @Schema(allowableValues = {"desc", "asc"})
    private String remark;

    @Schema(allowableValues = {"desc", "asc"})
    private String deptId;

    @Schema(allowableValues = {"desc", "asc"})
    private String postIds;

    @Schema(allowableValues = {"desc", "asc"})
    private String email;

    @Schema(allowableValues = {"desc", "asc"})
    private String mobile;

    @Schema(allowableValues = {"desc", "asc"})
    private String sex;

    @Schema(allowableValues = {"desc", "asc"})
    private String avatar;

    @Schema(allowableValues = {"desc", "asc"})
    private String status;

}
