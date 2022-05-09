package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.UserSessionDO;

/**
 * 在线用户 Session Service 接口
 *
 * @author 芋道源码
 */
public interface UserSessionService {

    /**
     * 获得在线用户分页列表
     *
     * @param reqVO 分页条件
     * @return 份额与列表
     */
    PageResult<UserSessionDO> getUserSessionPage(UserSessionPageReqVO reqVO);

    /**
     * 删除在线用户 Session
     *
     * @param token token 令牌
     */
    void deleteUserSession(String token);

    /**
     * 删除在线用户 Session
     *
     * @param id 编号
     */
    void deleteUserSession(Long id);

}
