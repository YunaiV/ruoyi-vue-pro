package cn.iocoder.yudao.module.im.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * IM 用户 Response VO（对 AdminUser 的字段窄投影）
 * <p>
 * 面向 IM 场景，主要关注 id / userName / nickName / headImage / sex / signature 这些字段；
 * 但 yudao 侧 AdminUser 没有 signature 字段，暂不返回（TODO @芋艿：【对齐】后续可加用户扩展表承载）。
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IM 用户 Response VO")
@Data
public class ImUserRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "昵称", example = "芋道")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别 0=未知 1=男 2=女", example = "1")
    private Integer sex;

    @Schema(description = "手机号")
    private String mobile;

    /**
     * TODO @芋艿：【对齐】个性签名（signature）字段当前缺失，yudao AdminUser 无此字段；
     * 如果要对齐，可走 system 模块的用户扩展表，或 IM 模块独立存。
     */

}
