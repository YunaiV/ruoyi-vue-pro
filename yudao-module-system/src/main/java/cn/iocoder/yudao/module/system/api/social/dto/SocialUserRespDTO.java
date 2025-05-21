package cn.iocoder.yudao.module.system.api.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 社交用户 Response DTO
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserRespDTO {

    /**
     * 社交用户的 openid
     */
    private String openid;
    /**
     * 社交用户的昵称
     */
    private String nickname;
    /**
     * 社交用户的头像
     */
    private String avatar;

    /**
     * 关联的用户编号
     */
    private Long userId;

}
