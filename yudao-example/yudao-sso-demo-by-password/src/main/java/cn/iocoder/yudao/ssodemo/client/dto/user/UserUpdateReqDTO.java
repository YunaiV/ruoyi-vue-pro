package cn.iocoder.yudao.ssodemo.client.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户基本信息 Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateReqDTO {

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 用户性别
     */
    private Integer sex;

}
