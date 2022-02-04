package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.UserSessionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

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
     * 移除超时的在线用户
     *
     * @return {@link Long } 移出的超时用户数量
     **/
    long clearSessionTimeout();

    /**
     * 创建在线用户 Session
     *
     * @param loginUser 登录用户
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return Session 编号
     */
    String createUserSession(LoginUser loginUser, String userIp, String userAgent);

    /**
     * 刷新在线用户 Session 的更新时间
     *
     * @param sessionId Session 编号
     * @param loginUser 登录用户
     */
    void refreshUserSession(String sessionId, LoginUser loginUser);

    /**
     * 删除在线用户 Session
     *
     * @param sessionId Session 编号
     */
    void deleteUserSession(String sessionId);

    /**
     * 获得 Session 编号对应的在线用户
     *
     * @param sessionId Session 编号
     * @return 在线用户
     */
    LoginUser getLoginUser(String sessionId);

    /**
     * 获得 Session 超时时间，单位：毫秒
     *
     * @return 超时时间
     */
    Long getSessionTimeoutMillis();

}
