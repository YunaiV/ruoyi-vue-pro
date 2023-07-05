package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户签到积分 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberSignInRecordBaseVO {

    @Schema(description = "签到用户", example = "6507")
    private Integer userId;

    @Schema(description = "第几天签到")
    private Integer day;

    @Schema(description = "签到的分数")
    private Integer point;

}
