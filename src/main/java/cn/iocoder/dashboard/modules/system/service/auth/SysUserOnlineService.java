package cn.iocoder.dashboard.modules.system.service.auth;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.auth.SysUserSessionDO;

import java.util.Date;

/**
 * 在线用户 Session Service 接口
 */
public interface SysUserOnlineService {

    /**
     * 创建在线用户 Session
     *
     * @param sessionId Session 编号
     * @param userId 用户编号
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     */
    void createUserOnline(String sessionId, Long userId, String userIp, String userAgent);

    /**
     * 更新在线用户 Session 的更新时间
     *
     * @param sessionId Session 编号
     * @param updateTime 更新时间
     */
    void updateUserOnlineUpdateTime(String sessionId, Date updateTime);

    /**
     * 获得在线用户分页列表
     *
     * @param reqVO 分页条件
     * @return 份额与列表
     */
    PageResult<SysUserSessionDO> getUserSessionPage(SysUserSessionPageReqVO reqVO);

}
