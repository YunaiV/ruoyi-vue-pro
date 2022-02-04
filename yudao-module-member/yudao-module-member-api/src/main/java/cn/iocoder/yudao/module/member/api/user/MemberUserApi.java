package cn.iocoder.yudao.module.member.api.user;

import cn.iocoder.yudao.module.member.api.user.dto.UserRespDTO;

/**
 * 会员用户的 API 接口
 *
 * @author 芋道源码
 */
public interface MemberUserApi {

    /**
     * 获得会员用户信息
     *
     * @param id 用户编号
     * @return 用户信息
     */
    UserRespDTO getUser(Long id);

}
